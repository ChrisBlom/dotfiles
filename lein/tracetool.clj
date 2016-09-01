(ns tracetool
  (:require
    [clojure.pprint]
    [clojure.string :as str]
    [clojure.data :as d]))

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
(defn ll
  ([] (ll :warn))
  ([level]
   (set! *warn-on-reflection* false)
   ((var-get (resolve 'taoensso.timbre/set-level!)) level)))


(defn clear-ns []
  (doall
   (for [ [s to] (.getMappings *ns*)
         :when (not (class? to))
         :when (not (= 'clojure.core (.-name (.-ns to))))]
     (do  (println "Unmapping" s)
          (.unmap *ns* s)
          s))))


(defn diff
  ([before after] (zipmap [:before :after :same] (d/diff before after)))
  ([before-label after-label before after] (zipmap [before-label after-label] (d/diff before after)))
  ([before-label after-label same-label before after] (zipmap [before-label after-label same-label] (d/diff before after))))


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

(defn ns->ms [nanos]
  (/ nanos 1000000.0))

(defonce instrumented-vars
  (atom #{}))

(defn instrumented? [v]
  (boolean (::instrumented (meta v))))

(defn uninstrument [v]
  (when-let [original (::instrumented (meta v))]
    (do (alter-var-root v (constantly original))
        (swap! instrumented-vars conj v)
        (alter-meta! v dissoc ::instrumented)
        :disabled)))

(defn wrap-instrument [v store f]
  (fn [& args]
    (let [start (System/nanoTime)
          result (apply f args)
          end (System/nanoTime)]
      (println "Time:" v (ns->ms (- end start)))
      (swap! store conj [start end])
      result)))

(defn instrument [v]
  (when-not (::instrumented (meta v))
    (let [original @v
          store (atom {})
          instrumented (wrap-instrument v store original)]
      (do (alter-var-root v (constantly instrumented))
          (swap! instrumented-vars conj v)
          (alter-meta! v assoc
                       ::instrumented original
                       ::store store)
          :enabled))))

(defn summary [p]
  (let [start (ns->ms (apply min (map first p)))
        end   (ns->ms (apply max (map second p)))
        durations (map (fn [[start end]] (- end start)) p)
        sums (apply + durations)]
    {:timespan (- end start)
     :duration-sums (ns->ms sums)
     :mean-duration (ns->ms (/ sums
                               (count durations)))}))

(defn instrument-report [v]
  (if-not (instrumented? v)
    :not-instrumented
    (summary @(::store (meta v)))))

(defn toggle-instrument [v]
  (assert (var? v) "Can only instrument vars")
  (assert (fn? @v) "Can only instrument vars pointing to functions")
  (if (instrumented? v)
    (uninstrument v)
    (instrument v)))

(defn uninstrument-all []
  (doseq [v @instrumented-vars]
    (uninstrument v)))

(defn uninstrument-ns [ns]
  ( 'tracetool)
  (doseq [v @instrumented-vars]
    (uninstrument v)))

(defn instrument-ns [ns]
  (for [v (vals (.getMappings (create-ns ns)))
        :when (var? v)
        :when (=   (.-name (.-ns v)) ns)
        :when (fn? @v)
        ]
    (instrument v)))

(defn uninstrument-ns [ns]
  (for [v (vals (.getMappings (create-ns ns)))
        :when (var? v)
        :when (=   (.-name (.-ns v)) ns)
        :when (fn? @v)
        ]
    (uninstrument v)))
