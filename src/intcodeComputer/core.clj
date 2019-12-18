(ns intcodeComputer.core
  (:require [clojure.string :refer [join]])
  (:gen-class))

(defn format-opcode
  [opcode]
  (let [n (count opcode)]
    (apply str (conj (vec (repeat (- 5 n) "0")) opcode))))

(defn get-prog-val
  [mode ind prog rel-base]
  (let [res (case mode
              \0 (get prog ind)
              \1 ind
              \2 (get prog (+ rel-base ind)))]
    (comment
      (println "Mode: " mode
               " | Value: " ind
               " | RB-Adjusted: " (+ rel-base ind)
               " | Result: " res))
    res))

(defn multi-hm-assoc
  [val coll]
  (reduce #(assoc %1 (first %2) (second %2)) val coll))

(defn add
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        k (if (= \0 (get opcode 0)) k (+ rel-base k))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k
                   (+'
                    (get-prog-val
                     (get opcode 2) i prog rel-base)
                    (get-prog-val
                     (get opcode 1) j prog rel-base)))
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn times
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        k (if (= \0 (get opcode 0)) k (+ rel-base k))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k
                   (*'
                    (get-prog-val
                     (get opcode 2) i prog rel-base)
                    (get-prog-val
                     (get opcode 1) j prog rel-base)))
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn get-input
  [{:keys [opcode pos prog uinput mode iters rel-base] :as dict}]
  (let [new-num (uinput mode)
        new-mode (if (= mode :phase) :signal mode)
        value (get prog (+ pos 1))
        ind (if (= \0 (get opcode 2)) value (+ rel-base value))
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

(defn output-value
  [mode ind prog rel-base]
  (case mode
    \0 ))

(defn get-output
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [mode (get opcode 2)
        value (get prog (+ pos 1))
        output (get-prog-val mode value prog rel-base)]
    (println "Value : " output)
    (multi-hm-assoc
     dict
     {:pos (+ pos 2)
      :output output
      :iters (inc iters)})))

(defn jump-if-true
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get opcode 2) i prog rel-base)
        ind (get-prog-val (get opcode 1) j prog rel-base)]
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
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j] (subvec prog (+ 1 pos) (+ 1 pos 2))
        check (get-prog-val (get opcode 2) i prog rel-base)
        ind (get-prog-val (get opcode 1) j prog rel-base)]
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
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get opcode 2) i prog rel-base)
        n1 (get-prog-val (get opcode 1) j prog rel-base)
        res (if (< n0 n1) 1 0)
        k (if (= \0 (get opcode 0)) k (+ rel-base k))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k res)
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn equals
  [{:keys [opcode pos prog uinput iters rel-base] :as dict}]
  (let [[i j k] (subvec prog (+ 1 pos) (+ 1 pos 3))
        n0 (get-prog-val (get opcode 2) i prog rel-base)
        n1 (get-prog-val (get opcode 1) j prog rel-base)
        res (if (= n0 n1) 1 0)
        k (if (= \0 (get opcode 0)) k (+ rel-base k))]
    (multi-hm-assoc
     dict
     {:prog (assoc prog k res)
      :pos (+ pos 4)
      :iters (inc iters)})))

(defn adjust-relative-base
  [{:keys [opcode pos prog iters rel-base] :as dict}]
  (let [ind (get-prog-val (get opcode 2) (get prog (+ pos 1)) prog rel-base)]
    (multi-hm-assoc
     dict
     {:rel-base (+ rel-base ind)
      :pos (+ pos 2)
      :iters (inc iters)})))

(defn functions
  [opcode]
  (case (last opcode)
    \1 add
    \2 times
    \3 get-input
    \4 get-output
    \5 jump-if-true
    \6 jump-if-false
    \7 less-than
    \8 equals
    \9 adjust-relative-base))

(defn get-mem-indices
  [opcode slice prog-memory rel-base prog]
  (let [pairs (map vector (reverse (take 3 opcode)) slice)
        inds (filter #(not= \1 (get % 0)) pairs)
        zeros (map #(second %) (filter #(= \0 (first %)) inds))
        twos (map #(+ rel-base (second %)) (filter #(= \2 (first %)) inds))
        indices (concat zeros twos)]
    (comment
      (do
        (println "Zeros: " zeros)
        (println "Twos: " twos)))
    (if (empty? indices)
      0
      (let [highest (apply max indices)]
        (if (> highest prog-memory)
          (+ 1 (- highest prog-memory))
          0)))))

(defn mem-locations
  [prog-memory state]
  (let [{:keys [opcode pos prog rel-base]} state
        params (case (last opcode)
                 \1 3
                 \2 3
                 \3 1
                 \4 1
                 \5 2
                 \6 2
                 \7 3
                 \8 3
                 \9 1)]
    (comment
      (println "Memory size: " prog-memory
               " | Instruction chunk: " (subvec prog pos (+ 1 pos params))
               " | Relative base: " rel-base))
    (get-mem-indices opcode (subvec prog (+ 1 pos) (+ 1 pos params)) prog-memory rel-base prog)))

(defn opcode-switch
  [state]
  (let [opcode (state :opcode)
        prog-memory (count (state :prog))
        expand-by (mem-locations prog-memory state)
        func (functions opcode)]
    (if (> expand-by 0)
        (func (assoc state :prog (vec (concat (state :prog) (repeat expand-by 0)))))
      (func state))))

(defn get-instruction
  [{:keys [prog pos]}]
  (get prog pos))

(defn init-state
  [intcode-program phase signal]
  {:prog intcode-program
   :pos 0
   :opcode (format-opcode (str (first intcode-program)))
   :uinput {:phase phase :signal signal}
   :output nil
   :mode :phase
   :iters 0
   :rel-base 0})

(defn generate-init-states
  [intcode-program phases]
  (vec
   (for [[phase signal] (map
                         vector
                         phases
                         (conj (repeat (- (count phases) 1) nil) 0))]
     (init-state intcode-program phase signal))))

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
  (loop [input (init-state (vec intcode-prog) (uinput :phase) (uinput :signal))]
    (comment
      (println input))
    (if (and
         (some? (get-instruction input)) (not= 99 (get-instruction input)))
      (recur (execute-instruction input))
      input)))

(def diagnostics
  [{:data [109, -1, 4, 1, 99] :output -1}
   {:data [109, -1, 104, 1, 99] :output 1}
   {:data [109, -1, 204, 1, 99] :output 109}
   {:data [109, 1, 9, 2, 204, -6, 99] :output 204}
   {:data [109, 1, 109, 9, 204, -6, 99] :output 204}
   {:data [109, 1, 209, -1, 204, -106, 99] :output 204}
   {:data [109, 1, 3, 3, 204, 2, 99] :output 42}
   {:data [109, 1, 203, 2, 204, 2, 99] :output 42}
   {:data [3,9,7,9,10,9,4,9,99,-1,8] :output 0}])
