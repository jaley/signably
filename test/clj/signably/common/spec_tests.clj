(ns signably.common.spec-tests
  (:require
   [clojure.test :refer :all]
   [clojure.spec.test.alpha :as stest]
   [signably.common.data]))

(defn check-namespace
  "Run generative tests on given namespace (symbol) and return
  the results summary {:total, :check-passed, :check-failed}"
  [sym]
  (println "Checking namespace: " sym)
  (-> sym
      stest/enumerate-namespace
      stest/check
      stest/summarize-results))

(defn test-namespace
  "Runs spec checks on namespace and asserts that there
  were no failures"
  [sym]
  (is (nil? (-> sym check-namespace :check-failed))
      (str "Spec checks for namesapce failed: " sym)))

(deftest data-test (test-namespace 'signably.common.data))
