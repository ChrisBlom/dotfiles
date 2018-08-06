(ns .)

(defn jsx->clj
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))
