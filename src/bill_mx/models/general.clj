(ns bill-mx.models.general
  (:require [clojure.spec.alpha :as s]
            [clj-time.spec :as ts]))

;; GENERAL TYPES
(s/def ::money-type decimal?)
(s/def ::rate decimal?)
(s/def ::date-type ::ts/local-date-time)
(s/def ::num-days int?)
(s/def ::day-of-month int?)
(s/def ::id uuid?)
(s/def ::type string?)