(defproject hibp "0.1.0-SNAPSHOT"
  :description "Simple application for checking if your passwords have made their way onto the internet"
  :url "https://github.com/MarkusPettersson98/HaveIBeenPwned-Clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [digest "1.4.9"]
                 [cheshire "5.10.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [javax.servlet/servlet-api "2.5"]]
  :repl-options {:init-ns hibp.core}
  :main hibp.core
  :aot [hibp.core])
