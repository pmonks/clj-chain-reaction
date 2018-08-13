;
; Copyright 2015 Peter Monks
; SPDX-License-Identifier: Apache-2.0
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns chain-reaction.console-test
  (:use midje.sweet)
  (:require [jansi-clj.core         :as jansi]
            [chain-reaction.core    :refer :all]
            [chain-reaction.console :refer :all]))

(fact "new-board-str"
  (board-str (new-board 2 2)) => "=0 =0\n=0 =0"
  (board-str (new-board))     => "=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0"

  (let [board (set-cell (new-board) [0 0] (new-cell :player1 1))]
    (board-str board) => ":player1=1 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0")

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [7 7] (new-cell :player2 1))]
    (board-str board) => ":player1=1 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 =0
=0 =0 =0 =0 =0 =0 =0 :player2=1")
  )

(def ansi-empty-cell-str (jansi/white       "●"))
(def player1-1cell-str   (jansi/blue-bright "➊"))

(fact "ansi-board-str"
  (ansi-board-str (new-board 2 2)) => (str clear-screen-str
                                           ansi-empty-cell-str " " ansi-empty-cell-str
                                           "\n"
                                           ansi-empty-cell-str " " ansi-empty-cell-str)

  (let [board (set-cell (new-board 2 2) [0 0] (new-cell :player1 1))]
    (ansi-board-str board) => (str clear-screen-str
                                   player1-1cell-str " " ansi-empty-cell-str
                                   "\n"
                                   ansi-empty-cell-str " " ansi-empty-cell-str))
  )
