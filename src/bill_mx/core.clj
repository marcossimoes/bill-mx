(ns bill-mx.core
  (:require [bill-mx.time :as bt]
            [clojure.spec.alpha :as s]
            [bill-mx.models.bill :as bm]
            [bill-mx.models.cc_acc :as cca])
  (:gen-class))

(defn open-date
  "Calculates open date from previous bill close date"
  [bill prev-bill-close-date]
  (assoc bill :open-date prev-bill-close-date))

(s/fdef open-date
        :args (s/cat :bill ::bm/bill
                     :prev-bill-close-date ::bm/close-date)
        :ret ::bm/bill)

(defn due-date
  "Receives a bill with open-date and a contracted-due-day-of-month
  and Returns a bill with due date"
  [bill due-day]
  (let [open-date (:open-date bill)
        due-date (bt/nxt-n-day open-date due-day)]
    (assoc bill :due-date due-date)))

(s/fdef due-date
        :args (s/cat :bill ::bm/bill
                     :due-day ::cca/contracted-due-day-of-month)
        :ret ::bm/bill)

(defn -main
  [prev-bill cc-acc]
  (-> (s/conform ::bm/bill {::bm/status :draft})
      (open-date (:close-date prev-bill))
      (due-date (:contracted-due-day-of-month cc-acc))
      ;(effective-due-date)
      ;(close-date (:grace-period cc-acc))
      ;(status prev-bill cc-acc)
      ))

;;TODO: create bill schema?