(ns bill-mx.models.line-item
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clj-time.core :as t]
            [bill-mx.models.general :as g]
            [bill-mx.time :as bt]))

;; LINE ITEMS TYPES
(s/def ::id ::g/id)
(s/def ::type ::g/type)
(s/def ::amount ::g/money-type)
(s/def ::clear-date ::g/date)
(s/def ::due-date ::g/date)

;; LINE ITEM
(s/def ::line-item (s/and (s/keys :req [::id ::type ::amount ::clear-date ::due-date])
                          #(bt/date-equal-or-after? (::due-date %) (::clear-date %))))

;; CUSTOM AMOUNT GEN

(defn cus-amount-gen-
  "Generates a custom amount generator accordingly to the
  predicate provided"
  [predicate]
  (s/def ::cus-amount (s/and ::g/money-type predicate)))

;; LINE ITEM OPTIONAL CUSTOM GENERATOR
(defn line-item-cus-gen
  "Receives an open-date, close-date and an optional signal keyword
  and returns a line-item whose dates are between these two dates
  open incl, close excl"
  ([open-date close-date]
    (line-item-cus-gen open-date close-date any?))
  ([open-date close-date predicate]
   (gen/fmap
     (fn [[id type amount days-before-due due-date]]
       (hash-map ::id id
                 ::type type
                 ::amount amount
                 ::clear-date (t/minus due-date (t/days days-before-due))
                 ::due-date due-date))
     (gen/tuple (s/gen ::id)
                (s/gen ::type)
                (s/gen (cus-amount-gen- predicate))
                (s/gen (s/int-in 0 2000))
                (g/date-between-gen open-date close-date)))))