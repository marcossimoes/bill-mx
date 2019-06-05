(ns bill-mx.numbers
  (:require [clojure.spec.alpha :as s]))

(defn not-neg?
  "Checks that all values are non neg, i.e zero or positive"
  [& values]
  (every? true? (map #(not (neg? %)) values)))

(s/fdef not-neg? :args number? :res boolean?)

(defn not-pos?
  "Checks that all values are non pos, i.e zero or negative"
  [& values]
  (every? true? (map #(not (pos? %)) values)))

(s/fdef not-pos? :args number? :res boolean?)