(ns bill-mx.time
  (:require [clj-time.core :as t]
            [clj-time.coerce :as co]
            [clj-time.predicates :as pr]
            [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.macros :as bm]
            [bill-mx.numbers :as n]))



(defn date-equal-or-after?
  "Checks if the dates provided are the same
  or in order from the first being after the second and so on"
  ([date-a date-b]
   (bm/when-let* [formatted-date-a (co/to-date-time date-a)
                  formatted-date-b (co/to-date-time date-b)]
     (not (t/before? formatted-date-a
                     formatted-date-b))))
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
  (bm/when-let* [formatted-date-a (co/to-date-time date-a)
                 formatted-date-b (co/to-date-time date-b)]
                (t/after? formatted-date-a
                          formatted-date-b)))

(s/fdef date-after?
        :args (s/cat :date-a ::g/clj-time-coerce-type
                     :date-b ::g/clj-time-coerce-type)
        :ret (s/or :boolean boolean? :nil nil?))

(defn nxt-n-day
  "Receives a bill with open-date and a contracted-due-day-of-month
  and Returns a bill with due date"
  [date day-of-month]
  (bm/when-let* [formatted-date (co/to-date-time date)
                 days-to (- day-of-month (t/day formatted-date))]
    (t/plus formatted-date (t/days days-to) (if (n/not-pos? days-to) (t/months 1)))))

(s/fdef nxt-n-day
        :args (s/cat :date ::g/clj-time-coerce-type
                     :day-of-month ::g/day-of-month)
        :ret ::g/date
        :fn #(t/after? (:ret %) (co/to-local-date (-> % :args :date (second)))))

;FIXME: stest/check not working for nxt-n-day

(def holidays
  (set (map (partial apply t/date-time) [[2019 1 1] [2019 1 6] [2019 2 2] [2019 2 4]
                                         [2019 2 5] [2019 2 14] [2019 3 24] [2019 3 6]
                                         [2019 3 18] [2019 3 18] [2019 3 20] [2019 4 21]
                                         [2019 4 14] [2019 4 18] [2019 4 19] [2019 4 20]
                                         [2019 5 21] [2019 5 30] [2019 5 1] [2019 5 5]
                                         [2019 5 10] [2019 5 15] [2019 5 30] [2019 6 9]
                                         [2019 6 16] [2019 6 20] [2019 6 21] [2019 8 15]
                                         [2019 9 15] [2019 9 16] [2019 9 23] [2019 10 12]
                                         [2019 10 31] [2019 11 1] [2019 11 2] [2019 11 18]
                                         [2019 11 20] [2019 11 24] [2019 12 8] [2019 12 12]
                                         [2019 12 21] [2019 12 24] [2019 12 25] [2019 12 28]
                                         [2019 12 31]])))

(defn contains-date?
  "Takes a date and returns true if the date is in a specific vector"
  [holidays date]
  (let [formatted-date (co/to-date-time date)]
    (some (partial pr/same-date? formatted-date) holidays)))

(s/fdef contains-date?
        :args (s/cat :holidays (s/coll-of ::g/date)
                     :date ::g/clj-time-coerce-type)
        :ret (s/or :boolean boolean? :nil nil?))

;; TODO: make contains-date accept date in any format compatible with clj-time coerce

(defn biz-day?
  "provided a date and a list of holidays returns true if the date is both a weekday
  and not a holiday"
  [date holidays]
  (let [formatted-date (co/to-date-time date)]
    (and (pr/weekday? formatted-date) (not (contains-date? holidays formatted-date)))))

(s/fdef biz-day?
        :args (s/cat :date ::g/clj-time-coerce-type
                     :holidays (s/coll-of ::g/date))
        :ret boolean?)

(defn nxt-day
  "provided a date returns the nxt day"
  [date]
  (let [formatted-date (co/to-date-time date)]
    (t/plus formatted-date (t/days 1))))

(s/fdef nxt-day
        :args ::g/clj-time-coerce-type
        :ret ::g/date
        :fn #(t/after? (:ret %) (co/to-local-date (-> % :args :date (second)))))

;FIXME: stest/check not working for nxt-day

(defn nxt-biz-day-incl
  "Provided a date and a list of holidays return the nxt week day that is not
  a holiday. if the provided date is a week day and not a holiday returns the same date"
  [date holidays]
  (let [formatted-date (co/to-date-time date)]
    (if (biz-day? formatted-date holidays)
      formatted-date
      (nxt-biz-day-incl (nxt-day formatted-date) holidays))))

(s/fdef nxt-biz-day-incl
        :args (s/cat :date ::g/clj-time-coerce-type
                     :holidays (s/coll-of ::g/date))
        :ret ::g/date
        :fn #(t/after? (:ret %) (co/to-local-date (-> % :args :date (second)))))

;FIXME: stest/check not working for nxt-biz-day-incl

;;TODO: would it make more sense to model holidays as a file instead of a hardcoded list here?