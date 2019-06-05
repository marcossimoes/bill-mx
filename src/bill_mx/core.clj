(ns bill-mx.core
  (:require [clojure.spec.alpha :as s]
            [clj-time.core :as t]
            [clj-time.coerce :as co]
            [bill-mx.models.general :as g]
            [bill-mx.models.bill :as bill]
            [bill-mx.models.cc-acc :as cca]
            [bill-mx.time :as bt])
  ;(:gen-class)
  )

(defn open-date
  "Receives a bill and a previous-bill-close-date and
  Returns a bill with an open date calculated from previous bill close date"
  [bill prev-bill-close-date]
  (when-let [formatted-date (co/to-date-time prev-bill-close-date)]
    (assoc bill ::bill/open-date formatted-date)))

(s/fdef open-date
        :args (s/cat :bill ::bill/bill
                     :prev-bill-close-date ::bill/close-date)
        :ret (s/and ::bill/bill #(s/valid? ::bill/open-date (-> % ::bill/open-date))))

(defn close-and-due-date
  "Receives a bill with open-date, a contracted-due-day-of-month
  and a grace-period and returns a bill with due date and close date"
  [bill due-day grace-period]
  (let [close-day (- due-day grace-period)
        close-date (-> (::bill/open-date bill)
                       (t/plus (t/months 1))
                       (bt/closest-n-day close-day))
        due-date (t/plus close-date (t/days grace-period))]
    (assoc bill ::bill/close-date close-date
                ::bill/due-date due-date)))

(s/fdef close-and-due-date
        :args (s/cat :bill (s/and ::bill/bill #(contains? % ::bill/open-date))
                     :due-day ::g/day-of-month
                     :grace-period ::cca/grace-period)
        :ret (s/and ::bill/bill
                    #(s/valid? ::bill/open-date (::bill/open-date %))
                    #(s/valid? ::bill/close-date (::bill/close-date %))
                    #(s/valid? ::bill/due-date (::bill/due-date %))))

(defn effective-due-date
  "Receives a bill with a due date and a list of holdiays dates and return the
  nxt business date including the actual due date as a possible business date"
  [bill holidays]
  (assoc bill ::bill/effective-due-date (bt/nxt-biz-day-incl (::bill/due-date bill) holidays)))

(s/fdef effective-due-date
        :args (s/cat :bill (s/and ::bill/bill #(contains? % ::bill/due-date))
                     :holidays (s/coll-of ::g/date))
        :ret (s/and ::bill/bill
                    #(s/valid? ::bill/due-date (::bill/due-date %))
                    #(s/valid? ::bill/effective-due-date (::bill/effective-due-date %))))

(defn -main
  [prev-bill cc-acc]
  (-> (s/conform ::bill/bill {::bill/status :draft})
      (open-date (::bill/close-date prev-bill))
      (close-and-due-date (::cca/contracted-due-day-of-month cc-acc)
                          (::cca/grace-period cc-acc))
      (effective-due-date bt/holidays)
      ;(status prev-bill cc-acc)
      ))