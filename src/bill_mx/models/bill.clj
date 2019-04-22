(ns bill-mx.models.bill
  (:require [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.models.line-item :as l]
            [bill-mx.time :as bt]
            [bill-mx.numbers :as n]))

;; BILL ITEMS TYPES

(s/def ::status #{:future :open :closed :late :paid})
(s/def ::effective-due-date ::g/date-type)
(s/def ::due-date ::g/date-type)
(s/def ::open-date ::g/date-type)
(s/def ::close-date ::g/date-type)
(s/def ::current-date ::g/date-type)
(s/def ::line-items (s/coll-of ::l/line-item :distinct true :into []))
(s/def ::total ::g/money-type)
(s/def ::amount-paid ::g/money-type)
(s/def ::min-pmt ::g/money-type)

;; BILL

(defmulti bill-status ::status)

(defmethod bill-status :future [_]
  (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total]))

(defmethod bill-status :open [_]
  (s/and (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid])
         #(n/not-neg? (::amount-paid %))))

(defmethod bill-status :closed [_]
  (s/and (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid ::min-pmt])
         #(n/not-neg? (::amount-paid %) (::min-pmt %))))

(defmethod bill-status :paid [_]
  (s/and (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid])
         #(n/not-neg? (::amount-paid %))))

(defmethod bill-status :late [_]
  (s/and (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid ::min-pmt])
         #(n/not-neg? (::amount-paid %) (::min-pmt %))))

(s/def ::bill (s/and (s/multi-spec bill-status ::status)
                     #(bt/date-equal-or-after? (::effective-due-date %)
                                            (::due-date %)
                                            (::close-date %))
                     #(bt/date-after? (::close-date %) (::open-date %))
                     ;;#(n/not-neg? (::amount-paid %) (::min-pmt %))
                     ))