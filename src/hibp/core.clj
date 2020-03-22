(ns hibp.core
  (:gen-class)
  (:require [clojure.string :as str]
            [digest :refer :all]))

;; DONE: Implement function for reading passwords from file
(defn read-passwords [file]
  (str/split-lines
    (slurp file)))

(def passwords (read-passwords "resources/passwords.txt"))

;; DONE: Hash passwords using SHA-1
;; Create a mapping between a password, it's hash and a shortened hash
(defn do-the-hash [password]
  (let [hashed-password (str/upper-case (digest/sha1 password))
        short-hashed-password (subs hashed-password 0 5)]
    {:password   password
     :hash       hashed-password
     :short-hash short-hashed-password}))

(def hashed-passwords (map do-the-hash passwords))

;; DONE: Make request against api for every password
(def api-url "https://api.pwnedpasswords.com/range/")

(defn fetch-pwned [password]
  (let [short-hash (password :short-hash)
        request-url (str api-url short-hash)]
    (assoc password
      :pwned
      (->> request-url
           (slurp)
           (str/split-lines)
           (map #(str short-hash %))
           (map #(str/split % #":"))
           (into {})))))

;; Do some formatting on the result
(defn pwned? [{hash :hash pwned :pwned password :password}]
  (if-let [result (get pwned hash)]
    (format "%s has been pwned %s times" password result)))

(defn -main [& args]
  ;; Read args as filename
  (def passwords (mapcat read-passwords args))

  ;; Check which passwords have been leaked on the web
  (dorun
    (for [password passwords]
      (if-let [result (->> password
                           (do-the-hash)
                           (fetch-pwned)
                           (pwned?))]
        (println result)))))