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

(def holidays
  (map (partial apply t/date-time) [[2019 1 1]
                                    [2019 1 6]
                                    [2019 2 2]
                                    [2019 2 4]
                                    [2019 2 5]
                                    [2019 2 14]
                                    [2019 3 24]
                                    [2019 3 6]
                                    [2019 3 18]
                                    [2019 3 18]
                                    [2019 3 20]
                                    [2019 4 21]
                                    [2019 4 14]
                                    [2019 4 18]
                                    [2019 4 19]
                                    [2019 4 20]
                                    [2019 5 21]
                                    [2019 5 30]
                                    [2019 5 1]
                                    [2019 5 5]
                                    [2019 5 10]
                                    [2019 5 15]
                                    [2019 5 30]
                                    [2019 6 9]
                                    [2019 6 16]
                                    [2019 6 20]
                                    [2019 6 21]
                                    [2019 8 15]
                                    [2019 9 15]
                                    [2019 9 16]
                                    [2019 9 23]
                                    [2019 10 12]
                                    [2019 10 31]
                                    [2019 11 1]
                                    [2019 11 2]
                                    [2019 11 18]
                                    [2019 11 20]
                                    [2019 11 24]
                                    [2019 12 8]
                                    [2019 12 12]
                                    [2019 12 21]
                                    [2019 12 24]
                                    [2019 12 25]
                                    [2019 12 28]
                                    [2019 12 31]]))

;;TODO: would it make more sense to model holidays as a file instead of a hardcoded list here?