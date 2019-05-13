(ns chain-reaction.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [chain-reaction.core-test]))

(doo-tests 'chain-reaction.core-test)
