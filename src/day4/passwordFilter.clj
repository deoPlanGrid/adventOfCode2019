(ns day4.passwordFilter
  (:gen-class))

(def password-range (range 235741 (+ 706948 1)))

(defn duped-nums
  [number]
  (not= (count (str number)) (count (set (str number)))))

(defn only-double
  [number digit]
  (and
   (nil? (re-find (re-pattern (apply str (repeat 3 digit))) (str number)))
   (some? (re-find (re-pattern (apply str (repeat 2 digit))) (str number)))))

(defn doubled-nums
  [number]
  (some true? (map #(only-double number %) (range 10))))

(defn digitize-number
  [number]
  (map #(Character/digit % 10) (str number)))

(defn monotonic-increasing
  [number]
  (apply <= (digitize-number number)))

(defn candidate-number
  [number]
  (every? true? (map #(% number) [monotonic-increasing doubled-nums duped-nums])))

(defn -main
  []
  (let [candidates (count (filter candidate-number password-range))]
    (println (str "Number of valid password candidates: " candidates))))
