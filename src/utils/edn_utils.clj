(ns utils.edn-utils
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:gen-class))

(defn read-edn-data
  [path]
  (try
    (with-open [r (io/reader path)]
      (edn/read (java.io.PushbackReader. r)))))
