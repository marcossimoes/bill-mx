(ns bill-mx.models.bill
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [bill-mx.models.general :as g]
            [bill-mx.models.line-item :as l]
            [bill-mx.time :as bt]
            [bill-mx.numbers :as n]
            [bill-mx.macros :as bm]
            [clj-time.core :as t]))

;; BILL ITEMS TYPES

(s/def ::status #{:future :open :closed :late :paid :draft})
(s/def ::effective-due-date ::g/date)
(s/def ::due-date ::g/date)
(s/def ::open-date ::g/date)
(s/def ::close-date ::g/date)
(s/def ::current-date ::g/date)
(s/def ::line-items (s/coll-of ::l/line-item :distinct true :into []))
(s/def ::total ::g/money-type)
(s/def ::amount-paid (s/and ::g/money-type n/not-neg?))
(s/def ::min-pmt (s/and ::g/money-type n/not-neg?))

;; BILL VALIDATIONS

(def min-pmt<=total
  #(bm/if-let* [min-pmt (::min-pmt %)
                total (::total %)]
               (if (n/not-pos? total) (<= min-pmt (- total)) 0M)
               true))

(def amount-paid<=total
  #(bm/if-let* [amount-paid (::amount-paid %)
                total (::total %)]
               (<= amount-paid (- total))
               true))

(def effective=>duedate
  #(bm/if-let* [effective-due-date (::effective-due-date %)
                due-date (::due-date %)]
               (bt/date-equal-or-after? effective-due-date due-date)
               true))

(def duedate=>closedate
  #(bm/if-let* [due-date (::due-date %)
                close-date (::close-date %)]
               (bt/date-equal-or-after? due-date close-date)
               true))

(def effective=>closedate
  #(bm/if-let* [effective-due-date (::effective-due-date %)
                close-date (::close-date %)]
               (bt/date-equal-or-after? effective-due-date close-date)
               true))

(def close-date>open-date
  #(bm/if-let* [close-date (::close-date %)
                open-date (::open-date %)]
               (bt/date-after? close-date open-date)
               true))

(def line-items-after-open
  #(bm/if-let* [open-date (::open-date %)
                line-items (::line-items %)]
               (map (fn [{:keys [due-date]}]
                      (bt/date-equal-or-after? due-date open-date))
                    line-items)
               true))

(def line-items-before-close
  #(bm/if-let* [close-date (::close-date %)
                line-items (::line-items %)]
               (map (fn [{:keys [due-date]}]
                      (bt/date-after? close-date due-date))
                    line-items)
               true))

(def line-items=total
  #(bm/if-let* [total (::total %)
                line-items (::line-items %)]
               (= total (reduce (fn [sub-total line-item]
                                  (+ sub-total (::l/amount line-item)))
                                0
                                line-items))
               true))

(def bill-basic-valids (s/and close-date>open-date
                              duedate=>closedate
                              effective=>duedate
                              effective=>closedate
                              line-items-after-open
                              line-items-before-close
                              line-items=total
                              min-pmt<=total
                              ;;amount-paid<=total
                              ))

;; BILL MULTI-SPEC

(defmulti bill-status ::status)

(defmethod bill-status :future [_]
  (s/and (s/keys :req [::status
                       ::open-date ::close-date ::due-date ::effective-due-date
                       ::current-date
                       ::line-items ::total])
         bill-basic-valids
         #(bt/date-after? (::open-date %) (::current-date %))))

(defmethod bill-status :open [_]
  (s/and (s/keys :req [::status
                       ::open-date ::close-date ::due-date ::effective-due-date
                       ::current-date
                       ::line-items ::total ::amount-paid])
         bill-basic-valids
         #(bt/date-equal-or-after? (::current-date) (::open-date))
         #(bt/date-after? (::close-date %) (::current-date %))))

(defmethod bill-status :closed [_]
  (s/and (s/keys :req [::status
                       ::open-date ::close-date ::due-date ::effective-due-date
                       ::current-date
                       ::line-items ::total ::amount-paid ::min-pmt])
         bill-basic-valids
         #(bt/date-equal-or-after? (::current-date %) (::close-date %))))

(defmethod bill-status :paid [_]
  (s/and (s/keys :req [::status
                       ::open-date ::close-date ::due-date ::effective-due-date
                       ::current-date
                       ::line-items ::total ::amount-paid])
         bill-basic-valids
         #(bt/date-equal-or-after? (::current-date %) (::close-date %))
         #(>= (::amount-paid %) (::min-pmt %))))

(defmethod bill-status :late [_]
  (s/and (s/keys :req [::status
                       ::open-date ::close-date ::due-date ::effective-due-date
                       ::current-date
                       ::line-items ::total ::amount-paid ::min-pmt])
         bill-basic-valids
         #(bt/date-equal-or-after? (::current-date %) (::close-date %))
         #(< (::amount-paid %) (::min-pmt %))))

(defmethod bill-status :draft [_]
  (s/and (s/keys :req [::status]
                 :opt [::effective-due-date ::due-date ::open-date ::close-date ::current-date ::line-items ::total ::amount-paid ::min-pmt])
         bill-basic-valids))

(s/def ::bill (s/multi-spec bill-status ::status))

;; BILL CUS GENERATOR

(defn curr-date-gen-
  "Receives bill status, open and due dates and generates
  a possible current date"
  [status open-date close-date effective-due-date]
  (cond
    (get #{:future}     status) (g/date-between-gen (t/minus open-date (t/days (rand-int 60))) open-date)
    (get #{:open}       status) (g/date-between-gen open-date close-date)
    (get #{:closed}     status) (g/date-between-gen close-date (t/plus effective-due-date (t/days 1)))
    (get #{:paid :late} status) (g/date-between-gen (t/plus effective-due-date (t/days 1)) (t/plus effective-due-date (t/days (rand-int 60))))))

(defn min-pmt-
  "Receives a bill and calculates the minimum paument"
  [bill]
  (let [total (::total bill)]
    (if (n/not-pos? total)
      (gen/generate
        (gen/fmap #(bigdec (* % (- total)))
                  (s/gen (s/double-in :min 0.0 :max 1.0 :NaN? false :infinite? false))))
      0M)))

(defn line-items-total
  "Receives line items and calculate their total value"
  [line-items]
  (reduce #(+ %1 (::l/amount %2)) 0 line-items))

(defn bill-with-total-
  "Receives bill status and line items and returns
  a bill with a possible random amount-paid when it is required"
  ([bill]
   (let [sub-total (line-items-total (::line-items bill))]
     (if (= (::status bill) :late) (bill-with-total- bill neg?)
                          (assoc bill ::total sub-total))))
  ([bill predicate]
   (let [sub-total (line-items-total (::line-items bill))]
     (if (predicate sub-total) (assoc bill ::total sub-total)
                               (->> (l/line-item-cus-gen (::open-date bill) (::close-date bill) predicate)
                                    (gen/sample)
                                    (update bill ::line-items conj)
                                    (#(bill-with-total- % neg?)))))))

(defn bill-with-min-pmt-
  "Receives bill status, open and due dates and returns
  a bill with a possible random min-pmt when its required"
  [bill]
  (let [status (::status bill)
        min-pmt (min-pmt- bill)]
    (cond
      (get #{:future :open :paid} status) bill
      (get #{:closed :late} status) (assoc bill ::min-pmt min-pmt))))

(defn cus-amount-paid-gen-
  "Generates a custom amount paid generator accordingly to the opt
  either :above or :bellow a certain value"
  [value opt]
  (let [min (case opt :above 1.0 :bellow 0.0)
        max (case opt :above 10.0 :bellow 1.0)]
    (s/def ::cus-amount-paid
      (gen/fmap #(bigdec (* % value))
                (s/gen (s/double-in :min min :max max :NaN? false :infinite? false))))))

(defn amount-paid-
  "Receives bill status, open and due dates and returns
  a bill with a possible random amount-paid when it is required"
  [status min-pmt]
  (cond
    (get #{:open :closed} status) (gen/generate (s/gen ::amount-paid))
    (get #{:paid} status) (gen/generate (cus-amount-paid-gen- min-pmt :above))
    (get #{:late} status) (gen/generate (cus-amount-paid-gen- min-pmt :bellow))))

(defn bill-with-amount-paid-
  "Receives bill status, open and due dates and returns
  a bill with a possible random amount-paid when it is required"
  [bill]
  (let [status (::status bill)
        min-pmt (::min-pmt bill)]
    (if (= :future status)
      bill
      (assoc bill :amount-paid (amount-paid- status min-pmt)))))

(defn bill
  "returns a bill hash map with the common fields independent of the status"
  [seeds]
  (let [status (get seeds 0)
        open-date (get seeds 1)
        close-date (t/plus open-date (t/days (get seeds 2)))
        due-date (t/plus close-date (t/days (get seeds 3)))
        effective-due-date (t/plus due-date (t/days (get seeds 4)))
        current-date (gen/generate (curr-date-gen- status open-date close-date effective-due-date))
        line-items (gen/sample (l/line-item-cus-gen open-date close-date))]
    (-> (hash-map ::status status
                  ::open-date open-date
                  ::close-date close-date
                  ::due-date due-date
                  ::effective-due-date effective-due-date
                  ::current-date current-date
                  ::line-items line-items)
        (bill-with-total-)
        (bill-with-min-pmt-)
        (bill-with-amount-paid-))))

(def bill-gen-seeds
  (gen/tuple (s/gen ::status)                             ;status
             (s/gen ::g/date)                             ;open-date
             (s/gen (s/int-in 0 60))                      ;days-to-close
             (s/gen (s/int-in 0 60))                      ;days-to-due
             (s/gen (s/int-in 0 10))))                    ;days-to-eff

(def bill-gen
  (gen/fmap (fn [seeds] (bill seeds)) bill-gen-seeds))

;; FIXME: bill gen sometimes generates a Nill Pointer Expection