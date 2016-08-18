(ns tracetool
  (:require
   [cemerick.pomegranate]
   [clojure.pprint]
   [clojure.string :as str]))

(defmacro show-env [] (println &env))


(def pid
  "Get current process PID"
  (memoize
   (fn []
     (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
         (.getName)
         (clojure.string/split #"@")
         (first)))))

(defn ppr
  ([title x]
   (println title)
   (clojure.pprint/pprint x))
  ([x] (clojure.pprint/pprint x)))

(defn ppl
  ([x]
   (doseq [y x]
     (clojure.pprint/pprint y))))

(defn pad
  ([n string] (pad n \space string))
  ([n padder string]
   (apply str (take n (concat string
                              (repeat padder))))))
(def ^:dynamic *trace*  true)

(defn transpose
  "transposes a vector of vectors"
  [rc]
  (apply mapv vector rc))

(defn toggle-trace []
  (alter-var-root #'*trace* not))

(defn print-table
  ([table] (print-table table (repeat \space)))
  ([table separators]
   (let [separators (concat separators (repeat \space))
         padding (->> table
                      transpose
                      (map (comp (partial reduce max 0)
                                 (partial map (comp count str))) ,,,))]
     (doseq [ row table]
       (println
        (->> (map (fn [s p]
                    (let [ss (str s)]
                      (str ss (apply str (take (- p (count ss)) (repeat \space))))))
                  (concat row [ ""])
                  (concat padding [0]))
             (interleave separators)
             (apply str)))))))

(defmacro no-trace [& body]
  `(binding [*trace* false]
     ~@body))

(defmacro trace [ & vals]
  `(do (when *trace*
         (printf "--Trace ---%n"  )
         (let [vals#        (vector ~@vals)
               symbol->val# (map vector (quote ~vals) vals#)
               l#           (->> symbol->val#
                                 (map (comp count pr-str first))
                                 (apply max))
               indent#      (-> l#
                                (+ 2)
                                (repeat \space)
                                (->> ,,, (apply str)))]
           (doseq [ [k# v#] symbol->val#]
             (println (pad l# (pr-str k#))
                      "="
                      (clojure.string/replace
                       (apply str (butlast (with-out-str (clojure.pprint/pprint v#)))) ; trim last newline
                       #"\n"
                       (str "\n" indent#))
                      " :: "
                      (type v#)
                      ))
           (println "---------------")
           (last vals#)))))

(defmacro label-trace [ label & vals]
  `(do (when *trace*
         (printf "--Trace: %s ---%n" ~label )
         (let [symbol->val# (map vector (quote ~vals) (list ~@vals))
               l#           (->> symbol->val#
                                 (map (comp count pr-str first))
                                 (apply max))
               indent#      (-> l#
                                (+ 2)
                                (repeat \space)
                                (->> ,,, (apply str)))]
           (doseq [ [k# v#] symbol->val#]
             (println (pad l# (pr-str k#))
                      "="
                      (clojure.string/replace
                       (apply str (butlast (with-out-str (clojure.pprint/pprint v#)))) ; trim last newline
                       #"\n"
                       (str "\n" indent#))
                      " :: "
                      (type v#)
                      )))
         (println "---------------"))
       ~(last vals)))

(defmacro trace-env [ & vals]
  (let [vals (concat (or vals '()) (keys &env))]
    `(when *trace*
       (printf "--Trace-env ---%n"  )
       (let [symbol->val# (map vector (quote ~vals) (list ~@vals))
             l#           (->> symbol->val#
                               (map (comp count pr-str first))
                               (apply max))
             indent#      (-> l#
                              (+ 2)
                              (repeat \space)
                              (->> (apply str)))]
         (doseq [ [k# v#] symbol->val#]
           (println (pad l# (pr-str k#))
                    "="
                    (clojure.string/replace
                     (apply str (butlast (with-out-str (ppr v#)))) ; trim last newline
                     #"\n"
                     (str "\n" indent#))
                    " :: "
                    (type v#)
                    )))
       (println "---------------"))))

(defmacro defn-trace [name args & body]
  (let [result-var (symbol (str "=>" name))
        outs (conj args result-var)
        ]
    `(defn ~name ~args
       (do
         (let [~result-var ~body]
           (label-trace
            '~name ~@outs)
           )))))

(defmacro undef [name]
  (ns-unmap (or (some-> name namespace symbol) *ns*)
            name))

#_(defmacro add-lib [ [lib version] ]
    (let [ coord   [lib version] ]
      (assert symbol? lib)
      (assert string? version)
      (print "adding" coord "to classpath... ")
      ;; add lib to classpath
      (cemerick.pomegranate/add-dependencies
       :coordinates [ coord]
       :repositories {"clojars" "http://clojars.org/repo"
                      "central" "http://repo1.maven.org/maven2/"})
      (println "done")

      ;; add lib to dependencies
      (print "adding" coord "to project.clj... ")
      (let [zright+ (fn [loc]
                      (if-let [next (z/right loc)]
                        (recur next)
                        loc))]

        (some-> "project.clj"
                z/of-file
                z/down
                (z/find-value z/right :dependencies) ; find dependencies key
                z/right ; move to dependencies list
                z/down  ; move into dep list
                zright+ ; mode to last element
                (z/insert-right coord) ;; add coords
                z/right
                z/prepend-newline
                z/->root-string
                (->> (spit "project.clj"))))
      (println "done")))

;; trace-let
(defn- ->debug-sym [n] (symbol (str "<" (name n) ">")))

(defmacro capture-locals [n]
  (let [locals (keys &env)
        lname (->debug-sym n)]
    `(def ~lname (zipmap '~locals (list ~@locals)))))

(defmacro with-locals [n & body]
  (let [m @(resolve (->debug-sym n))]
    `(let ~(vec (mapcat identity m))
       ~@body)))

(comment

  (let [x 1 y 2 z 3]
    (capture-locals A)
    (+ x y z))

  (with-locals A
    (+ x y z))

  )
