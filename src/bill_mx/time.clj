(ns bill-mx.time
  (:require [clj-time.core :as t]
            [clj-time.coerce :as co]
            [clojure.spec.alpha :as s]))

(s/def ::clj-time-coerce-type (s/or :inst inst?
                                    :int integer?
                                    :str string?))

(defn date-equal-or-after?
  "Checks if the dates provided are the same
  or in order from the first being after the second and so on"
  ([date-a date-b]
   (not (t/before? (co/to-local-date date-a)
                   (co/to-local-date date-b))))
  ([local-date-a local-date-b & other-local-dates]
   (->> (concat [local-date-a local-date-b] other-local-dates)
        ;; first brakes the list of local-dates in pairs to be compared
        (partition 2 1)
        ;; check if for each pair the first date is equal or after
        (map #(apply date-equal-or-after? %))
        ;; checks if all validations were tru
        (every? true?))))

(s/fdef date-equal-or-after?
        :args (s/alt :dueary (s/cat :one ::clj-time-coerce-type
                                    :two ::clj-time-coerce-type)
                     :variadic (s/cat :one ::clj-time-coerce-type
                                      :two ::clj-time-coerce-type
                                      :many (s/* ::clj-time-coerce-type)))
        :ret boolean?)

;; TODO: check if these checks in args should be made
;; the function date-equal-or-after uses in its core to-local-date function
;;  from clj-time-coerce which has a very wide range of obj types it accepts
;;  including strings, integers, etc...

(defn date-after?
  "Checks if the date on a local-date-time-a
  is after a local-date-time-b"
  [date-a date-b]
  (t/after? (co/to-local-date date-a)
            (co/to-local-date date-b)))

(s/fdef date-after?
        :args (s/cat :date-a ::clj-time-coerce-type
                     :date-b ::clj-time-coerce-type)
        :ret boolean?)