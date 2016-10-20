(ns windrose.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [windrose.core-test]))

(doo-tests 'windrose.core-test)
