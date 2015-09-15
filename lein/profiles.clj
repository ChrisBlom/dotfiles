{:profiles

 {:dev {:dependencies []}}

 :cider
 {:plugins
  [[cider/cider-nrepl "0.9.1"] ]
  :dependencies
  [[org.clojure/tools.nrepl "0.2.7"]]
  :jvm-opts ["-Dapple.awt.UIElement=true"] ; hide java icon in OSX dock
  }


 :check
 {:plugins
  [[jonase/kibit "0.0.8"]
   [jonase/eastwood "0.2.1" :exclusions [org.clojure/clojure]]]}


 :power
 {
  :plugins
  [#_[com.gfredericks/nrepl-53-monkeypatch "0.1.0"]
   [cider/cider-nrepl "0.9.1"] ; cider repl integration
   [refactor-nrepl "1.0.5"]
   ;; [lein-kibit "0.0.8" :exclusions [org.clojure/clojure]] ; static code analysis
   ;; [jonase/eastwood "0.2.1" :exclusions [org.clojure/clojure]] ; linter
   ;; [lein-ancient "0.5.5"  :exclusions [org.clojure/clojure]] ; dependency update checker
   ;; [lein-localrepo "0.5.3" :exclusions [org.clojure/clojure]] ; installs artifacts in local maven repo
   ]

  :jvm-opts ["-Dapple.awt.UIElement=true"] ; hide java icon in OSX dock

  :dependencies
  [[org.clojure/tools.nrepl "0.2.7"]
   [org.clojure/tools.namespace "0.2.7"]
   ;[pjstadig/humane-test-output "0.7.0"]
   #_[spyscope "0.1.4"  :exclusions [org.clojure/clojure]] ; tracing tools
   #_[im.chit/iroh "0.1.11"  :exclusions [org.clojure/clojure]] ; java reflection tools
   [im.chit/vinyasa "0.2.2" :exclusions [org.clojure/clojure]] ; workflow tools
   #_[com.gfredericks/debug-repl "0.0.6"]]


  :injections
  [(require '[vinyasa.inject :as inject])

   (load-file (str (System/getProperty "user.home") "/.lein/tracetool.clj"))
                                        ;   (require 'user)

   ;; (require 'pjstadig.humane-test-output)
   ;; (pjstadig.humane-test-output/activate!)

   (inject/in ;; the default injected namespace is `.`
    ;; note that `:refer, :all and :exclude can be used
    [vinyasa.inject :refer [inject [in inject-in]]]
    [tracetool :refer [trace trace-env *trace* defn-trace no-trace]]
    [clojure.pprint :refer [pprint pp]]
    [clojure.repl :refer [dir-fn doc source]]
    [clojure.java.shell :refer [sh]]
    [clojure.data :refer [diff]]
    [vinyasa.pull :all]
    #_[com.gfredericks.debug-repl :refer [break! unbreak! unbreak!!]])

   ]

  :repl-options
  {:init
   (use '[clojure.repl :only (dir-fn doc source)])
   :nrepl-middleware
   [#_com.gfredericks.debug-repl/wrap-debug-repl]}}}
