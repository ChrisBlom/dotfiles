
{:jvm-opts ["-XX:-OmitStackTraceInFastThrow"] ; DONT . OMIT . STACKTRACES!!!

 :profiles
 {:dev
  {:dependencies []}}

 :cider
 {:plugins
  [[cider/cider-nrepl "0.14.0"]]
  :dependencies
  [[org.clojure/tools.nrepl "0.2.12"]]
                                        ; hide java icon in OSX dock
  :jvm-opts ["-Dapple.awt.UIElement=true"]}

 :check
 {:plugins
  [[jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]
   [lein-kibit "0.0.8" :exclusions [org.clojure/clojure]] ; static code analysis
   [lein-cljfmt "0.5.3"]
   [lein-ancient "0.6.10"  :exclusions [org.clojure/clojure]] ; dependency update checker
   [lein-cljfmt "0.5.3"]]}

 :power
 {:plugins
  [[cider/cider-nrepl "0.14.0"] ; cider repl integration
   ;[com.billpiel/sayid "0.0.14" :exclusions [org.clojure/tools.namespace]]
   [refactor-nrepl "2.3.0-SNAPSHOT"]]

  :jvm-opts ["-Dapple.awt.UIElement=true"  ; hide java icon in OSX dock
             "-XX:-OmitStackTraceInFastThrow"  ; DONT . OMIT . STACKTRACES!!!
             ]

  :dependencies
  [[org.clojure/tools.nrepl "0.2.12"]
   ;; [acyclic/squiggly-clojure "0.1.6"] ; linters for emacs
   ;;[org.clojure/tools.namespace "0.2.10"]
   ]

  :injections
  [(load-file (str (System/getProperty "user.home") "/.lein/..clj"))
   (require '.)]

  :repl-options
  {:timeout 120000
   :init (use '[clojure.repl :only (dir-fn doc source)])}}}
