(ns bill-mx.time-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test.alpha :as stest]
            [bill-mx.time :refer :all]))

(defn check' [spec-check]
  (->> spec-check
       first
       :clojure.spec.test.check/ret
       :result
       type
       str))

(def date-equal-or-after-check' (check' (stest/check `date-equal-or-after?)))
(def date-after-check' (check' (stest/check `date-after?)))
(def nxt-n-day-check' (check' (stest/check `nxt-n-day)))

(deftest time-funcs-test
         (is (not (= date-equal-or-after-check' "class clojure.lang.ExceptionInfo")))
         (is (not (= date-after-check' "class clojure.lang.ExceptionInfo")))
         (is (not (= nxt-n-day-check' "class clojure.lang.ExceptionInfo")))       )

;; TODO: research better ways to integrate spec with test