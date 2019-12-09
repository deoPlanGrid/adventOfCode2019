(ns day5.intcodeComputer02
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [split trim-newline trim]])
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
  [modes pos prog]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))]
    [(assoc prog k
            (+
             (get-prog-val
              (get modes 2) i prog)
             (get-prog-val
              (get modes 1) j prog)))
     (+ pos 4)]))

(defn times
  [modes pos prog]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))]
    [(assoc prog k
            (*
             (get-prog-val
              (get modes 2) i prog)
             (get-prog-val
              (get modes 1) j prog)))
     (+ pos 4)]))

(defn get-input
  [modes pos prog]
  (let [user-input (do (print "Input value: ") (flush) (read-line))
        new-num (Integer/parseInt user-input)
        ind (get-prog-val (get modes 2) (+ pos 1) prog)]
    [(assoc prog ind new-num)
     (+ pos 2)]))

(defn get-output
  [modes pos prog]
  (let [ind (get-prog-val (get modes 2) (+ pos 1) prog)]
    (println (str "Value at index " ind ": " (get prog ind)))
    [prog (+ pos 2)]))

(defn jump-if-true
  [modes pos prog]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get modes 2) i prog)
        ind (get-prog-val (get modes 1) j prog)]
    (if (not= 0 check)
      [prog ind]
      [prog (+ pos 3)])))

(defn jump-if-false
  [modes pos prog]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get modes 2) i prog)
        ind (get-prog-val (get modes 1) j prog)]
    (if (= 0 check)
      [prog ind]
      [prog (+ pos 3)])))

(defn less-than
  [modes pos prog]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get modes 2) i prog)
        n1 (get-prog-val (get modes 1) j prog)
        res (if (< n0 n1) 1 0)]
    [(assoc prog k res) (+ pos 4)]))

(defn equals
  [modes pos prog]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get modes 2) i prog)
        n1 (get-prog-val (get modes 1) j prog)
        res (if (= n0 n1) 1 0)]
    [(assoc prog k res) (+ pos 4)]))

(defn opcode-switch
  [opcode pos prog]
  (let [func (case (last opcode)
               \1 add
               \2 times
               \3 get-input
               \4 get-output
               \5 jump-if-true
               \6 jump-if-false
               \7 less-than
               \8 equals)]
    (func opcode pos prog)))

(defn get-instruction
  [{:keys [prog pos]}]
  (get prog pos))

(defn execute-instruction
  [{:keys [prog pos]}]
  (let [opcode (format-opcode (str (get-instruction {:prog prog :pos pos})))
        [new-prog new-pos] (opcode-switch opcode pos prog)]
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
