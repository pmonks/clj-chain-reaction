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

(defproject org.clojars.pmonks/clj-chain-reaction "0.1.0-SNAPSHOT"
  :description      "Clojure/Script implementation of Chain Reaction board game."
  :url              "https://github.com/pmonks/clj-chain-reaction"
  :license          {:spdx-license-identifier "Apache-2.0"
                     :name                    "Apache License, Version 2.0"
                     :url                     "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version "2.9.0"
  :dependencies     [[org.clojure/clojure       "1.10.1"]
                     [org.clojure/clojurescript "1.10.520"]]
  :cljsbuild        {:builds        {}
                     :test-commands {"unit-tests" ["lein" "with-profile" "test" "doo" "node" "once"]}}
  :doo              {:build "test"}
  :profiles         {:dev  {:plugins [[lein-cljsbuild "1.1.7"]
                                      [lein-doo       "0.1.11"]]}
                     :test {:cljsbuild {:builds {:test {:source-paths  ["src" "test"]
                                                        :compiler      {:output-to "unit-tests.js"
                                                                        :target    :nodejs
                                                                        :main      chain-reaction.doo-runner}
                                                        :optimizations :whitespace
                                                        :pretty-print  true}}}}})
