{:profiles

 {:dev {:dependencies []}}

 :cider
 {:plugins
  [[cider/cider-nrepl "0.8.2"]  ]
  :dependencies
  [[org.clojure/tools.nrepl "0.2.6"]]
  :jvm-opts ["-Dapple.awt.UIElement=true"] ; hide java icon in OSX dock
  }

 :power
 {
  :plugins
  [[lein-midje "3.1.3"] ; better testing
   [cider/cider-nrepl "0.8.2"] ; cider repl integration
 ;  [refactor-nrepl "0.1.0"] ; clj-refactor integration
   [lein-ns-dep-graph "0.1.0-SNAPSHOT" :exclusions [org.clojure/clojure]] ; namespace dependency graphs
   [lein-kibit "0.0.8" :exclusions [org.clojure/clojure]] ; static code analysis
   [jonase/eastwood "0.1.4" :exclusions [org.clojure/clojure]] ; linter
   [lein-drip "0.1.1-SNAPSHOT"] ; jvm launch accelarator
   [lein-ancient "0.5.5"  :exclusions [org.clojure/clojure]] ; dependency update checker
   [lein-localrepo "0.5.3"] ; installs artifacts in local maven repo
   ]

  :jvm-opts ["-Dapple.awt.UIElement=true"] ; hide java icon in OSX dock

  :dependencies
  [[org.clojure/tools.nrepl "0.2.6"]
   [org.clojure/tools.namespace "0.2.7"]
   [aprint "0.1.0" :exclusions [org.clojure/clojure]]
   #_[leiningen #=(leiningen.core.main/leiningen-version)  :exclusions [org.clojure/clojure]]
   [spyscope "0.1.4"  :exclusions [org.clojure/clojure]] ; tracing tools
   [io.aviso/pretty "0.1.12"] ; better pretty printing
   [im.chit/iroh "0.1.11"  :exclusions [org.clojure/clojure]] ; java reflection tools
   [im.chit/vinyasa "0.2.2" :exclusions [org.clojure/clojure]] ; workflow tools
   [slamhound "1.5.5"] ; namespace imports
   [org.clojars.gjahad/debug-repl "0.3.3"] ; debug repl
   ]

  :injections
  [(require 'spyscope.core)
   (require '[vinyasa.inject :as inject])
   (require 'io.aviso.repl)
   (require 'alex-and-georges.debug-repl)
   (load-file (str (System/getProperty "user.home") "/.lein/user.clj"))
   (require 'user)

   (inject/in ;; the default injected namespace is `.`

    ;; note that `:refer, :all and :exclude can be used
    [vinyasa.inject :refer [inject [in inject-in]]]
    #_[vinyasa.lein :exclude [*project*]]

    [user :refer [trace trace-env]]

    [alex-and-georges.debug-repl :refer [debug-repl exit-dr quit-dr view-locals local-bindings eval-with-locals]]

    ;; imports all functions in vinyasa.pull
    [vinyasa.pull :all]


    [cemerick.pomegranate add-classpath get-classpath resources]

    [aprint.core :refer [aprint]]

    ;; inject into clojure.core
    clojure.core
    [iroh.core .> .? .* .% .%>]

    ;; inject into clojure.core with prefix
    clojure.core >
    [clojure.pprint pprint]
    [clojure.pprint pp]
    [clojure.java.shell sh])

   ;; clearer exceptions/stack-traces

   #_(alter-var-root #'clojure.main/repl-caught
                     (constantly @#'io.aviso.repl/pretty-pst))
   #_(io.aviso.repl/install-pretty-exceptions)
   ]

  :repl-options
  {#_:nrepl-middleware #_[ io.aviso.nrepl/pretty-middleware]
   :init
   (do
     (use '[clojure.repl :only (dir-fn doc source)]))}}

}
