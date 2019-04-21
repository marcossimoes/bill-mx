(ns bill-mx.numbers)

(defn not-neg?
  "Checks that all values are non neg, i.e zero or positive"
  [& values]
  (every? true? (map #(not (neg? %)) values)))