(ns bill-mx.core
  (:require [clojure.spec.alpha :as s]
            [clj-time.core :as t]
            [clj-time.predicates :as pr]
            [bill-mx.models.general :as g]
            [bill-mx.models.bill :as bm]
            [bill-mx.models.cc-acc :as cca]
            [bill-mx.time :as bt])
  ;(:gen-class)
  )

(defn open-date
  "Calculates open date from previous bill close date"
  [bill prev-bill-close-date]
  (assoc bill :open-date prev-bill-close-date))

(s/fdef open-date
        :args (s/cat :bill ::bm/bill
                     :prev-bill-close-date ::bm/close-date)
        :ret (s/and ::bm/bill #(s/valid? ::bm/open-date (-> % :open-date))))

(defn due-date
  "Receives a bill with open-date and a contracted-due-day-of-month
  and Returns a bill with due date"
  [bill due-day]
  (let [open-date (:open-date bill)
        due-date (bt/nxt-n-day open-date due-day)]
    (assoc bill :due-date due-date)))

(s/fdef due-date
        :args (s/cat :bill (s/and ::bm/bill #(contains? % :open-date))
                     :due-day ::cca/contracted-due-day-of-month)
        :ret (s/and ::bm/bill
                    #(s/valid? ::bm/due-date (-> % :due-date))
                    #(s/valid? ::bm/open-date (-> % :open-date))))

;;FIXME: stest/check not working for open date and due date due to bill schema

(defn effective-due-date
  "Receives a bill with a due date and a list of holdiays dates and return the
  nxt business date including the actual due date as a possible business date"
  [bill holidays]
  (assoc bill :effective-due-date (bt/nxt-biz-day-incl (:due-date bill) holidays)))

(s/fdef effective-due-date
        :args (s/cat :bill (s/and ::bm/bill #(contains? % :due-date))
                     :holidays (s/coll-of ::g/date))
        :ret (s/and ::bm/bill
                    #(s/valid? ::bm/due-date (-> % :due-date))
                    #(s/valid? ::bm/effective-due-date (-> % :effective-due-date))))

(defn -main
  [prev-bill cc-acc]
  (-> (s/conform ::bm/bill {::bm/status :draft})
      (open-date (:close-date prev-bill))
      (due-date (:contracted-due-day-of-month cc-acc))
      (effective-due-date bt/holidays)
      ;(close-date (:grace-period cc-acc))
      ;(status prev-bill cc-acc)
      ))

;;TODO: create bill schema?