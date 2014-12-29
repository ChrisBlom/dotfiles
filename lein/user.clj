(ns user
  (:require clojure.pprint))

(defmacro show-env [] (println &env))

(defn ppr
  ([title x]
     (println title)
     (clojure.pprint/pprint x))
  ([x] (clojure.pprint/pprint x))
  )

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

(defmacro spy [x]
  `(when *trace*
     (println "spy:" (with-out-str (ppr ~x))))
  ~x)

(defn transpose
  "transposes a vector of vectors"
  [rc]
  (apply mapv vector rc))

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

(comment
  (print-table
   [ [:a "bar" "baz"]
     [:foo 2 "1"]
     ]
   [" " " = " " :: "])


  (print-table
   [  [:a  (assoc-in {} "asdadsdasdasdadsasda" 1) "b"]]
   (repeat " | ")
   ))

(defmacro with-e [ t & body]
  `(binding [*trace*  t]
     ~@body))

(defmacro trace [ & vals]
  `(do
     (when *trace*
       (printf "--Trace ---%n"  )
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
                     (apply str (butlast (with-out-str (ppr v#)))) ; trim last newline
                     #"\n"
                     (str "\n" indent#))
                    " :: "
                    (type v#)
                    )))
       (println "---------------"))))

(defmacro trace-env [ & vals]
  (let [vals (concat (or vals '()) (keys &env))]
    `(do
       (when *trace*
         (printf "--Trace ---%n"  )
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
                       (apply str (butlast (with-out-str (ppr v#)))) ; trim last newline
                       #"\n"
                       (str "\n" indent#))
                      " :: "
                      (type v#)
                      )))
         (println "---------------")))))

(defmacro undef [name]
  (ns-unmap (or (some-> name namespace symbol) *ns*)
            name))


(defmacro add-dep
  [ [lib release] ]
  (cemerick.pomegranate/add-dependencies
   :coordinates [[lib release]]
   :repositories {"clojars" "http://clojars.org/repo"
                  "central" "http://repo1.maven.org/maven2/"}))
