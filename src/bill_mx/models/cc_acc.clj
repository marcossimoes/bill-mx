(ns bill-mx.models.cc-acc
  (:require [clojure.spec.alpha :as s]
            [bill-mx.models.general :as g]
            [bill-mx.models.acc-mvmt :as acm]))

(s/def ::id ::g/id)
(s/def ::mvmts (s/coll-of ::acm/acc-mvmt :distinct true :into []))
(s/def ::contracted-due-day-of-month ::g/day-of-month)
(s/def ::grace-period (s/with-gen ::g/num-days
                                  #(s/gen (s/int-in 0 60))))
(s/def ::days-to-recog-pmt ::g/num-days)
(s/def ::min-pmt-func ::g/rate)                             ;; TODO: change this from rate to function
(s/def ::rev-int-apr ::g/rate)
(s/def ::late-int-apr ::g/rate)
(s/def ::late-fee ::g/money-type)
(s/def ::fx-spread ::g/rate)

(s/def ::cc-acc (s/and (s/keys :req [::id ::mvmts ::contracted-due-day-of-month ::grace-period ::days-to-recog-pmt
                                     ::min-pmt-func ::rev-int-apr ::late-int-apr ::late-fee ::fx-spread])
                         #(> (::late-int-apr %) (::rev-int-apr %))))