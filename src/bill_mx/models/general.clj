(ns bill-mx.models.general
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clj-time.coerce :as co]
            [clj-time.format :as tf]
            [bill-mx.numbers :as n]))

;; GENERAL TYPES
(s/def ::money-type decimal?)
(s/def ::rate decimal?)
(s/def ::num-days (s/and int? n/not-neg?))
(s/def ::day-of-month (set (range 1 (inc 31))))
(s/def ::month (set (range 1 (inc 12))))
(s/def ::id uuid?)
(s/def ::type string?)

;; GENERAL DATE

(def date-gen
  #(gen/fmap (fn [inst] (co/to-date-time inst))
             (s/gen (s/inst-in #inst "1900-01-01" #inst "2100-12-31"))))

(s/def ::date (s/with-gen #(instance? org.joda.time.DateTime %)
                                date-gen))

;; GENERAL STRING DATE

(s/def ::date-str-wo-gen (s/and string? #(some? (co/to-date-time %))))

(defn- date-to-str [inst]
  (tf/unparse (tf/formatter "yyyy-MM-dd") (co/to-date-time inst)))

(def date-str-gen
  #(gen/fmap (fn [inst] (date-to-str inst))
             (s/gen (s/inst-in #inst "1900-01-01" #inst "2100-12-31"))))

(s/def ::date-str (s/with-gen ::date-str-wo-gen date-str-gen))

(s/def ::clj-time-coerce-type (s/or :inst inst?
                                    :int int?
                                    :str ::date-str))