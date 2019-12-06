(ns day2.intcode
  (:require [clojure.string :refer [split trim-newline join]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn read-intcode-file
  [filename]
  (map #(Integer/parseInt %) (split (trim-newline (slurp filename)) #",")))

(defn run-cmd
  [cmd output]
  (let [[command n1 n2 pos] cmd]
    (case command
      1 (swap! output assoc pos (+ (get @output n1) (get @output n2)))
      2 (swap! output assoc pos (* (get @output n1) (get @output n2)))
      )))

(defn run-intcode-prog
  [output]
  (loop [d 0]
    (let [cmd (take 4 (drop d @output))]
      (when (not= 99 (first cmd))
        (run-cmd cmd output)
        (recur (+ d 4))))))

(defn find-noun-verb
  [intcode]
  (let [pairs (for [n (range 100) v (range 100)] [n v])
        input (atom @intcode)
        res (atom 0)]
    (loop [[noun verb] (first pairs)
           tail (rest pairs)]
      (if (and (not (nil? noun)) (not= 19690720 @res))
        (do
          (reset! input @intcode)
          (swap! input assoc 1 noun)
          (swap! input assoc 2 verb)
          (run-intcode-prog input)
          (reset! res (get @input 0))
          (recur (first tail) (rest tail)))
        [noun verb]))))

(def cli-options
  [[nil "--filename FILENAME" "Path to input file"]])

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        filename (:filename options)
        intcode (read-intcode-file filename)
        output (atom (vec intcode))]
    (do
      (println (find-noun-verb output))
      )))
