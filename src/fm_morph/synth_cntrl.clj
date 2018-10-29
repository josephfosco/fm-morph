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

(defn change-mod-lvl
  [oper mod-to-oper lvl-change]
  (println "mod-lvl-change")
  (let [cntl-bus-val ((cntl-bus-vals oper)
                      (+ mod-to-oper settings/base-mod-lvl-bus-ndx))
        new-mod-lvl (reset!
                     cntl-bus-val
                     (+ lvl-change @cntl-bus-val))
        ]
    (ctl (sy/mod-lvl-synths oper)
         (keyword (str "out-mod-lvl" mod-to-oper))
         new-mod-lvl)
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
