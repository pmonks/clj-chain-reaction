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

(ns chain-reaction.core)

(defn new-board
  "Returns a new board (defaults to 8x8 & empty, unless otherwise specified)."
  ([]      (new-board 8 8 nil))
  ([w h]   (new-board w h nil))
  ([w h c] { :width  w
             :height h
             :cells  (if (nil? c) {} c) }))

(defn new-cell
  "Returns a new cell."
  ([] (new-cell nil 0))
  ([owner count]
   (if (pos? count)
     { :owner owner :count count })))

(defn board-width
  "Returns the width of the given board."
  [board]
  (:width board))

(defn board-height
  "Returns the height of the given board."
  [board]
  (:height board))

(defn board-cells
  "Returns the populated cells on the given board."
  [board]
  (:cells board))

(defn legal-coords?
  "Are the given coordinates legal for the given board?"
  [board [x y]]
  (and (>= x 0)
       (<  x (board-width  board))
       (>= y 0)
       (<  y (board-height board))))

; Not clear that this is needed...
(defn all-cell-coords
  "Returns the coordinates of all cells on the given board."
  [board]
  (for [x (range (board-width board)) y (range (board-height board))] [x y]))

(defn occupied-cells
  "Returns a set of the occupied cells on the given board."
  [board]
  (keys (board-cells board)))

(defn players
  "Returns all of the players on the given board."
  [board]
  (set (map :owner (vals (board-cells board)))))

(defn get-cell
  "Returns the given cell on the board."
  [board coords]
  {:pre [ (legal-coords? board coords) ]}
  (when-let [cells (board-cells board)]
    (when-let [cell (get cells coords)]
      cell)))

(defn set-cell
  "Sets the given cell on the board."
  [board coords cell]
  {:pre [ (legal-coords? board coords) ]}
  (if (nil? cell)
    (new-board (board-width  board)
               (board-height board)
               (dissoc (board-cells board) coords))
    (new-board (board-width  board)
               (board-height board)
               (assoc (board-cells board) coords cell))))

(defn cell-owner
  "Returns the owner of the given cell (nil if unowned)."
  ([board coords] (cell-owner (get-cell board coords)))
  ([cell]         (:owner cell)))

(defn cell-unowned?
  "Is the given cell unowned?"
  ([board coords] (cell-unowned? (get-cell board coords)))
  ([cell] (nil? (cell-owner cell))))

(defn cell-count
  "Returns the number of items in the given cell."
  ([board coords]
   (cell-count (get-cell board coords)))
  ([cell]
   (if (nil? cell)
    0
    (:count cell))))

(defn number-of-neighbours
  "Returns the number of neighbours the given cell has."
  [board [x y]]
  { :pre [ (legal-coords? board [x y]) ]}
  (- 4 (if (= x 0)                          1 0)
       (if (= x (dec (board-width board)))  1 0)
       (if (= y 0)                          1 0)
       (if (= y (dec (board-height board))) 1 0)))

(defn neighbours
  "Returns the neighbours of the given cell."
  [board [x y]]
  { :pre [ (legal-coords? board [x y]) ]}
  (filter #(legal-coords? board %) [[x       (dec y)]
                                    [(dec x) y]
                                    [(inc x) y]
                                    [x       (inc y)]]))

(defn full?
  "Is the given cell full?"
  [board coords]
  (>= (cell-count board coords) (number-of-neighbours board coords)))

(defn any-full-cells?
  "Are there any full cells on the board?"
  [board]
  (boolean (some true? (map #(full? board %) (occupied-cells board)))))

(defn find-first-full-cell
  "Find the first full cell on the board, or nil if there aren't any."
  [board]
  (let [cell-coords (occupied-cells board)]
    (loop [board         board
           cell-to-check (first cell-coords)
           other-cells   (rest  cell-coords)]
      (when-not (nil? cell-to-check)
        (if (full? board cell-to-check)
          cell-to-check
          (recur board (first other-cells) (rest other-cells)))))))

(defn explode-full-cell
  "Explode the given full cell (which must be full)."
  [board coords]
  {:pre [ (full? board coords) ]}
  (let [owner          (cell-owner           board coords)
        ccount         (cell-count           board coords)
        num-neighbours (number-of-neighbours board coords)
        neighbours     (neighbours           board coords)]
    (loop [board             (set-cell board coords (new-cell owner (- ccount num-neighbours)))
           current-neighbour (first neighbours)
           other-neighbours  (rest  neighbours)]
      (if (nil? current-neighbour)
        board
        (recur (set-cell board
                         current-neighbour
                         (new-cell owner
                                   (inc
                                      (cell-count board current-neighbour))))
               (first other-neighbours)
               (rest  other-neighbours))))))

(defn explode-full-cells
  "Recursively explodes any full cells in the board until there are no full cells on the board."
  [board]
  (loop [board board]
    (if (any-full-cells? board)
      (recur (explode-full-cell board (find-first-full-cell board)))
      board)))

(defn legal-move?
  "Is the given move legal?"
  [board player coords]
  (if (legal-coords? board coords)
    (let [cell (get-cell board coords)]
      (or (cell-unowned? cell)
          (= player (cell-owner cell))))
    false))

(defn place-piece
  "Place a piece at the given location on the board, returning the new board."
  [board player coords]
  {:pre [ (legal-move? board player coords) ]}
  (let [new-board (new-board (board-width  board)
                             (board-height board)
                             (assoc (board-cells board) coords (new-cell player (inc (cell-count board coords)))))]
    (explode-full-cells new-board)))

