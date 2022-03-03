(defproject signably "0.1.0-SNAPSHOT"
  :description "Card signing app based on Ably"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/clojurescript "1.10.914" :scope "provided"]
                 [com.cognitect/transit-cljs "0.8.269"]
                 [io.ably/ably-java "1.2.11"]
                 [reagent "1.1.0"]
                 [reagent-utils "0.3.4"]
                 [cljs-ajax "0.8.4"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]
                 [ring "1.9.5"]
                 [ring/ring-defaults "0.3.3"]
                 [ring-middleware-format "0.7.4"]
                 [ring-server "0.5.0"]
                 [ring-oauth2 "0.2.0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "1.1.9"]
                 [metosin/reitit "0.5.16"]
                 [pez/clerk "1.0.0"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]
                 [com.taoensso/timbre "5.1.2"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]]

  :jvm-opts ["-Xmx1G"]

  :plugins [[lein-environ "1.1.0"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.5.0"
  :uberjar-name "signably.jar"
  :main signably.server

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :omit-source true
  :jar-exclusions [#"cljs-runtime/.*" #"manifest.edn"]

  :profiles {:dev {:repl-options {:init-ns signably.repl}
                   :dependencies [[binaryage/devtools "1.0.4"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.9.5"]
                                  [nrepl "0.9.0"]
                                  [thheller/shadow-cljs "2.16.12"]
                                  [org.clojure/spec.alpha "0.3.218"]
                                  [org.clojure/test.check "1.1.1"]]

                   :source-paths ["env/dev/clj"]

                   :env {:dev true}}

             :shadow-cljs
             {:dependencies
              [[com.google.javascript/closure-compiler-unshaded "v20220104"]]}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
