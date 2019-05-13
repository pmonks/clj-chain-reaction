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
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test    :refer-macros [deftest is testing run-tests]])
            [chain-reaction.core :refer [new-cell
                                         new-board
                                         board-width
                                         board-height
                                         legal-coords?
                                         set-cell
                                         get-cell
                                         all-cell-coords
                                         occupied-cells
                                         players
                                         cell-owner
                                         cell-unowned?
                                         cell-count
                                         number-of-neighbours
                                         neighbours
                                         full?
                                         any-full-cells?
                                         find-first-full-cell
                                         explode-full-cell
                                         explode-full-cells
                                         legal-move?
                                         place-piece]]))
#?(:clj  (println "\n☔️ Running tests on Clojure" (clojure-version) "/ JVM" (System/getProperty "java.version") (str "(" (System/getProperty "java.vm.name") " v" (System/getProperty "java.vm.version") ")"))
   :cljs (println "\n☔️ Running tests on ClojureScript" *clojurescript-version* "/ Node" (. js/process -version)))

(deftest new-cell-tests
  (testing "new-cell"
    (testing "no-args" (is (nil? (new-cell))))
    (testing "args"    (is (= {:owner :player1 :count 1}
                              (new-cell :player1 1))))))

(deftest new-board-tests
  (testing "new-board"
    (testing "default-size"  (is (= {:cells {} :width 8 :height 8}
                                    (new-board))))
    (testing "provided-size" (is (= {:cells {} :width 4 :height 4}
                                    (new-board 4 4))))))

(deftest board-dimensions-tests
  (testing "board-dimensions"
    (testing "width"  (is (= 4 (board-width  (new-board 4 6)))))
    (testing "height" (is (= 6 (board-height (new-board 4 6)))))))

(deftest legal-coords-tests
  (testing "legal-coords?"
    (testing "default-board"
      (testing "legal-coord"
        (testing "minimum" (is (true? (legal-coords? (new-board) [0 0]))))
        (testing "maximum" (is (true? (legal-coords? (new-board) [7 7])))))
      (testing "illegal-coord"
        (testing "both-negative" (is (false? (legal-coords? (new-board) [-1 -1]))))
        (testing "both-too-big"  (is (false? (legal-coords? (new-board) [8 8]))))
        (testing "y-negative"    (is (false? (legal-coords? (new-board) [0 -1]))))
        (testing "x-negative"    (is (false? (legal-coords? (new-board) [-1 0]))))
        (testing "y-too-big"     (is (false? (legal-coords? (new-board) [7 8]))))
        (testing "x-too-big"     (is (false? (legal-coords? (new-board) [8 7]))))))
    (testing "non-default-board"
      (testing "illegal-coord"
        (testing "both-too-big" (is (false? (legal-coords? (new-board 2 2) [2 2]))))))))

(deftest set-cell-tests
  (testing "set-cell"
    (testing "legal-coord" (is (= {:cells {[0 0] {:owner :player1 :count 1}} :width 8 :height 8}
                                  (set-cell (new-board) [0 0] (new-cell :player1 1)))))))

(deftest get-cell-tests
  (testing "get-cell"
    (testing "empty-cell"     (is (nil? (get-cell (new-board) [0 0]))))
    (testing "populated-cell" (is (= {:owner :player1 :count 1}
                                     (get-cell (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]))))))

(deftest all-cell-coords-tests
  (testing "all-cell-coords"
    (testing "2x2-board" (is (= '([0 0] [0 1] [1 0] [1 1])
                                (all-cell-coords (new-board 2 2)))))))

(deftest occupied-cells-tests
  (testing "occupied-cells"
    (testing "empty-board"    (is (nil? (occupied-cells (new-board)))))
    (testing "populaed-board" (is (= '([0 0])
                                     (occupied-cells (set-cell (new-board) [0 0] (new-cell :player1 1))))))))

(deftest players-tests
  (testing "players"
    (testing "empty-board" (is (= #{} (players (new-board)))))
    (testing "populated-board"
      (testing "1-cell" (is (= #{:player1}
                               (players (set-cell (new-board) [0 0] (new-cell :player1 1))))))
      (testing "2-cells" (is (= #{:player1 :player2}
                                (players (-> (new-board)
                                             (set-cell [0 0] (new-cell :player1 1))
                                             (set-cell [1 1] (new-cell :player2 1)))))))
      (testing "3-cells" (is (= #{:player1 :player2 :player3}
                                (players (-> (new-board)
                                             (set-cell [0 0] (new-cell :player1 1))
                                             (set-cell [1 1] (new-cell :player2 1))
                                             (set-cell [0 1] (new-cell :player3 1))))))))))

(deftest cell-owner-tests
  (testing "cell-owner"
    (testing "empty-cell"     (is (nil? (cell-owner (new-board) [0 0]))))
    (testing "populated-cell" (is (= :player1 (cell-owner (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]))))))


(deftest cell-unowned-tests
  (testing "cell-unowned?"
    (testing "empty-cell"     (is (true? (cell-unowned? (new-board) [0 0]))))
    (testing "populated-cell" (is (false? (cell-unowned? (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]))))))

(deftest cell-count-tests
  (testing "cell-count"
    (testing "empty cell"     (is (= 0 (cell-count (new-board) [0 0]))))
    (testing "populated cell" (is (= 1 (cell-count (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]))))))

(deftest number-of-neighbours-tests
  (testing "number-of-neighbours"
    (testing "corner" (is (= 2 (number-of-neighbours (new-board) [0 0]))))
    (testing "corner" (is (= 2 (number-of-neighbours (new-board) [0 7]))))
    (testing "corner" (is (= 2 (number-of-neighbours (new-board) [7 0]))))
    (testing "corner" (is (= 2 (number-of-neighbours (new-board) [7 7]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 1]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 2]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 3]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 4]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 5]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [0 6]))))
    (testing "edge"   (is (= 3 (number-of-neighbours (new-board) [4 7]))))
    (testing "centre" (is (= 4 (number-of-neighbours (new-board) [1 1]))))
    (testing "centre" (is (= 4 (number-of-neighbours (new-board) [4 4]))))
    (testing "centre" (is (= 4 (number-of-neighbours (new-board) [6 6]))))
    (testing "centre" (is (= 4 (number-of-neighbours (new-board) [1 6]))))))

(deftest neighbours-tests
  (testing "neighbours"
    (testing "corner" (is (= '([1 0] [0 1])             (neighbours (new-board) [0 0]))))
    (testing "corner" (is (= '([7 6] [6 7])             (neighbours (new-board) [7 7]))))
    (testing "edge"   (is (= '([0 0] [1 1] [0 2])       (neighbours (new-board) [0 1]))))
    (testing "edge"   (is (= '([7 5] [6 6] [7 7])       (neighbours (new-board) [7 6]))))
    (testing "centre" (is (= '([1 0] [0 1] [2 1] [1 2]) (neighbours (new-board) [1 1]))))
    (testing "centre" (is (= '([6 5] [5 6] [7 6] [6 7]) (neighbours (new-board) [6 6]))))))

(deftest full-tests
  (testing "full"
    (testing "empty-board"
      (testing "corner" (is (false? (full? (new-board) [0 0]))))
      (testing "edge"   (is (false? (full? (new-board) [0 1]))))
      (testing "centre" (is (false? (full? (new-board) [1 1])))))
    (testing "populated-board"
      (testing "non-full-cell"
        (testing "corner" (is (false? (full? (set-cell (new-board) [0 0] (new-cell :player1 1)) [0 0]))))
        (testing "edge"   (is (false? (full? (set-cell (new-board) [0 1] (new-cell :player1 2)) [0 1]))))
        (testing "centre" (is (false? (full? (set-cell (new-board) [1 1] (new-cell :player1 3)) [1 1])))))
      (testing "full-cell"
        (testing "corner" (is (true? (full? (set-cell (new-board) [0 0] (new-cell :player1 2)) [0 0]))))
        (testing "edge"   (is (true? (full? (set-cell (new-board) [0 1] (new-cell :player1 3)) [0 1]))))
        (testing "centre" (is (true? (full? (set-cell (new-board) [1 1] (new-cell :player1 4)) [1 1])))))
      (testing "overfull-cell"
        (testing "corner" (is (true? (full? (set-cell (new-board) [7 7] (new-cell :player1 4))  [7 7]))))
        (testing "edge"   (is (true? (full? (set-cell (new-board) [5 7] (new-cell :player1 7))  [5 7]))))
        (testing "centre" (is (true? (full? (set-cell (new-board) [6 2] (new-cell :player1 12)) [6 2]))))))))

(deftest any-full-cells-tests
  (testing "any-full-cells"
    (testing "empty-board" (is (false? (any-full-cells? (new-board)))))
    (testing "populated-board"
      (testing "no-full-cells"
        (testing "corner" (is (false? (any-full-cells? (set-cell (new-board) [0 0] (new-cell :player1 1))))))
        (testing "edge"   (is (false? (any-full-cells? (set-cell (new-board) [1 0] (new-cell :player1 2))))))
        (testing "centre" (is (false? (any-full-cells? (set-cell (new-board) [6 6] (new-cell :player1 3)))))))
      (testing "1-full-cell"
        (testing "corner" (is (true? (any-full-cells? (set-cell (new-board) [0 0] (new-cell :player1 2))))))
        (testing "edge"   (is (true? (any-full-cells? (set-cell (new-board) [1 0] (new-cell :player1 3))))))
        (testing "centre" (is (true? (any-full-cells? (set-cell (new-board) [6 6] (new-cell :player1 4)))))))
      (testing "1-overfull-cell"
        (testing "corner" (is (true? (any-full-cells? (set-cell (new-board) [7 7] (new-cell :player1 4))))))
        (testing "edge"   (is (true? (any-full-cells? (set-cell (new-board) [0 4] (new-cell :player1 7))))))
        (testing "centre" (is (true? (any-full-cells? (set-cell (new-board) [5 5] (new-cell :player1 9))))))))))

(deftest find-first-full-cells-tests
  (testing "find-first-full-cell"
    (testing "empty-board" (is (nil? (find-first-full-cell (new-board)))))
    (testing "populated-board"
      (testing "1-non-empty-cell"
        (testing "non-full"
          (testing "corner" (is (nil? (find-first-full-cell (set-cell (new-board) [0 0] (new-cell :player1 1))))))
          (testing "edge"   (is (nil? (find-first-full-cell (set-cell (new-board) [0 1] (new-cell :player1 2))))))
          (testing "centre" (is (nil? (find-first-full-cell (set-cell (new-board) [2 6] (new-cell :player1 3)))))))
        (testing "full"
          (testing "corner" (is (= [0 0] (find-first-full-cell (set-cell (new-board) [0 0] (new-cell :player1 2))))))
          (testing "edge"   (is (= [0 1] (find-first-full-cell (set-cell (new-board) [0 1] (new-cell :player1 3))))))
          (testing "centre" (is (= [2 6] (find-first-full-cell (set-cell (new-board) [2 6] (new-cell :player1 4)))))))
        (testing "overfull"
          (testing "corner" (is (= [7 0] (find-first-full-cell (set-cell (new-board) [7 0] (new-cell :player1 9))))))
          (testing "edge"   (is (= [5 7] (find-first-full-cell (set-cell (new-board) [5 7] (new-cell :player1 9))))))
          (testing "centre" (is (= [2 2] (find-first-full-cell (set-cell (new-board) [2 2] (new-cell :player1 9)))))))
      (testing "2-non-empty-cells"
        (testing "corner-and-centre" (is (= [0 0]
                                            (find-first-full-cell (-> (new-board)
                                                                      (set-cell [0 0] (new-cell :player1 2))
                                                                      (set-cell [4 4] (new-cell :player2 4))))))))))))

(deftest explode-full-cell-tests
  (testing "explode-full-cell"
    (testing "non-full-cell"
      (testing "corner" #?(:clj  (is (thrown? java.lang.AssertionError (explode-full-cell (new-board) [0 0])))
                           :cljs (is (thrown? js/Error                 (explode-full-cell (new-board) [0 0])))))
      (testing "edge"   #?(:clj  (is (thrown? java.lang.AssertionError (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 1)) [0 1])))
                           :cljs (is (thrown? js/Error                 (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 1)) [0 1])))))
      (testing "centre" #?(:clj  (is (thrown? java.lang.AssertionError (explode-full-cell (set-cell (new-board) [4 4] (new-cell :player1 3)) [4 4])))
                           :cljs (is (thrown? js/Error                 (explode-full-cell (set-cell (new-board) [4 4] (new-cell :player1 3)) [4 4]))))))
    (testing "empty-neighbours"
      (testing "exactly-full"
        (testing "corner" (is (= (-> (new-board)
                                     (set-cell [0 1] (new-cell :player1 1))
                                     (set-cell [1 0] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [0 0] (new-cell :player1 2)) [0 0]))))
        (testing "edge"   (is (= (-> (new-board)
                                     (set-cell [0 0] (new-cell :player1 1))
                                     (set-cell [0 2] (new-cell :player1 1))
                                     (set-cell [1 1] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 3)) [0 1]))))
        (testing "centre" (is (= (-> (new-board)
                                     (set-cell [0 1] (new-cell :player1 1))
                                     (set-cell [1 0] (new-cell :player1 1))
                                     (set-cell [1 2] (new-cell :player1 1))
                                     (set-cell [2 1] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [1 1] (new-cell :player1 4)) [1 1])))))
      (testing "over-full"
        (testing "corner" (is (= (-> (new-board)
                                     (set-cell [0 0] (new-cell :player1 1))
                                     (set-cell [0 1] (new-cell :player1 1))
                                     (set-cell [1 0] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [0 0] (new-cell :player1 3)) [0 0]))))
        (testing "edge"   (is (= (-> (new-board)
                                     (set-cell [0 0] (new-cell :player1 1))
                                     (set-cell [0 1] (new-cell :player1 1))
                                     (set-cell [0 2] (new-cell :player1 1))
                                     (set-cell [1 1] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [0 1] (new-cell :player1 4)) [0 1]))))
        (testing "centre" (is (= (-> (new-board)
                                     (set-cell [0 1] (new-cell :player1 1))
                                     (set-cell [1 0] (new-cell :player1 1))
                                     (set-cell [1 1] (new-cell :player1 3))
                                     (set-cell [1 2] (new-cell :player1 1))
                                     (set-cell [2 1] (new-cell :player1 1)))
                                 (explode-full-cell (set-cell (new-board) [1 1] (new-cell :player1 7)) [1 1]))))))
  (testing "populated-neighbours"
    (testing "corner" (is (= (-> (new-board)
                                 (set-cell [0 1] (new-cell :player1 2))
                                 (set-cell [1 0] (new-cell :player1 1)))
                             (explode-full-cell (-> (new-board)
                                                    (set-cell [0 0] (new-cell :player1 2))
                                                    (set-cell [0 1] (new-cell :player2 1)))
                                                [0 0]))))
    (testing "edge"   (is (= (-> (new-board)
                                 (set-cell [3 7] (new-cell :player1 1))
                                 (set-cell [4 6] (new-cell :player1 1))
                                 (set-cell [5 7] (new-cell :player1 2)))
                             (explode-full-cell (-> (new-board)
                                                    (set-cell [4 7] (new-cell :player1 3))
                                                    (set-cell [5 7] (new-cell :player2 1)))
                                                [4 7]))))
    (testing "centre" (is (= (-> (new-board)
                                 (set-cell [3 4] (new-cell :player1 2))
                                 (set-cell [4 3] (new-cell :player1 2))
                                 (set-cell [4 5] (new-cell :player1 2))
                                 (set-cell [5 4] (new-cell :player1 2)))
                             (explode-full-cell (-> (new-board)
                                                    (set-cell [4 4] (new-cell :player1 4))
                                                    (set-cell [3 4] (new-cell :player2 1))
                                                    (set-cell [4 3] (new-cell :player3 1))
                                                    (set-cell [4 5] (new-cell :player4 1))
                                                    (set-cell [5 4] (new-cell :player5 1)))
                                                [4 4])))))))

(deftest explode-full-cells-tests
  (testing "explode-full-cells"
    (testing "empty-board" (is (= (explode-full-cells (new-board)) (new-board))))
    (testing "no-full-cells"
      (testing "corner" (is (= (set-cell (new-board) [0 0] (new-cell :player1 1))
                               (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 1))))))
      (testing "edge"   (is (= (set-cell (new-board) [0 1] (new-cell :player1 2))
                               (explode-full-cells (set-cell (new-board) [0 1] (new-cell :player1 2))))))
      (testing "centre" (is (= (set-cell (new-board) [3 3] (new-cell :player1 3))
                               (explode-full-cells (set-cell (new-board) [3 3] (new-cell :player1 3)))))))
    (testing "1-full-cell"
      (testing "corner" (is (= (-> (new-board)
                                   (set-cell [0 1] (new-cell :player1 1))
                                   (set-cell [1 0] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 2))))))
      (testing "edge"   (is (= (-> (new-board)
                                   (set-cell [0 0] (new-cell :player1 1))
                                   (set-cell [0 2] (new-cell :player1 1))
                                   (set-cell [1 1] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [0 1] (new-cell :player1 3))))))
      (testing "centre" (is (= (-> (new-board)
                                   (set-cell [0 1] (new-cell :player1 1))
                                   (set-cell [1 0] (new-cell :player1 1))
                                   (set-cell [1 2] (new-cell :player1 1))
                                   (set-cell [2 1] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [1 1] (new-cell :player1 4)))))))
    (testing "multiple-full-cells"
      (testing "non-interacting"
        (testing "corners" (is (= (-> (new-board)
                                      (set-cell [0 1] (new-cell :player1 1))
                                      (set-cell [1 0] (new-cell :player1 1))
                                      (set-cell [6 7] (new-cell :player1 1))
                                      (set-cell [7 6] (new-cell :player1 1)))
                                  (explode-full-cells (-> (new-board)
                                                          (set-cell [0 0] (new-cell :player1 2))
                                                          (set-cell [7 7] (new-cell :player1 2)))))))
        (testing "edges"   (is (= (-> (new-board)
                                      (set-cell [0 3] (new-cell :player1 1))
                                      (set-cell [1 4] (new-cell :player1 1))
                                      (set-cell [0 5] (new-cell :player1 1))
                                      (set-cell [3 7] (new-cell :player1 1))
                                      (set-cell [4 6] (new-cell :player1 1))
                                      (set-cell [5 7] (new-cell :player1 1)))
                                  (explode-full-cells (-> (new-board)
                                                          (set-cell [0 4] (new-cell :player1 3))
                                                          (set-cell [4 7] (new-cell :player1 3)))))))
        (testing "centres" (is (= (-> (new-board)
                                      (set-cell [1 0] (new-cell :player1 1))
                                      (set-cell [0 1] (new-cell :player1 1))
                                      (set-cell [1 2] (new-cell :player1 1))
                                      (set-cell [2 1] (new-cell :player1 1))
                                      (set-cell [5 6] (new-cell :player1 1))
                                      (set-cell [6 5] (new-cell :player1 1))
                                      (set-cell [7 6] (new-cell :player1 1))
                                      (set-cell [6 7] (new-cell :player1 1)))
                                  (explode-full-cells (-> (new-board)
                                                          (set-cell [1 1] (new-cell :player1 4))
                                                          (set-cell [6 6] (new-cell :player1 4))))))))
      (testing "interacting" (is (= (-> (new-board)
                                        (set-cell [0 0] (new-cell :player1 1))
                                        (set-cell [0 1] (new-cell :player1 1))
                                        (set-cell [1 0] (new-cell :player1 1))
                                        (set-cell [1 1] (new-cell :player1 1))
                                        (set-cell [0 2] (new-cell :player1 1)))
                                    (explode-full-cells (-> (new-board)
                                                            (set-cell [0 0] (new-cell :player1 2))
                                                            (set-cell [0 1] (new-cell :player1 3))))))))
    (testing "overfull"
      (testing "corner" (is (= (-> (new-board)
                                   (set-cell [0 0] (new-cell :player1 1))
                                   (set-cell [0 1] (new-cell :player1 1))
                                   (set-cell [1 0] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [0 0] (new-cell :player1 3))))))
      (testing "edge"   (is (= (-> (new-board)
                                  (set-cell [0 0] (new-cell :player1 1))
                                   (set-cell [0 1] (new-cell :player1 1))
                                   (set-cell [0 2] (new-cell :player1 1))
                                  (set-cell [1 1] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [0 1] (new-cell :player1 4))))))
      (testing "centre" (is (= (-> (new-board)
                                   (set-cell [0 1] (new-cell :player1 1))
                                   (set-cell [1 0] (new-cell :player1 1))
                                   (set-cell [1 1] (new-cell :player1 3))
                                   (set-cell [1 2] (new-cell :player1 1))
                                   (set-cell [2 1] (new-cell :player1 1)))
                               (explode-full-cells (set-cell (new-board) [1 1] (new-cell :player1 7)))))))
    (testing "chain-reactions"
      (testing "1-step"
        (testing "corner-and-edge"   (is (= (-> (new-board)
                                                (set-cell [0 0] (new-cell :player1 1))
                                                (set-cell [0 2] (new-cell :player1 1))
                                                (set-cell [1 0] (new-cell :player1 1))
                                                (set-cell [1 1] (new-cell :player1 1)))
                                            (explode-full-cells (-> (new-board)
                                                                    (set-cell [0 0] (new-cell :player1 2))
                                                                    (set-cell [0 1] (new-cell :player1 2)))))))
        (testing "edge-and-edge"     (is (= (-> (new-board)
                                                (set-cell [0 0] (new-cell :player1 1))
                                                (set-cell [0 1] (new-cell :player1 1))
                                                (set-cell [0 3] (new-cell :player1 1))
                                                (set-cell [1 1] (new-cell :player1 1))
                                                (set-cell [1 2] (new-cell :player1 1)))
                                            (explode-full-cells (-> (new-board)
                                                                    (set-cell [0 1] (new-cell :player1 3))
                                                                    (set-cell [0 2] (new-cell :player1 2)))))))
        (testing "edge-and-centre"   (is (= (-> (new-board)
                                                (set-cell [0 0] (new-cell :player1 1))
                                                (set-cell [0 1] (new-cell :player1 1))
                                                (set-cell [0 2] (new-cell :player1 1))
                                                (set-cell [1 0] (new-cell :player1 1))
                                                (set-cell [1 2] (new-cell :player1 1))
                                                (set-cell [2 1] (new-cell :player1 1)))
                                            (explode-full-cells (-> (new-board)
                                                                    (set-cell [0 1] (new-cell :player1 3))
                                                                    (set-cell [1 1] (new-cell :player1 3)))))))
        (testing "centre-and-centre" (is (= (-> (new-board)
                                                (set-cell [2 3] (new-cell :player1 1))
                                                (set-cell [2 4] (new-cell :player1 1))
                                                (set-cell [3 2] (new-cell :player1 1))
                                                (set-cell [3 3] (new-cell :player1 1))
                                                (set-cell [3 5] (new-cell :player1 1))
                                                (set-cell [4 3] (new-cell :player1 1))
                                                (set-cell [4 4] (new-cell :player1 1)))
                                            (explode-full-cells (-> (new-board)
                                                                    (set-cell [3 3] (new-cell :player1 4))
                                                                    (set-cell [3 4] (new-cell :player1 3))))))))

      (testing "2-step")))) ;####TODO!!!!

(deftest legal-move-tests
  (testing "legal-move?"
    (testing "empty-cell"
      (testing "corner" (is (true? (legal-move? (new-board) :player1 [0 0]))))
      (testing "edge"   (is (true? (legal-move? (new-board) :player1 [7 6]))))
      (testing "centre" (is (true? (legal-move? (new-board) :player1 [2 2])))))
    (testing "non-empty-cell"
      (testing "own-cell"            (is (true?  (legal-move? (set-cell (new-board) [0 0] (new-cell :player1 1)) :player1 [0 0]))))
      (testing "other-player's-cell" (is (false? (legal-move? (set-cell (new-board) [4 4] (new-cell :player1 1)) :player2 [4 4])))))))

(deftest place-piece-tests
  (testing "place-piece"
    (testing "empty-board"
      (testing "corner" (is (= (-> (new-board)
                                   (set-cell [0 0] (new-cell :player1 1)))
                               (place-piece (new-board) :player1 [0 0]))))
      (testing "edge"   (is (= (-> (new-board)
                                   (set-cell [7 6] (new-cell :player1 1)))
                               (place-piece (new-board) :player1 [7 6]))))
      (testing "centre" (is (= (-> (new-board)
                                   (set-cell [4 2] (new-cell :player1 1)))
                               (place-piece (new-board) :player1 [4 2])))))
    (testing "populated-board"
      (testing "legal-move"
        (testing "corner" (is (= (-> (new-board)
                                     (set-cell [0 0] (new-cell :player1 1))
                                     (set-cell [7 7] (new-cell :player2 1)))
                                 (place-piece (set-cell (new-board) [0 0] (new-cell :player1 1)) :player2 [7 7]))))
        (testing "edge"   (is (= (-> (new-board)
                                     (set-cell [0 3] (new-cell :player1 1))
                                     (set-cell [7 5] (new-cell :player2 1)))
                                 (place-piece (set-cell (new-board) [0 3] (new-cell :player1 1)) :player2 [7 5]))))
        (testing "centre" (is (= (-> (new-board)
                                     (set-cell [3 3] (new-cell :player1 1))
                                     (set-cell [3 4] (new-cell :player2 1)))
                                 (place-piece (set-cell (new-board) [3 3] (new-cell :player1 1)) :player2 [3 4])))))
      (testing "illegal-move"))))   ;####TODO!!!!
