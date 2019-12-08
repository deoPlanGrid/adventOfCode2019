(ns day5.intcodeComputer02
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [split trim-newline trim]]
            [clojure.edn :as edn])
  (:gen-class))

(def cli-options
  [[nil "--input-prog INPUTPROG" "Path to input intcode program"]])

(defn read-intcode-prog
  "Reads in a file containing an INTCODE program and coerces the strings to integers."
  [filename]
  (map #(Integer/parseInt (trim %)) (split (trim-newline (slurp filename)) #",")))

(defn format-opcode
  [opcode]
  (let [n (count opcode)]
    (apply str (conj (vec (repeat (- 5 n) "0")) opcode))))

(defn get-prog-val
  [mode ind prog]
  (case mode
    \0 (get prog ind)
    \1 ind))

(defn add
  [modes stream prog]
  (let [[i j k] (take 3 stream)]
    (assoc prog k
           (+
            (get-prog-val
             (get modes 2) i prog)
            (get-prog-val
             (get modes 1) j prog)))))

(defn times
  [modes stream prog]
  (let [[i j k] (take 3 stream)]
    (assoc prog k
           (*
            (get-prog-val
             (get modes 2) i prog)
            (get-prog-val
             (get modes 1) j prog)))))

(defn get-input
  [modes stream prog]
  (let [user-input (do (print "Input value: ") (flush) (read-line))
        new-num (Integer/parseInt user-input)
        ind (first stream)]
    (assoc prog ind new-num)))

(defn get-output
  [modes stream prog]
  (let [ind (first stream)]
    (println (str "Value at index " ind ": " (get prog ind)))
    prog))

(defn opcode-switch
  [opcode tail prog]
  (case (last opcode)
    \1 [(add opcode tail prog) 3]
    \2 [(times opcode tail prog) 3]
    \3 [(get-input opcode tail prog) 1]
    \4 [(get-output opcode tail prog) 1]))

(defn get-instruction
  [{:keys [prog pos]}]
  (get prog pos))

(defn execute-instruction
  [{:keys [prog pos]}]
  (let [opcode (format-opcode (str (get-instruction {:prog prog :pos pos})))
        tail (drop (+ 1 pos) prog)
        [new-prog params] (opcode-switch opcode tail prog)
        new-pos (+ pos 1 params)]
    {:prog new-prog
     :pos new-pos}))

(defn interpret-program
  [intcode-prog]
  (loop [input {:prog (vec intcode-prog)
                :pos 0}]
    (if (and
         (some? (get-instruction input)) (not= 99 (get-instruction input)))
      (recur (execute-instruction input)))))

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        input-program-path (options :input-prog)
        intcode-prog (read-intcode-prog input-program-path)]
    (interpret-program intcode-prog)))
