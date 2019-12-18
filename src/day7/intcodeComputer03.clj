(ns day7.intcodeComputer03
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [join]]
            [clojure.math.combinatorics :refer [permutations]])
  (:gen-class))

(def cli-options
  [[nil "--input-prog INPUTPROG" "Path to input intcode program"]
   [nil "--feedback-mode" "Run program in feedback mode"]])

(defn format-opcode
  [opcode]
  (let [n (count opcode)]
    (apply str (conj (vec (repeat (- 5 n) "0")) opcode))))

(defn get-prog-val
  [mode ind prog]
  (case mode
    \0 (get prog ind)
    \1 ind))

(defn multi-hm-assoc
  [val coll]
  (reduce #(assoc %1 (first %2) (second %2)) val coll))

(defn add
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k
                   (+'
                    (get-prog-val
                     (get opcode 2) i prog)
                    (get-prog-val
                     (get opcode 1) j prog)))
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn times
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k
                   (*'
                    (get-prog-val
                     (get opcode 2) i prog)
                    (get-prog-val
                     (get opcode 1) j prog)))
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn get-input
  [{:keys [opcode pos prog uinput mode iters] :as dict}]
  (let [new-num (uinput mode)
        new-mode (if (= mode :phase) :signal mode)
        ind (get-prog-val (get opcode 2) (+ pos 1) prog)
        new-uinput {:phase (uinput :phase)
                    :signal (if (and (some? (uinput :signal)) (= mode :signal))
                              nil
                              (uinput :signal))}]
    (multi-hm-assoc
     dict
     {:prog (assoc prog ind new-num)
      :pos (+ pos 2)
      :mode new-mode
      :uinput new-uinput
      :iters (inc iters)})))

(defn get-output
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [ind (get-prog-val (get opcode 2) (+ pos 1) prog)]
    (multi-hm-assoc
     dict
     {:pos (+ pos 2)
      :output (get prog ind)
      :iters (inc iters)})))

(defn jump-if-true
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get opcode 2) i prog)
        ind (get-prog-val (get opcode 1) j prog)]
    (if (not= 0 check)
      (multi-hm-assoc
       dict
       {:pos ind
        :iters (inc iters)})
      (multi-hm-assoc
       dict
       {:pos (+ pos 3)
        :iters (inc iters)}))))

(defn jump-if-false
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get opcode 2) i prog)
        ind (get-prog-val (get opcode 1) j prog)]
    (if (= 0 check)
      (multi-hm-assoc
       dict
       {:pos ind
        :iters (inc iters)})
      (multi-hm-assoc
       dict
       {:pos (+ pos 3)
        :iters (inc iters)}))))

(defn less-than
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get opcode 2) i prog)
        n1 (get-prog-val (get opcode 1) j prog)
        res (if (< n0 n1) 1 0)]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k res)
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn equals
  [{:keys [opcode pos prog uinput iters] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get opcode 2) i prog)
        n1 (get-prog-val (get opcode 1) j prog)
        res (if (= n0 n1) 1 0)]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k res)
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn opcode-switch
  [state]
  (let [func (case (last (state :opcode))
               \1 add
               \2 times
               \3 get-input
               \4 get-output
               \5 jump-if-true
               \6 jump-if-false
               \7 less-than
               \8 equals)]
    (func state)))

(defn get-instruction
  [{:keys [prog pos]}]
  (get prog pos))

(defn generate-init-state
  [intcode-program phases]
  (vec
   (for [[phase signal] (map
                         vector
                         phases
                         (conj (repeat (- (count phases) 1) nil) 0))]
     {:prog intcode-program
      :pos 0
      :uinput {:phase phase :signal signal}
      :output nil
      :mode :phase
      :iters 0})))

(defn execute-instruction
  [state]
  (let [opcode (format-opcode (str (get-instruction state)))]
    (opcode-switch (assoc state :opcode opcode))))

(defn needs-signal
  [state]
  (and (= 3 (get-instruction state)) (nil? (get-in state [:uinput :signal]))))

(defn process-forward
  [input]
  (loop [state input]
    (if (and (not= 99 (get-instruction state)) (not (needs-signal state)))
      (recur (execute-instruction state))
      state)))

(defn interpret-program
  [intcode-prog uinput]
  (loop [input {:prog (vec intcode-prog)
                :pos 0
                :uinput uinput
                :output nil
                :mode :phase
                :iters 0}]
    (if (and
         (some? (get-instruction input)) (not= 99 (get-instruction input)))
      (recur (execute-instruction input))
      input)))

(defn send-signal-forward
  [states]
  (let [num-machines (count states)]
    (loop [n 0
           machine-states states]
      (if (< n num-machines)
        (recur
         (+ 1 n)
         (assoc-in
          (assoc-in
           machine-states
           [(mod (+ 1 n) num-machines) :uinput :signal]
           (get-in machine-states [n :output]))
          [n :output]
          nil))
        machine-states))))

(defn feed-forward
  [machine-states]
  (vec
   (send-signal-forward
    (vec
     (map process-forward machine-states)))))

(defn feedback-amplifier
  [intcode-program phases]
  (let [num-machines (count phases)
        initial-machine-states (generate-init-state intcode-program phases)]
    (loop [machine-states initial-machine-states]
      (if (not= 99 (get-instruction (last machine-states)))
        (recur (feed-forward machine-states))
        machine-states))))

(defn feedback-thruster-output
  [intcode-prog phases]
  (->> phases
       (feedback-amplifier intcode-prog)
       (first)
       (:uinput)
       (:signal)))

(defn run-amplifier
  [phases program signal]
  (loop [phases phases
         input {:prog (vec program)
                :pos 0
                :command (get-instruction {:prog (vec program) :pos 0})
                :uinput {:phase (first phases)
                         :signal signal}
                :output nil}]
    (println input)
    (if (empty? phases)
      input
      (let [{:keys [prog output pos uinput] :as result} (interpret-program
                                                         (input :prog)
                                                         (input :uinput))
            new-input {:prog prog
                       :pos 0
                       :command (get-instruction result)
                       :uinput {:phase (first (rest phases))
                                :signal output}
                       :output output}]
        (recur (rest phases) new-input)))))

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        feedback-mode (options :feedback-mode)
        input-program-path (options :input-prog)
        {:keys [data] :as data-file} (read-edn-data input-program-path)
        intcode-prog data]
    (if (nil? feedback-mode)
      (println "Maximum thruster signal: "
               (apply max
                      (map
                       #((run-amplifier % intcode-prog 0) :output)
                       (permutations (range 5)))))
      (println "Maximum feedback signal: "
       (apply max
              (map
               #(feedback-thruster-output intcode-prog %)
               (permutations (range 5 10))))))))
