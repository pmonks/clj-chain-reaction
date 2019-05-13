[![Build Status](https://travis-ci.com/pmonks/clj-chain-reaction.svg?branch=master)](https://travis-ci.com/pmonks/clj-chain-reaction)
[![Open Issues](https://img.shields.io/github/issues/pmonks/clj-chain-reaction.svg)](https://github.com/pmonks/clj-chain-reaction/issues)
[![License](https://img.shields.io/github/license/pmonks/clj-chain-reaction.svg)](https://github.com/pmonks/clj-chain-reaction/blob/master/LICENSE)
[![Dependencies Status](https://versions.deps.co/pmonks/clj-chain-reaction/status.svg)](https://versions.deps.co/pmonks/clj-chain-reaction)

# clj-chain-reaction

A cross-platform (Clojure & ClojureScript) implementation of the game described [here](http://www.atarimagazines.com/compute/issue80/chain_reaction.php).

## Installation

For now, clj-chain-reaction is provided in source form only, so git cloning!

## Usage

The game itself is implemented in the [`chain-reaction.core`](https://github.com/pmonks/clj-chain-reaction/blob/master/src/chain_reaction/core.clj) namespace,
with the `new-board` and `place-piece` fns being the primary means of playing the game.

Require it in the REPL:

```clojure
(require '[chain-reaction.core :as cr] :reload-all)
```

Require it in your application:

```clojure
(ns my-app.core
  (:require [chain-reaction.core :as cr]))
```

## Developer Information

[GitHub project](https://github.com/pmonks/clj-chain-reaction)

[Bug Tracker](https://github.com/pmonks/clj-chain-reaction/issues)

## License

Copyright © 2015 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
