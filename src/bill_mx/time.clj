(ns bill-mx.time
  (:require [clj-time.core :as t]
            [clj-time.coerce :as co]
            [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.macros :as bm]
            [bill-mx.numbers :as n]))



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
        :args (s/alt :dueary (s/cat :one ::g/clj-time-coerce-type
                                    :two ::g/clj-time-coerce-type)
                     :variadic (s/cat :one ::g/clj-time-coerce-type
                                      :two ::g/clj-time-coerce-type
                                      :many (s/* ::g/clj-time-coerce-type)))
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
        :args (s/cat :date-a ::g/clj-time-coerce-type
                     :date-b ::g/clj-time-coerce-type)
        :ret (s/or :boolean boolean? :nil nil?))

(defn nxt-n-day
  "Receives a bill with open-date and a contracted-due-day-of-month
  and Returns a bill with due date"
  [date day-of-month]
  (let [days-to (- day-of-month (t/day date))]
    (t/plus date (t/days days-to) (if (n/not-pos? days-to) (t/months 1)))))

(s/fdef nxt-n-day
        :args (s/cat :date ::g/date-type
                     :day-of-month ::g/day-of-month)
        :ret ::g/date-type
        :fn #(t/after? (:ret %) (-> % :args :date)))