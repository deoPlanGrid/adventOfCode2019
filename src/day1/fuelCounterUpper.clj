(ns day1.fuelCounterUpper
  (:require [clojure.string :refer [split-lines]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn format-coerce-input
  [input]
  (map
   #(Integer/parseInt %)
   (split-lines
    (slurp input))))

(defn fuel-req
  [number]
  (let [fuel (- (Math/floor (/ number 3)) 2)]
    (if (neg? fuel)
      0
      (int fuel))))

(defn fuel-for-fuel
  [module-weight]
  (loop [weight module-weight
         fuel 0]
    (if (zero? weight)
      (+ fuel module-weight)
      (recur (fuel-req weight) (+ fuel (fuel-req weight))))))

(def weight+fuel (comp fuel-for-fuel fuel-req))

(defn total-fuel-reqs
  [modules]
  (reduce + (map weight+fuel modules)))

(def cli-options
  [[nil "--input INPUT" "Path to input file"]])

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        input (:input options)
        modules (format-coerce-input input)]
    (do
      (println (str "Total fuel required: " (total-fuel-reqs modules))))))
