;;; Copyright (c) Provisdom Inc.
;;; All rights reserved
(ns provisdom.tapic)

(defn ttap>
  "Send the value 'v' to the topic function with key 'topic'."
  [topic value]
  (tap> {::topic topic ::value value}))

(defonce ^:private ttaps (atom {}))

(defn- route-topic
  [{::keys [topic value]}]
  (when-let [f (@ttaps topic)]
    (f value)))

(defn swap-ttap
  "Replaces the tap function for 'topic' with 'f'."
  [topic f]
  (when (empty? @ttaps)
    (add-tap route-topic))
  (swap! ttaps assoc topic f)
  nil)

(defn remove-ttap
  "Removes the tap function for 'topic'."
  [topic]
  (swap! ttaps dissoc topic)
  (when (empty? @ttaps)
    (remove-tap route-topic))
  nil)

(defn clear-ttaps
  "Clears taps for all topics. Non-topic taps are unaffected."
  []
  (reset! ttaps {})
  (remove-tap route-topic)
  nil)
