(ns day5.intcodeComputer02
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [split trim-newline]])
  (:gen-class))

(def cli-options
  [[nil "--input-prog" "Path to input intcode program"]])

(defn read-intcode-prog
  "Reads in a file containing an INTCODE program and coerces the strings to integers."
  [filename]
  (map #(Integer/parseInt %) (split (trim-newline (slurp filename)) #",")))

(defn format-opcode
  [opcode]
  (let [n (count opcode)]
    (conj (repeat (- 5 n) "0") opcode)))

(format-opcode "1")

(defn execute-instruction
  [instr tail prog]
  (let [opcode (str instr)
        code (last opcode)]
    (case code
      "1" nil
      "2" nil
      "3" nil
      "4" nil)))

(defn intepret-program
  [intcode-prog]
  (loop [inputs {:prog (vec intcode-prog)
                 :instruction (first intcode-prog)
                 :tail (rest intcode-prog)}]
    (if (some? (:instruction inputs))
      (recur (execute-instruction instruction tail prog)))))

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)]
    ))
