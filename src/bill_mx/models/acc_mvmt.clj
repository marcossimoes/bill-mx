(ns bill-mx.models.acc-mvmt
  (:require [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.time :as bt]))

(s/def ::id ::g/id)
(s/def ::type ::g/type)
(s/def ::amount ::g/money-type)
(s/def ::clear-date ::g/date-type)
(s/def ::due-date ::g/date-type)

(s/def ::acc-mvmt (s/and (s/keys :req [::id ::type ::amount ::clear-date ::due-date])
                         #(bt/date-equal-or-after? (::due-date %)
                                                   (::clear-date %))))