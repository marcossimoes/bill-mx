(ns bill-mx.models.general
  (:require [clojure.spec.alpha :as s]
            [clj-time.spec :as ts]
            [bill-mx.numbers :as n]))

;; GENERAL TYPES
(s/def ::money-type decimal?)
(s/def ::rate decimal?)
(s/def ::date-type inst?)
;; TODO: create a more generic date-type
(s/def ::num-days (s/and int? n/not-neg?))
(s/def ::day-of-month (set (range 1 (inc 31))))
(s/def ::id uuid?)
(s/def ::type string?)
(s/def ::clj-time-coerce-type (s/or :inst inst?
                                    :int integer?
                                    :str string?))