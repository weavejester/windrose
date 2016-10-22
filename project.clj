(defproject windrose "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]]
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-doo "0.1.6"]
            [lein-figwheel "0.5.8"]]
  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}
  :aliases {"test-cljs" ["with-profile" "test" "doo" "rhino" "test" "once"]
            "test-all"  ["do" ["test" "test-cljs"]]}
  :profiles
  {:dev  {:resource-paths ["dev/resources" "target/figwheel"]
          :source-paths   ["dev/src"]
          :cljsbuild
          {:builds
           {:dev
            {:figwheel     true
             :source-paths ["src" "dev/src"]
             :compiler     {:main          cljs.user
                            :asset-path    "js/out"
                            :output-to     "target/figwheel/public/js/main.js"
                            :output-dir    "target/figwheel/public/js/out"
                            :optimizations :none}}}}
          :figwheel
          {:http-server-root "public"
           :server-port      3001
           :css-dirs         ["resources/public/css"]}}
   :test {:dependencies [[org.mozilla/rhino "1.7.7"]]
          :cljsbuild
          {:builds
           {:test
            {:source-paths ["src" "test"]
             :compiler {:output-to  "target/test/main.js"
                        :output-dir "target/test/"
                        :main windrose.test-runner
                        :optimizations :simple}}}}}})
