(ns bill-mx.models.bill
  (:require [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.models.line-item :as l]))

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

(defn- bill-status-dispatch [status]
  (case status
    :future :future
    :open :open-or-paid
    :closed :closed-or-late
    :late :closed-or-late
    :paid :open-or-paid))

(defmulti bill-status bill-status-dispatch)

(defmethod bill-status :future [_]
  (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total]))

(defmethod bill-status :open-or-paid [_]
  (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid]))

(defmethod bill-status :closed-or-late [_]
  (s/keys :req [::status ::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid ::min-pmt]))

(s/def ::bill (s/multi-spec bill-status ::status))