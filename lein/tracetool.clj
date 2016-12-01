(ns tracetool
  (:require
   [clojure.pprint]
   [clojure.string :as str]
   [clojure.data :as d]
   [clojure.java.shell :as sh]))

(defmacro show-env [] (println &env))

(def pid
  "Get current process PID"
  (memoize
   (fn []
     (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
         (.getName)
         (clojure.string/split #"@")
         (first)))))

(defn open-profiler
  "open a profiler for the current process or a provided pid"
  ([] (open-profiler (pid)))
  ([pid]
   (sh/sh "jvisualvm" "--openpid" (str pid))))

(comment

  (pid)

  (open-profiler)

  )

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

(comment

  (trace (+ 1 1))
  )

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

(defn ll
  ([] (ll :warn))
  ([level]
   (set! *warn-on-reflection* false)
   ((var-get (resolve 'taoensso.timbre/set-level!)) level)))

(defmacro undef [name]
  (ns-unmap (or (some-> name namespace symbol) *ns*)
            name))

(defn clear-ns
  "unmap all entries, except for classes and clojure.core"
  []
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

;;;;

(defn ns->ms [nanos]
  (/ nanos 1000000.0))

(defonce instrumented-vars
  (atom #{}))

(defn timed? [v]
  (boolean (::timed (meta v))))

(defn untime! [v]
  (when-let [original (::timed (meta v))]
    (do (alter-var-root v (constantly original))
        (swap! instrumented-vars conj v)
        (alter-meta! v dissoc ::timed)
        :disabled)))

(defn wrap-timed [v store f]
  (fn [& args]
    (let [start (System/nanoTime)
          result (apply f args)
          end (System/nanoTime)]
      (println "Time:" v (ns->ms (- end start)))
      (swap! store conj [start end])
      result)))

(defn time! [v]
  (when-not (::timed (meta v))
    (let [original @v
          store (atom {})
          instrumented (wrap-timed v store original)]
      (do (alter-var-root v (constantly instrumented))
          (swap! instrumented-vars conj v)
          (alter-meta! v assoc
                       ::timed original
                       ::time-store store)
          [:enabled-timing v]))))

;; TODO percentiles
(defn summary [p]
  (let [start (ns->ms (apply min (map first p)))
        end   (ns->ms (apply max (map second p)))
        durations (map (fn [[start end]] (- end start)) p)
        sums (apply + durations)]
    {:timespan (- end start)
     :min (ns->ms (apply min durations))
     :max (ns->ms (apply max durations))
     :duration-sums (ns->ms sums)
     :mean-duration (ns->ms (/ sums (float (count durations))))}))

(defn times [v]
  (if-not (timed? v)
    :not-timed
    (summary @(::time-store (meta v)))))

(defn toggle-timed! [v]
  (assert (var? v) "Can only instrument vars")
  (assert (fn? @v) "Can only instrument vars pointing to functions")
  (if (timed? v)
    (untime! v)
    (time! v)))

(defn untime-all []
  (doseq [v @instrumented-vars]
    (untime! v)))

(defn clear-times []
  (doseq [v @instrumented-vars]
    (reset! (::time-store (meta v)) {})))

(defn time-ns! [ns]
  (for [v (vals (.getMappings (create-ns ns)))
        :when (var? v)
        :when (=   (.-name (.-ns v)) ns)
        :when (fn? @v)
        ]
    (time! v)))

(defn untime-ns [ns]
  (for [v (vals (.getMappings (create-ns ns)))
        :when (var? v)
        :when (=   (.-name (.-ns v)) ns)
        :when (fn? @v)
        ]
    (untime! v)))
