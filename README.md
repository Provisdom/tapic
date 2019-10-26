# tapic

The `tap>` functionality introduced in Clojure 1.10 has proven
very useful for debugging. However, I find myself using the same
pattern most of the time, which is the following:

* Send Clojure maps with a common `:tag` or `:topic` attribute
* Add tap functions which filter on a specific value of that attribute

Or in code:

```clj

(defn f1
  [x]
  (tap> :f1 {:tag :f1 :data x})
  ...)

...

(comment
  (defn f1-tap
    [x]
    (when (= :f1 (:tag x))
      <do something with x>))

  (add-tap f1-tap)
  (remove-tap f1-tap)

  <... more taps for other topics ...>
)
```

I'll evaluate the code in the comment block by hand in the REPL to activate
my taps. Not terrible, but boilerplatey after awhile. And sometimes I'll want
to change `f1-tap` but forget to remove the original copy before redefining
and adding, and now wind up with two versions receiving tap messages.

tapic is a simple library to support my quick-and-dirty workflow, with the
following features:

* Tap functions have an explicit topic, which can be anything that will
serve as a key in a Clojure map.
* There is only a single tap function for each topic. The function `swap-ttap`
will replace the existing tap (if any) for the topic with the new function.
* The implementation is hidden, i.e. whatever you pass as a value will be
handed directly to your topic tap function, without being cluttered by
bookkeeping stuff such as the `:tag` key above.
* Uses the existing clojure `tap>` functionality, but does not interfere
with it (though without some kind of filtering your "normal" tap functions
will see any raw messages sent via `ttap>`.)

## Usage

```clj
(ns provisdom.tapic-test
  (:require [cognitect.transcriptor :as xr :refer (check!)]
            [provisdom.tapic :as t]))

(def x (atom nil))
(def c (atom 0))

(defn f1
  [x]
  (t/ttap> :f1 x)
  (inc x))

;;; Replace/add tap function for topic :f1
(t/swap-ttap :f1 (fn [v]
                 (reset! x v)
                 (swap! c inc)))
(f1 5)
(check! (fn [_] (= 5 @x)))
(check! (fn [_] (= 1 @c)))

;;; Remove the tap function for topic :f1
(t/remove-ttap :f1)

;;; OR just remove all taps for all topics. Does not affect other tap
;;; functions added with clojure.core/add-tap.
(t/clear-ttaps)
```

## License

Copyright © 2019 Provisdom Corp.

Distributed under the MIT License.
