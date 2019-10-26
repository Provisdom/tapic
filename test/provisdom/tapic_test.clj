(ns provisdom.tapic-test
  (:require [cognitect.transcriptor :as xr :refer (check!)]
            [provisdom.tapic :as t]))

(def x (atom nil))
(def c (atom 0))

(defn f1
  [x]
  (t/ttap> :f1 x)
  (inc x))

;;; Initial single topic tap
(t/swap-ttap :f1 (fn [v]
                   (reset! x v)
                   (swap! c inc)))
(f1 5)
(check! (fn [_] (= 5 @x)))
(check! (fn [_] (= 1 @c)))

;;; Update single topic tap
(t/swap-ttap :f1 (fn [v]
                   (reset! x (* 2 v))
                   (swap! c inc)))

(reset! c 0)
(f1 5)
(check! (fn [_] (= 10 @x)))
(check! (fn [_] (= 1 @c)))

;;; Remove topic tap
(reset! c 0)
(t/remove-ttap :f1)
(f1 5)
(check! (fn [_] (= 0 @c)))

;;; Tapped message goes to only selected topic
(t/swap-ttap :f1 (fn [v] (swap! x conj :f1)))
(t/swap-ttap :f2 (fn [v] (swap! x conj :f2)))
(reset! x #{})
(defn f2
  [v]
  (t/ttap> :f2 v)
  (dec v))

(f2 5)
(check! (fn [_] (= @x #{:f2})))

;;; Non-topic taps still work
(defn f3
  [v]
  (tap> v)
  (* 2 v))

(reset! c 0)
(defn normal-tap
  [v]
  (reset! x v))
(add-tap normal-tap)
(t/swap-ttap :f3 (fn [v] (reset! x :f3)))
(f3 9)
(check! (fn [_] (= @x 9)))
(check! (fn [_] (= 0 @c)))

;;; Clear all topic taps

(t/clear-ttaps)
(f1 5)
(f2 5)
(check! (fn [_] (= 0 @c)))

;;; Clean up
(remove-tap normal-tap)
