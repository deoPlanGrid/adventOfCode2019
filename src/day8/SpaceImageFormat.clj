(ns day8.SpaceImageFormat
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:gen-class))

(def cli-options
  [[nil "--input INPUT" "Path to input file."]
   [nil "--width WIDTH" "Width of image"]
   [nil "--height HEIGHT" "Height of image"]])

(defn read-image
  [filename width height]
  (partition (* width height) (slurp filename)))

(defn print-layer
  [layer width]
  (println
   (string/join \newline
                (map #(string/join %)
                     (partition width layer)))))

(defn count-digit
  [layer digit]
  (count (filter #(= digit %) layer)))

(defn comp-layers
  [layer0 layer1]
  (let [n0zeroes (count-digit layer0 \0)
        n1zeroes (count-digit layer1 \0)]
    (if (> n0zeroes n1zeroes)
      layer1
      layer0)))

(defn find-min0-layer
  [layers]
  (reduce comp-layers layers))

(defn =2
  [v]
  (= v \2))

(defn get-top-color
  [layers]
  (map #(first (drop-while =2 %)) (apply map list layers)))

(defn color-image
  [image]
  (replace {\0 "." \1 "W" \2 " "} image))

(defn -main
  [& args]
  (let [{:keys [options]} (parse-opts args cli-options)
        width (Integer/parseInt (options :width))
        height (Integer/parseInt (options :height))
        image (read-image (options :input) width height)
        min0-layer (find-min0-layer image)
        res (* (count-digit min0-layer \1) (count-digit min0-layer \2))]
    (print-layer (color-image (get-top-color image)) width)))
