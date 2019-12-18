(ns day9.sensorBoost
  (:require [intcodeComputer.core :as icc]
            [utils.edn-utils :refer [read-edn-data]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [[nil "--input INPUT" "Path to input edn file."]])

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        {:keys [data input]} (read-edn-data (options :input))]
    (icc/interpret-program data input)))
