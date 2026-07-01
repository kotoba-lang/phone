(ns kotoba.phone.export-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.phone :as phone]
            [kotoba.phone.export :as ex]))
(deftest csv-export
  (let [csv (ex/numbers->csv ["+442079460958" "bad"])]
    (is (re-find #"number,valid,normalized" csv))
    (is (re-find #"\+442079460958,yes" csv))
    (is (re-find #"bad,no" csv))))
(deftest json-export
  (let [j (ex/numbers->json ["+442079460958"])]
    (is (re-find #"\"valid\":true" j))))
