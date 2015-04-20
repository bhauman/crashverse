(ns crashverse.core-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [crashverse.core :refer [center]]))

(deftest center-test
  (is (= (center {:width 50 :height 50}) {:x 25 :y 25}))
  (is (= (center {:width 50 :height 40}) {:x 25 :y 20}))
  (is (= (center {:width 51 :height 41}) {:x 26 :y 21}))
  (is (= (center {:width 0 :height 0}) {:x 0 :y 0})))

