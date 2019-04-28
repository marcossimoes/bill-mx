(ns bill-mx.time
  (:require [clj-time.core :as t]
            [clj-time.coerce :as co]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [bill-mx.macros :as bm]))

(s/def ::clj-time-coerce-type (s/or :inst inst?
                                    :int integer?
                                    :str string?))

(defn date-equal-or-after?
  "Checks if the dates provided are the same
  or in order from the first being after the second and so on"
  ([date-a date-b]
   (bm/when-let* [formated-date-a (co/to-local-date date-a)
                  formated-date-b (co/to-local-date date-b)]
     (not (t/before? formated-date-a
                     formated-date-b))))
  ([date-a date-b & other-dates]
   (->> (concat [date-a date-b] other-dates)
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
        :ret (s/or :boolean boolean? :nil nil?))

(defn date-after?
  "Checks if the date on a local-date-time-a
  is after a local-date-time-b"
  [date-a date-b]
  (bm/when-let* [formated-date-a (co/to-local-date date-a)
                 formated-date-b (co/to-local-date date-b)]
                (t/after? formated-date-a
                          formated-date-b)))

(s/fdef date-after?
        :args (s/cat :date-a ::clj-time-coerce-type
                     :date-b ::clj-time-coerce-type)
        :ret (s/or :boolean boolean? :nil nil?))