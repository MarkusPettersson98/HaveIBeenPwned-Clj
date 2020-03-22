(ns hibp.core
  (:gen-class)
  (:require [clojure.string :as str]
            [digest :refer :all]))

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

;; Do some formatting on the result
(defn pwned? [{occurrences :pwned password :password}]
  (if (some? occurrences)
    (format "%s has been pwned %s times" password occurrences)
    (format "%s has not been pwned!" password)))

(defn -main [& args]
  ;; Read args as filename
  (let [passwords (mapcat read-lines args)]
    ;; Check which passwords have been leaked on the web
    (dorun
      (for [password passwords]
        (let [pwned-password (pwned? (fetch-pwned-stats (do-the-hash password)))]
          (println pwned-password))))))