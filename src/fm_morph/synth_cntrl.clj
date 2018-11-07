;    Copyright (C) 2018  Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns fm-morph.synth-cntrl
  (:require
   [overtone.live :refer :all]
   [fm-morph.settings :as settings]
   [fm-morph.synth :as sy]
   )
  )

(def cntl-bus-vals (vec (for [o (range settings/num-operators)]
                          (vec (for [b (range settings/num-cntl-buses)] (atom 0))))))

(defn set-cntl-bus-vals
  []
  (for [oper (range settings/num-operators)]
    (for [cbus (range settings/num-cntl-buses)]
      (reset! ((cntl-bus-vals oper) cbus)
              (control-bus-get (+ (:id (sy/cntl-buses oper))) cbus)
              )))
  )

(def fm-voice
  (for [oper-id (range settings/num-operators)]
      (sy/fm-oper [:tail sy/fm-early-g]
               :in-mod-bus (sy/feedback-buses oper-id)
               :out-mod-bus (sy/fm-mod-buses 0)
               :cntl-bus (sy/cntl-buses oper-id)
               )
    ))

(defn trigger-synth
  [trigger-val]
  (println "triggering synth val:" trigger-val)
  (doseq [oper fm-voice]
    (ctl oper :gate trigger-val)
    )
  )

(defn round-dec
 [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)
    ))

(defn scale-mod-lvl
  "Scales raw values from -50 to 50 to -5 to 5"
  [val]
  (/ val 10)
  )

(defn change-mod-lvl
     [oper mod-to-oper change-amt]
     (let [cntl-bus-val ((cntl-bus-vals oper)
                         (+ mod-to-oper settings/base-mod-lvl-bus-ndx))
           cur @cntl-bus-val
           new-mod-lvl (reset!
                        cntl-bus-val
                        (round-dec 1 (+ (scale-mod-lvl change-amt)
                                        @cntl-bus-val)))
           ]
       (println (str "cur-mod-lvl " cur " out-mod-lvl" mod-to-oper) ": " new-mod-lvl)
       (ctl (sy/mod-lvl-synths oper)
            (keyword (str "out-mod-lvl" mod-to-oper))
            new-mod-lvl)
       )
     )

(defn scale-vol-lvl
  "Scales raw values from -50 to 50 to -0.5 to 0.5"
  [val]
  (/ val 100)
  )

(defn change-vol-lvl
     [oper change-amt]
     (let [cntl-bus-val ((cntl-bus-vals oper)
                         (+ settings/cntrl-vol-ndx settings/base-cntrl-bus-ndx))
           new-mod-lvl (reset!
                        cntl-bus-val
                        (round-dec 2 (+ (scale-vol-lvl change-amt)
                                        @cntl-bus-val)))
           ]
       (println (str "out-vol-lvl") ": " new-mod-lvl)
       (ctl (sy/cntl-synths oper) :volume new-mod-lvl)
       )
     )

(defn scale-ratio
  "Scales raw values from -50 to 50 to -5 to 5"
  [val]
  (/ val 10)
  )

(defn change-ratio
     [oper change-amt]
     (let [cntl-bus-val ((cntl-bus-vals oper)
                         (+ settings/cntrl-ratio-ndx
                            settings/base-cntrl-bus-ndx))
           new-mod-lvl (reset!
                        cntl-bus-val
                        (round-dec 1 (+ (scale-ratio change-amt)
                                        @cntl-bus-val)))
           ]
       (println (str "out-ratio") ": " new-mod-lvl)
       (ctl (sy/cntl-synths oper) :freq-ratio new-mod-lvl)
       )
     )

(defn scale-env-a-t
  "Scales raw values from -50 to 50 to -5 to 5"
  [val]
  (/ val 10)
  )

(defn change-env-attack
     [oper change-amt]
     (let [env-bus-val ((cntl-bus-vals oper)
                        (+ settings/env-a-t-ndx
                           settings/base-env-bus-ndx))
           new-a-t-lvl (reset!
                        env-bus-val
                        (max 0
                             (round-dec 1 (+ (scale-env-a-t change-amt)
                                             @env-bus-val))))
           ]
       (println "env-a-t:" new-a-t-lvl)
       (ctl (sy/env-synths oper) :env-a-t new-a-t-lvl)
       )
     )

(defn scale-env-r-t
  "Scales raw values from -50 to 50 to -5 to 5"
  [val]
  (/ val 10)
  )

(defn change-env-release
     [oper change-amt]
     (let [env-bus-val ((cntl-bus-vals oper)
                        (+ settings/env-r-t-ndx
                           settings/base-env-bus-ndx))
           new-r-t-lvl (reset!
                        env-bus-val
                        (max 0
                             (round-dec 1 (+ (scale-env-r-t change-amt)
                                             @env-bus-val))))
           ]
       (println "env-r-t:" new-r-t-lvl)
       (ctl (sy/env-synths oper) :env-r-t new-r-t-lvl)
       )
     )

(defn process-cntrlr-input
  [bank btn val]
  (cond
    (and (< bank 4) (< btn 4)) (change-mod-lvl bank btn val)
    (and (< bank 4) (= btn 4)) (change-vol-lvl bank val)
    (and (< bank 4) (= btn 5)) (change-ratio bank val)
    (and (< bank 4) (= btn 6)) (change-env-attack bank val)
    (and (< bank 4) (= btn 7)) (change-env-release bank val)
    (= 9 bank btn) (trigger-synth val)
    :else (println "ERROR: Invalid controller values - bank:" bank, "btn:" btn, "val:" val)
    )
  )


;; (doseq [oper fm-voice]
;;   (ctl oper :gate 1 :action FREE)
;;   )

;; (doseq [oper fm-voice]
;;   (ctl oper :gate 1)
;;   )

;; (doseq [oper fm-voice]
;;   (ctl oper :gate 0)
;;   )

;; (doseq [synth sy/env-synths]
;;   (ctl synth :env-r-t 6.0)
;;   )

;; (ctl (cntl-synths 5) :freq-ratio 3.6)
;; (ctl (cntl-synths 7) :volume 1)
;; (ctl (cntl-synths 1) :env-bias -0.50)

;; (ctl (env-synths 7) :env-dly-t 0.5)
;; (ctl (env-synths 7) :env-d-l 0.4)
;; (ctl (env-synths 7) :env-d-t 1.8)
;; (ctl (env-synths 7) :env-a-t 0.01)
;; (ctl (env-synths 7) :env-d-c -5.0)

;; (ctl (sy/mod-lvl-synths 2) :out-mod-lvl1 2.00)
;; (ctl (sy/mod-lvl-synths 6) :out-mod-lvl1 400.0)
;; (control-bus-set! sy/base-freq-bus 220)
;; (stop)
