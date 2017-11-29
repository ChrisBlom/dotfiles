(ns .
  "Dev utils

   Don't include this in production code, keep it in you lein profile, it personal stuff, like a shameful secret.

   To make it available in all clojure projects add this to your leiningen profile:

  :injections
  [(load-file (str (System/getProperty \"user.home\") \"/.lein/..clj\"))
   (require '.)]

  To highlight usages of this namespace in emacs add this to init.el:

  (font-lock-add-keywords 'clojure-mode
                          `((,(concat \"\\\\<\"
                                      (rx \"./\")
                                      \"[a-z0-9-_]+\"
                                      \"\\\\>\")
                             0
                             font-lock-warning-face)))"
  (:require
   [clojure.pprint]
   [clojure.string :as str]
   [clojure.data :as d]
   [clojure.repl]
   [clojure.java.shell :as sh]))

(defmacro doc
  "like clojure.repl/doc, but namespaces take precedence over special docs"
  [name]
  (if-let [special-name ('{& fn catch try finally try} name)]
    (#'clojure.repl/print-doc (#'clojure.repl/special-doc special-name))
    (cond
      (find-ns name) `(#'clojure.repl/print-doc (#'clojure.repl/namespace-doc (find-ns '~name)))
      (#'clojure.repl/special-doc-map name) `(#'clojure.repl/print-doc (#'clojure.repl/special-doc '~name))
      (resolve name) `(#'clojure.repl/print-doc (meta (var ~name))))))

(comment

  (doc .)

  )

;;;;  process

;; TODO split strings like python's shlex
;; TODO don't split :dir, :in-enc, :out-enc, :in arguments
(defn sh [& args]
  (->> args
       (mapcat (fn [x] (if (string? x)
                        (str/split x #" +")
                        x)))
       (apply sh/sh)))

(comment

  (sh "ls -s")

  )

(defn pid
  "Returns the pid of the process"
  []
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
      (.getName)
      (clojure.string/split #"@")
      (first)
      Integer/parseInt))

(defn jvisualvm
  "open jvisualvm profiler for the current process or a provided pid"
  ([] (jvisualvm (pid)))
  ([pid]
   (sh "jvisualvm --openpid" (str pid))))

(comment

  (pid)

  (jvisualvm)

  )

;;;; Printing

(defn pp [x]
  (clojure.pprint/pprint x)
  x)

(defn pp-str [x]
  (let [s (with-out-str (clojure.pprint/pprint x))]
    ;; drop newline that pprint adds
    (subs s 0 (dec (count s)))))

;;;; Tracing

(def ^:dynamic *trace* true)

(defn tt
  "toggle tracing"
  ([] (if (alter-var-root #'*trace* not)
        :enabled-trace-print
        :disabled-trace-print))
  ([enable] (if (alter-var-root #'*trace* (constantly (boolean enable)))
              :enabled-trace-print
              :disabled-trace-print)))

(defmacro t-
  "disables trace within body"
  [& body]
  `(binding [*trace* false]
     ~@body))

(defmacro t+
  "disables trace within body"
  [& body]
  `(binding [*trace* true]
     ~@body))

(defn- pad
  [n string]
  (apply str (take n (concat string (repeat \space)))))

(defmacro loc []
  `(keys ~&env))

(defmacro tr
  "trace, pretty-prints the value of each expression
   returns the value of the last expression"
  [ & vals]
  (let [vals (if (seq vals) vals (keys &env))]
    `(if *trace*
       (do (printf "--Trace ---%n"  )
           (let [vals# (list ~@vals)
                 keys# (quote ~vals)

                 keys-str# (map str keys#)

                 longest-k# (->> keys-str#
                                 (map count)
                                 (apply max))

                 indent# (-> longest-k#
                             (+ 2)
                             (repeat \space)
                             (->> ,,, (apply str)))]
             (doseq [ [k# v#] (map vector keys-str# vals#)]
               (println (#'pad longest-k# k#)
                        "=>"
                        (clojure.string/replace (pp-str v#) "\n" (str "\n" indent# "  "))
                        "::"
                        (type v#)
                        ))
             (println "---------------")
             (last vals#)))
       ~(last vals))))

(comment

  (tr (+ 1 1))

  (t-
   (tr (str "a" "b") (+ 1 1)))

  ;; combine t- & t+ to selectively enable & disable tracing
  (t+
   (dotimes [i 1000]
     (tr i) ; not printed
     (when (zero? (mod i 100))
       (t+
        (let [foo [:i i]]
          (tr foo) ; printed
          )))))

  )

;;;; Logging

(defn ll
  "sets timbre log level to level
   disables reflection warnings for levels below :info"
  ([] (ll :warn))
  ([level]
   (set! *warn-on-reflection*
         (case level
           (:debug :trace) true
           false))
   (if-let [set-level-var (resolve 'taoensso.timbre/set-level!)]
     (set-level-var level)
     {:could-not-resolve 'taoensso.timbre/set-level!})))

(defn timbre? []
  (try (require 'taoensso.timbre)
       true
       (catch Exception e
         false)))

(defmacro ^:private when-timbre [& body]
  (if (try (require 'taoensso.timbre)
           true
           (catch Exception e#
             false))
    `(do ~@body)
    :timbre-not-available))

;; (defmacro maybe-resolve [s]
;;   (some-> sym
;;           resolve
;;           var-get))

;; (defn lf
;;   "log file, sets timbre to log to a file, rotating after 512Kb"
;;   ([] (lf not "application.log"))
;;   ([trg] (lf trg "application.log"))
;;   ([trg logfile]
;;    (when-timbre
;;        (require 'taoensso.timbre.appenders.3rd-party.rotor)
;;      (-> (taoensso.timbre/swap-config! update-in [:appenders :rotor]
;;                                        (fn [a]
;;                                          (let [a (or a ((maybe-resolve taoensso.timbre.appenders.3rd-party.rotor/rotor-appender)))]
;;                                            (cond
;;                                              (fn? trg) (update a :enabled? trg)
;;                                              (keyword? trg) (assoc a :min-level trg)
;;                                              :else
;;                                              (assoc a :enabled? (boolean trg))))))
;;          (get :appenders)
;;          ))))



;; (defn lp
;;   ([] (lp not))
;;   ([trg]
;;    (when-timbre
;;        (-> (taoensso.timbre/swap-config! update-in [:appenders :println]
;;                                          (fn [a]
;;                                            (let [a (or a (get-in taoensso.timbre/example-config [:appenders :println]))]
;;                                              (cond
;;                                                (fn? trg) (update a :enabled? trg)
;;                                                (keyword? trg) (assoc a :min-level trg)
;;                                                :else
;;                                                (assoc a :enabled? (boolean trg))))))
;;            (get :appenders)
;;            ))))

;; (defn lr "log reset: sets timbre config back to default"
;;   []
;;   (taoensso.timbre/set-config! taoensso.timbre/example-config))

;;;; Namespaces

(defn undef-var
  "Takes a symbol.
   If the symbol is namespaced, remove the symbol from its namespace
   else removes it from the current namespace"
  [var]
  (assert (var? var))
  (when (var-get var)
    (ns-unmap (.-name (.-ns var)) (.-sym var))
    [:unmapped var]))

(defmacro nu
  "namespace unmap
   Takes a symbol, if the symbol is namespaced, remove the symbol from its namespace
   else removes it from the current namespace"
  [name]
  (ns-unmap (or (some-> name namespace symbol) *ns*)
            (symbol (clojure.core/name name)))
  `{:unmapped '~name})

(comment

  (def typoasdf 1)

  (nu typoasdf)

  typoasdf ;; => exception, as symbol is no longer defined

  )

(defn nc
  "namespace clear: unmap all entries, except for classes and clojure.core"
  []
  {:cleared
   (vec (for [ [s to] (.getMappings *ns*)
              :when (not (class? to))
              :when (not (= 'clojure.core (.-name (.-ns to))))]
          (do
            (let [v (resolve s)]
              (.unmap *ns* s)
              v))))})

(defn ns->sym [ns]
  (.-name ns))

(defn var->sym [v]
  (symbol (name (ns->sym  (.-ns v)))
          (name (.-sym v))))

(defn load-ns
  ([]
   (load-ns (ns->sym *ns*)))
  ([ns]
   (require ns :reload)))

(defn ns-syms []
  (vec (for [ [s to] (.getMappings *ns*)
             :when (not (class? to))
             :when (not (= 'clojure.core (.-name (.-ns to))))]
         (var->sym (resolve s)))))

(defn sym-name [x]
  (symbol (name x)))

(defn diff
  ([before after] (zipmap [:before :after :same] (d/diff before after)))
  ([before-label after-label before after] (zipmap [before-label after-label] (d/diff before after)))
  ([before-label after-label same-label before after] (zipmap [before-label after-label same-label] (d/diff before after))))

(defn nr
  "namespace reload"
  []
  (let [{:keys [cleared]} (nc)
        removed (set (map (comp sym-name var->sym) cleared))
        ;; TODO filter by *ns*
        f (:file (meta (first cleared))) ;; hack to get the filename
        _ (load-file f)
        loaded (set (map sym-name (ns-syms)))]
    (into
     {:reloaded f}
     (diff :removed :added removed loaded)
     )))

;;;; debug defs

(defn- ->debug-sym [n] (symbol (str "_" (name n))))

(defonce debug-vars (atom #{}))

(defmacro dd
  "debug def"
  ([n]
   `(let [x# ~n]
      (let [g# (def ~(->debug-sym n) x#)]
        (swap! debug-vars conj g#))
      x#))
  ([n expr]
   `(let [x# ~expr]
      (let [g# (def ~(->debug-sym n) x#)]
        (swap! debug-vars conj g#))
      x#)))


(defmacro dd-let
  {:style/indent [1]}
  [bindings & body]
  `(let ~bindings
     ~@(for [v (map first (partition 2 bindings))]
         `(dd ~v))
     ~@body))

(comment
  (dd-let [a 1 b (inc a)]
    (+ a b))

  _a

  _b

  )

(defmacro dd-locals [n]
  (let [locals (keys &env)]
    `(dd ~n (zipmap '~locals (list ~@locals)))))

(comment

  (let [a 1 b (inc a)]
    (dd-locals x)
    (+ a b))

  _x

  )

(defn dd-clear!
  "undefines all debug def vars"
  []
  (let [vs @debug-vars]
    (doseq [v vs]
      (undef-var v)
      (swap! debug-vars disj v))
    [:unmapped vs]))

(comment

  (let [a {:a 1}
        b 2]
    (dd ab (assoc a :b 2))
    (dd t (type a))
    (dd-locals l)
    (dd-let [x "x" y "y" z "z"]
      (str x y z)))

  _ab

  _t

  _l

  (str _x _y _z)

  (dd-clear!)

  _a

  )

(defmacro with-locals [n & body]
  (let [m @(resolve (->debug-sym n))]
    `(let ~(vec (mapcat identity m))
       ~@body)))

(comment

  (let [x 1 y (inc x) z (+ x y)]
    (dd-locals l)
    (+ x y z))

  _l

  (with-locals l
    [x y z])

  ;; Limitation: cannot use locals that cannot be read with read-string
  (let [x (Object.)]
    (dd-locals ERR))

  (with-locals ERR x)

  (let [x (atom 1)]
    (dd-locals ERR_2))

  (with-locals ERR_2 x)

  )

;;;; Timing

(defn ns->ms [nanos]
  (/ nanos 1e6))

(defonce instrumented-vars
  (atom #{}))

(def empty-stats
  {:max Double/NEGATIVE_INFINITY
   :min Double/POSITIVE_INFINITY
   :first nil
   :last nil
   :sum 0
   :count 0})

(defn add-times [store start end now]
  (let [duration (- end start)]
    (swap! store
           (fn [m] (-> (or m empty-stats)
                       (update :max max duration)
                       (update :min min duration)
                       (update :first #(or % now))
                       (assoc :last now)
                       (update :sum + duration)
                       (update :count inc))))))

(defn timed? [var]
  (boolean (::timed (meta var))))

(def ^:dynamic *print-time* true)

(defn wrap-timed [v store f]
  (assert (not= f wrap-timed))
  (fn [& args]
    (let [now (System/currentTimeMillis)
          start (System/nanoTime)
          result (apply f args)
          end (System/nanoTime)]
      (when *print-time* (println "Time:" v (ns->ms (- end start))))
      (add-times store start end now)
      result)))

(defn time! [v]
  (if (or (= v #'time!)
          (= v #'wrap-timed))
    :cannot-time ; prevent infinite loop
    (if (timed? v)
      :already-timed
      (let [original @v
            store (atom empty-stats)
            instrumented (wrap-timed v store original)]
        (do (alter-var-root v (constantly instrumented))
            (swap! instrumented-vars conj v)
            (alter-meta! v assoc
                         ::timed original
                         ::time-store store)
            :enabled-timing )))))

;; TODO add option to also remove store
(defn untime! [v]
  (if-let [original (::timed (meta v))]
    (do (alter-var-root v (constantly original))
        (swap! instrumented-vars disj v)
        (alter-meta! v dissoc ::timed)
        :disabled-timing)
    :was-not-timed))

(defmacro it
  "instrumentation toggle"
  ([sym]
   `(let [v# ~(if (var? sym) sym (resolve sym))]
      (if (timed? v#)
        (untime! v#)
        (time! v#))))
  ([sym bool]
   `(let [v# ~(if (var? sym) sym (resolve sym))]
      (if ~bool
        (time! v#)
        (untime! v#)))))

(defn times-report [v]
  (assert (var? v))
  (if-let [{:keys [max min sum count first last] :as store} (some-> (::time-store (meta v)) deref)]
    (if (zero? count)
      store
      (reduce (fn [acc k] (update acc k ns->ms))
              (cond-> store
                (and first last) (assoc :span (- last first))
                ;; TODO hide equal parts of first & last
                first (update :first #(java.util.Date. (long %)))
                last (update :last #(java.util.Date. (long %)))
                (pos? count) (assoc :mean (/ sum count)))
              [:min :max :sum :mean]))
    :not-timed))

(defmacro ir [sym]
  `(times-report ~(if (var? sym) sym `(var ~sym))))

(comment

  (defn work [] (Thread/sleep (rand-int 40)))

  (it work)

  (dotimes [_ 10] (work))

  (ir work)

  (dotimes [_ 5] (work))

  (it work false)

  (dotimes [_ 1] (work))

  (ir work)

  ;; TODO (it-clear!)
  vinyasa.inject
  )



(defn inject-single [to-ns sym svar]
  (intern to-ns
          (with-meta sym
            (select-keys (meta svar)
                         [:doc :macro :arglists]))
          (deref svar)))



(defonce watched (atom []))

(defn ww*
  "watchable watch (atoms, refs, vars)"
  ([ref label]
   (ww* label ref (fn [_ _ old new]
                    (print label "changed: ")
                    (pp (diff :before :after old new)))))
  ([ref label f]
   (swap! watched conj ref)
   (add-watch ref ::aw f)))

(defmacro ww
  ([ref]
   `(ww* '~ref ~ref))
  ([ref label]
   `(ww* ~label ~ref))
  ([ref label f]
   `(ww* '~label ~ref ~f)))

(defn ww-clear! []
  (let [w @watched]
    (doseq [a w] (remove-watch a ::aw))
    (reset! watched [])
    [:cleared-watches (count w) ] ))

(comment

  (def a (atom 0))

  (ww a)

  (swap! a inc)

  (ww-clear!)

  (swap! a inc)

  )

;; (defn ttns!
;;   ([]
;;    (ttns! (.-name *ns*)))
;;   ([ns]
;;    (doseq [v (vals (.getMappings (create-ns ns)))
;;            :when (var? v)
;;            :when (=   (.-name (.-ns v)) ns)
;;            :when (fn? @v)
;;            :when (not (::dont-instrument (meta v)))]
;;      (println v (time! v)))))

;; (defn untime-ns! [ns]
;;   (doseq [v (vals (.getMappings (create-ns ns)))
;;           :when (var? v)
;;           :when (=   (.-name (.-ns v)) ns)
;;           :when (fn? @v)]
;;     (println v (untime! v))))




(defn pp-class [x]
  (cond (= x (class (byte-array 0)))
        'byte-array
        (= x (class (char-array "")))
        'char-array
        :else
        x))



(defn methods [class & patterns]
  (let [class (if (class? class) class (.getClass class))]
    {class
     (into (sorted-map-by (fn [a b] (compare (pr-str a) (pr-str b))))
           (for [m  (.getMethods class)
                 :let [bm (bean m)
                       exn (:genericExceptionTypes bm)]
                 :when (not (contains? #{"getClass" "notify" "notifyAll" "hashCode" "equals" "toString" "wait"} (:name bm)))
                 :when (every? #(re-find (re-pattern %) (:name bm) ) patterns)
                 ]
             [(cons (symbol (str "." (:name bm)))
                    (map pp-class (:parameterTypes bm)))
              (cond-> [(:genericReturnType bm)]
                (seq exn) (conj (list 'throws exn)))]))}))


(.getMethods java.lang.String)



(defmacro field [x sym]
  `( ~(symbol (str "." sym)) ~x))

(defn private-field [x sym]
  (let [f (.. x getClass (getDeclaredField  (name sym)))]
    (.setAccessible f true)
    (.get f x)))


(defn fields-map [x & fields]
  (into {} (for [f fields]
             [(keyword f)
              (private-field x f)])))
