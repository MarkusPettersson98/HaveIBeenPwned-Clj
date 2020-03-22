(ns hibp.core
  (:gen-class)
  (:require [clojure.string :as str]
            [digest :refer :all]
            [cheshire.core :as json]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]))

;; DONE: Implement function for reading passwords from file
(defn read-lines [resource]
  (str/split-lines
    (slurp resource)))

;; DONE: Hash passwords using SHA-1
;; Create a mapping between a password, it's hash and a shortened hash
(defn do-the-hash [password]
  (let [hashed-password (str/upper-case (digest/sha1 password))
        shortened-hash (subs hashed-password 0 5)]
    {:password   password
     :hash       hashed-password
     :short-hash shortened-hash}))

;; DONE: Make request against api for every password
(def api-url "https://api.pwnedpasswords.com/range/")

(defn fetch-pwned-stats [{hash :hash short-hash :short-hash :as password}]
  (let [request-url (str api-url short-hash)]
    (assoc password
      :pwned
      (get (->> request-url
                (read-lines)
                (map (comp (fn [hash-n-count]
                             (str/split hash-n-count #":"))
                           (partial str short-hash)))
                (into {}))
           hash))))


(def hashed-passwords (map do-the-hash (read-lines "resources/passwords.txt")))

(json/encode
  (fetch-pwned-stats (second hashed-passwords)))

;; Do some formatting on the result
(defn pwned? [{occurrences :pwned password :password}]
  (if (some? occurrences)
    (format "%s has been pwned %s times" password occurrences)
    (format "%s has not been pwned!" password)))

(defn json-pwned-stats [& passwords]
  (json/encode
    ;; Flattening passwords allows for treating a single string and a list of strings the same way
    (map #(fetch-pwned-stats (do-the-hash %)) (flatten passwords))))

(defn check-password [req]
  (let [passwords (:pass (:params req))]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (json-pwned-stats passwords)}))

;; HTTP web server stuff
(defroutes app-routes
           (GET "/check" [] check-password)
           (route/not-found "Error, page not found!"))

(defn -main [& args]
  ;; Start web server
  ; Run the server with Ring.defaults middleware
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))