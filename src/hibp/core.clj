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

(defn fetch-pwned [password]
  (let [short-hash (password :short-hash)
        request-url (str api-url short-hash)]
    (assoc password
      :pwned
      (into {}
            (map #(str/split % #":")
                 (map #(str short-hash %)
                      (read-lines request-url)))))))

;; Do some formatting on the result
(defn pwned? [{hash :hash pwned :pwned password :password}]
  (if-let [result (get pwned hash)]
    (format "%s has been pwned %s times" password result)))

(defn -main [& args]
  ;; Read args as filename
  (def passwords (mapcat read-lines args))

  ;; Check which passwords have been leaked on the web
  (dorun
    (for [password passwords]
      (if-let [pwned-password (pwned? (fetch-pwned (do-the-hash password)))]
        (println pwned-password)))))