(ns bill-mx.models.line-item
  (:require [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [clj-time.core :as t]))

;; LINE ITEMS TYPES
(s/def ::id ::g/id)
(s/def ::type ::g/type)
(s/def ::amount ::g/money-type)
(s/def ::clear-date ::g/date-type)
(s/def ::due-date ::g/date-type)

;; LINE ITEM
(s/def ::line-item (s/and (s/keys :req [::id ::type ::amount ::clear-date ::due-date])
                          #(t/after? (::due-date %) (::clear-date %))))