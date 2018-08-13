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

(ns chain-reaction.core-test
  (:use midje.sweet)
  (:require [chain-reaction.core :refer :all]))

(fact "new-cell"
  (new-cell)            => nil
  (new-cell :player1 1) => {:owner :player1 :count 1}
  )

(fact "new-board"
  (new-board)     => {:cells {} :width 8 :height 8}
  (new-board 4 4) => {:cells {} :width 4 :height 4}
  )

(fact "board dimensions"
  (board-width  (new-board 4 6)) => 4
  (board-height (new-board 4 6)) => 6
  )

(fact "legal-coords?"
  (legal-coords? (new-board)     [0 0])   => true
  (legal-coords? (new-board)     [7 7])   => true
  (legal-coords? (new-board)     [-1 -1]) => false
  (legal-coords? (new-board)     [8 8])   => false
  (legal-coords? (new-board)     [0 -1])  => false
  (legal-coords? (new-board)     [-1 0])  => false
  (legal-coords? (new-board)     [7 8])   => false
  (legal-coords? (new-board)     [8 7])   => false
  (legal-coords? (new-board 2 2) [2 2])   => false
  )

(fact "set-cell"
  (set-cell (new-board) [0 0] (new-cell :player1 1)) => {:cells {[0 0] {:owner :player1 :count 1}} :width 8 :height 8}
  )

(fact "get-cell"
  (get-cell (new-board) [0 0])                                        => nil
  (get-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]) => {:owner :player1 :count 1}
  )

(fact "all-cells"
  (all-cells (new-board 2 2)) => '([0 0] [0 1] [1 0] [1 1])
  )

(fact "occupied-cells"
  (occupied-cells (new-board))                                        => nil
  (occupied-cells (set-cell (new-board) [0 0] (new-cell :player1 1))) => '([0 0])
  )

(fact "players"
  (players (new-board))                                        => #{}
  (players (set-cell (new-board) [0 0] (new-cell :player1 1))) => #{:player1}

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [1 1] (new-cell :player2 1))]
    (players board) => #{:player1 :player2})

  (let [board (set-cell
                (set-cell
                  (set-cell (new-board)
                            [0 0] (new-cell :player1 1))
                  [1 1] (new-cell :player2 1))
                [0 1] (new-cell :player3 1))]
    (players board) => #{:player1 :player2 :player3})
  )

(fact "cell-owner"
  (cell-owner (new-board) [0 0])                                        => nil
  (cell-owner (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]) => :player1
  )

(fact "cell-unowned?"
  (cell-unowned? (new-board) [0 0])                                        => true
  (cell-unowned? (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]) => false
  )

(fact "cell-count"
  (cell-count (new-board) [0 0])                                        => 0
  (cell-count (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]) => 1
  )

(fact "number-of-neighbours"
  (number-of-neighbours (new-board) [0 0]) => 2
  (number-of-neighbours (new-board) [0 7]) => 2
  (number-of-neighbours (new-board) [7 0]) => 2
  (number-of-neighbours (new-board) [7 7]) => 2
  (number-of-neighbours (new-board) [0 1]) => 3
  (number-of-neighbours (new-board) [0 2]) => 3
  (number-of-neighbours (new-board) [0 3]) => 3
  (number-of-neighbours (new-board) [0 4]) => 3
  (number-of-neighbours (new-board) [0 5]) => 3
  (number-of-neighbours (new-board) [0 6]) => 3
  (number-of-neighbours (new-board) [4 7]) => 3
  (number-of-neighbours (new-board) [1 1]) => 4
  (number-of-neighbours (new-board) [4 4]) => 4
  (number-of-neighbours (new-board) [6 6]) => 4
  (number-of-neighbours (new-board) [1 6]) => 4
  )

(fact "neighbours"
  (neighbours (new-board) [0 0]) => '([1 0] [0 1])
  (neighbours (new-board) [7 7]) => '([7 6] [6 7])
  (neighbours (new-board) [0 1]) => '([0 0] [1 1] [0 2])
  (neighbours (new-board) [7 6]) => '([7 5] [6 6] [7 7])
  (neighbours (new-board) [1 1]) => '([1 0] [0 1] [2 1] [1 2])
  (neighbours (new-board) [6 6]) => '([6 5] [5 6] [7 6] [6 7])
  )

(fact "full?"
  (full? (new-board) [0 0])                                        => false
  (full? (new-board) [0 1])                                        => false
  (full? (new-board) [1 0])                                        => false
  (full? (new-board) [1 1])                                        => false
  (full? (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]) => false
  (full? (set-cell (new-board) [0 0] (new-cell :player1 2)) [0 0]) => true
  (full? (set-cell (new-board) [0 0] (new-cell :player1 9)) [0 0]) => true  ; Note: overfull
  (full? (set-cell (new-board) [0 1] (new-cell :player1 2)) [0 1]) => false
  (full? (set-cell (new-board) [0 1] (new-cell :player1 3)) [0 1]) => true
  (full? (set-cell (new-board) [1 1] (new-cell :player1 3)) [1 1]) => false
  (full? (set-cell (new-board) [1 1] (new-cell :player1 4)) [1 1]) => true
  (full? (set-cell (new-board) [6 6] (new-cell :player1 3)) [6 6]) => false
  (full? (set-cell (new-board) [6 6] (new-cell :player1 4)) [6 6]) => true
  )

(fact "any-full-cells?"
  (any-full-cells? (new-board))                                        => false
  (any-full-cells? (set-cell (new-board) [0 0] (new-cell :player1 1))) => false
  (any-full-cells? (set-cell (new-board) [0 0] (new-cell :player1 2))) => true
  (any-full-cells? (set-cell (new-board) [1 0] (new-cell :player1 2))) => false
  (any-full-cells? (set-cell (new-board) [1 0] (new-cell :player1 3))) => true
  (any-full-cells? (set-cell (new-board) [6 6] (new-cell :player1 3))) => false
  (any-full-cells? (set-cell (new-board) [6 6] (new-cell :player1 4))) => true
  (any-full-cells? (set-cell (new-board) [7 7] (new-cell :player1 9))) => true  ; Note: overfull
  )

(fact "find-full-cell"
  (find-full-cell (new-board))                                        => nil
  (find-full-cell (set-cell (new-board) [0 0] (new-cell :player1 1))) => nil
  (find-full-cell (set-cell (new-board) [0 0] (new-cell :player1 2))) => [0 0]
  (find-full-cell (set-cell (new-board) [0 1] (new-cell :player1 2))) => nil
  (find-full-cell (set-cell (new-board) [0 1] (new-cell :player1 3))) => [0 1]
  (find-full-cell (set-cell (new-board) [2 6] (new-cell :player1 3))) => nil
  (find-full-cell (set-cell (new-board) [2 6] (new-cell :player1 4))) => [2 6]
  (find-full-cell (set-cell (new-board) [7 7] (new-cell :player1 9))) => [7 7]  ; Note: overfull
  )

(fact "explode-full-cell"
  (explode-full-cell (new-board) [0 0])                                        => (throws java.lang.AssertionError)
  (explode-full-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [0 0]) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                           [1 0] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 3)) [0 1]) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                           [0 2] {:count 1 :owner :player1}
                                                                                           [1 1] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  (explode-full-cell (set-cell (new-board) [1 1] (new-cell :player1 4)) [1 1]) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                           [1 0] {:count 1 :owner :player1}
                                                                                           [1 2] {:count 1 :owner :player1}
                                                                                           [2 1] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  )

(fact "explode-full-cell (overfull)"
  (explode-full-cell (set-cell (new-board) [0 0] (new-cell :player1 3)) [0 0]) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                           [0 1] {:count 1 :owner :player1}
                                                                                           [1 0] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 4)) [0 1]) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                           [0 1] {:count 1 :owner :player1}
                                                                                           [0 2] {:count 1 :owner :player1}
                                                                                           [1 1] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  (explode-full-cell (set-cell (new-board) [1 1] (new-cell :player1 7)) [1 1]) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                           [1 0] {:count 1 :owner :player1}
                                                                                           [1 1] {:count 3 :owner :player1}
                                                                                           [1 2] {:count 1 :owner :player1}
                                                                                           [2 1] {:count 1 :owner :player1}}
                                                                                   :height 8 :width 8}
  )

(fact "explode-full-cells"
  (explode-full-cells (new-board))                                        => (new-board)
  (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 1))) => {:cells {[0 0] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 2))) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                      [1 0] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  (explode-full-cells (set-cell (new-board) [0 1] (new-cell :player1 3))) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                      [0 2] {:count 1 :owner :player1}
                                                                                      [1 1] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  (explode-full-cells (set-cell (new-board) [1 1] (new-cell :player1 4))) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                      [1 0] {:count 1 :owner :player1}
                                                                                      [1 2] {:count 1 :owner :player1}
                                                                                      [2 1] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  )

(fact "explode-full-cells (overfull)"
  (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 3))) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                      [0 1] {:count 1 :owner :player1}
                                                                                      [1 0] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  (explode-full-cells (set-cell (new-board) [0 1] (new-cell :player1 4))) => {:cells {[0 0] {:count 1 :owner :player1}
                                                                                      [0 1] {:count 1 :owner :player1}
                                                                                      [0 2] {:count 1 :owner :player1}
                                                                                      [1 1] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  (explode-full-cells (set-cell (new-board) [1 1] (new-cell :player1 7))) => {:cells {[0 1] {:count 1 :owner :player1}
                                                                                      [1 0] {:count 1 :owner :player1}
                                                                                      [1 1] {:count 3 :owner :player1}
                                                                                      [1 2] {:count 1 :owner :player1}
                                                                                      [2 1] {:count 1 :owner :player1}}
                                                                              :height 8 :width 8}
  )

(fact "explode-full-cells (multiple full cells)"
  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [7 7] (new-cell :player2 1))]
    (explode-full-cells board) => board)

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [7 7] (new-cell :player2 1))]
    (explode-full-cells board) => {:cells {[0 1] {:count 1 :owner :player1}
                                           [1 0] {:count 1 :owner :player1}
                                           [7 7] {:count 1 :owner :player2}}
                                   :height 8 :width 8})

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [7 7] (new-cell :player2 2))]
    (explode-full-cells board) => {:cells {[0 1] {:count 1 :owner :player1}
                                           [1 0] {:count 1 :owner :player1}
                                           [6 7] {:count 1 :owner :player2}
                                           [7 6] {:count 1 :owner :player2}}
                                   :height 8 :width 8})
  )

(fact "explode-full-cells (chain reactions)"
  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 1] (new-cell :player1 1))]
    (explode-full-cells board) => board)

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [1 0] (new-cell :player1 2))]
    (explode-full-cells board) => {:cells {[0 0] {:count 1 :owner :player1}
                                           [2 0] {:count 1 :owner :player1}
                                           [0 1] {:count 1 :owner :player1}
                                           [1 1] {:count 1 :owner :player1}}
                                   :height 8 :width 8})

  (let [board (set-cell (set-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [1 0] (new-cell :player1 2)) [2 0] (new-cell :player1 2))]
    (explode-full-cells board) => {:cells {[0 0] {:count 1 :owner :player1}
                                           [1 0] {:count 1 :owner :player1}
                                           [3 0] {:count 1 :owner :player1}
                                           [0 1] {:count 1 :owner :player1}
                                           [1 1] {:count 1 :owner :player1}
                                           [2 1] {:count 1 :owner :player1}}
                                   :height 8 :width 8})
  )

(fact "legal-move?"
  (legal-move? (new-board) :player1 [0 0])                                        => true
  (legal-move? (new-board) :player1 [1 1])                                        => true
  (legal-move? (new-board) :player1 [7 6])                                        => true
  (legal-move? (set-cell (new-board) [0 0] (new-cell :player1 1)) :player1 [0 0]) => true
  (legal-move? (set-cell (new-board) [4 4] (new-cell :player1 1)) :player2 [4 4]) => false
  )

(fact "place-piece"
  (place-piece (new-board) :player1 [0 0]) => {:cells {[0 0] {:count 1 :owner :player1}}
                                               :height 8 :width 8}
  (place-piece (new-board) :player1 [1 1]) => {:cells {[1 1] {:count 1 :owner :player1}}
                                               :height 8 :width 8}
  (place-piece (new-board) :player1 [7 6]) => {:cells {[7 6] {:count 1 :owner :player1}}
                                               :height 8 :width 8}

  (let [board (set-cell (new-board) [0 0] (new-cell :player1 1))]
    (place-piece board :player2 [7 6])     => {:cells {[0 0] {:count 1 :owner :player1}
                                                     [7 6] {:count 1 :owner :player2}}
                                               :height 8 :width 8})

  (let [board (set-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [7 7] (new-cell :player2 1))]
    (place-piece board :player1 [0 0])     => {:cells {[0 1] {:count 1 :owner :player1}
                                                       [1 0] {:count 1 :owner :player1}
                                                       [7 7] {:count 1 :owner :player2}}
                                               :height 8 :width 8})
  )



