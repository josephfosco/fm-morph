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

(ns fm-morph.synth
  (:require
   [overtone.live :refer :all]
   )
  )

(defonce fm-main-g (group "fm-main"))
(defonce fm-early-g (group "fm early" :head fm-main-g))
(defonce fm-later-g (group "fm later" :after fm-early-g))

(defonce main-audio-bus (audio-bus 1 "fm-audio-bus"))

(defsynth main-out-synth
  []
  (out [0 1]
       (limiter (in main-audio-bus) 0.9 0.01)
       )
  )

(def fm-main-out (main-out-synth [:tail fm-later-g]))

(def num-operators 8)
(def num-cntl-buses 10)

(defonce fm-mod-buses (vec (for [i (range num-operators)]
                              (audio-bus 1 (str "fm-mod-bus" i)))))
(defonce feedback-buses (vec (for [i (range num-operators)]
                                (audio-bus 1 (str "feedback-bus" i)))))

(defsynth feedback-synth
  [inbus 3 outbus 3]
  (let [input (in-feedback:ar inbus)]
    (out:ar outbus input)
    ))

;; feedback-synths move audio data from the fm-mod-busses to the
;; feedback-busses. This is done so that all operators can access the output
;; of all other operators (on the feedback-busses). Without this, an
;; individual operator would only be able to use the output of operators
;; defined after it was. This works because feedback-synth uses the
;; in-feedback ugen.
(def feedback-synths (vec (for [i (range num-operators)]
                        (feedback-synth [:head fm-early-g]
                                        :inbus (fm-mod-buses i)
                                        :outbus (feedback-buses i))
                        )))

(defonce base-freq-bus (control-bus 1 "base-freq-bus"))
(control-bus-set! base-freq-bus 110)

;; Creates a vector of num-operators vectors with each internal vector
;; having num-cntl-buses control-buses
(def cntl-buses
  (vec (for [opr (range num-operators)]
         (control-bus num-cntl-buses (str opr "-"))
         ))
  )

(defsynth mod-lvls-synth
  [
   out-bus 0
   out-mod-lvl0 0
   out-mod-lvl1 0
   out-mod-lvl2 0
   out-mod-lvl3 0
   out-mod-lvl4 0
   out-mod-lvl5 0
   out-mod-lvl6 0
   out-mod-lvl7 0
   morph-time 1
   ]
  (let [
        o-ml0 (lag3:kr out-mod-lvl0 morph-time)
        o-ml1 (lag3:kr out-mod-lvl1 morph-time)
        o-ml2 (lag3:kr out-mod-lvl2 morph-time)
        o-ml3 (lag3:kr out-mod-lvl3 morph-time)
        o-ml4 (lag3:kr out-mod-lvl4 morph-time)
        o-ml5 (lag3:kr out-mod-lvl5 morph-time)
        o-ml6 (lag3:kr out-mod-lvl6 morph-time)
        o-ml7 (lag3:kr out-mod-lvl7 morph-time)
        ]
    (out:kr out-bus [o-ml0 o-ml1 o-ml2 o-ml3 o-ml4 o-ml5 o-ml6 o-ml7])
    )
  )

(defsynth cntl-synth
  [
   out-bus-num 0
   freq-ratio 1
   volume 1
   morph-time 1
   ]
  (let [
        fr (lag3:kr freq-ratio morph-time)
        vol (lag3:kr volume morph-time)
        ]
    (out:kr out-bus-num [fr vol])
    )
  )

(def cntl-parms [
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 1 :vol 1
                  }
                 {:out-mod-lvl0 500 :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 2 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 400
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 3 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 300 :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 4 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 200
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 5 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 100 :out-mod-lvl5 0
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 6 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 75
                  :out-mod-lvl6 0   :out-mod-lvl7 0
                  :freq-ratio 7 :vol 0
                  }
                 {:out-mod-lvl0 0   :out-mod-lvl1 0
                  :out-mod-lvl2 0   :out-mod-lvl3 0
                  :out-mod-lvl4 0   :out-mod-lvl5 0
                  :out-mod-lvl6 50  :out-mod-lvl7 0
                  :freq-ratio 8 :vol 0
                  }
                ])

; These indexes must match the order of the control-buses The specific -ndx(s)
;; are used in the various control synths to set a base for their outputs
(def base-mod-lvl-bus-ndx 0)
(def base-cntl-bus-ndx 8)

(def mod-lvl-synths
  (vec (for [i (range num-operators)]
         (let [parms (cntl-parms i)]
           (mod-lvls-synth [:head fm-early-g]
                       (cntl-buses i)
                       (or (:out-mod-lvl0 parms) 0)
                       (or (:out-mod-lvl1 parms) 0)
                       (or (:out-mod-lvl2 parms) 0)
                       (or (:out-mod-lvl3 parms) 0)
                       (or (:out-mod-lvl4 parms) 0)
                       (or (:out-mod-lvl5 parms) 0)
                       (or (:out-mod-lvl6 parms) 0)
                       (or (:out-mod-lvl7 parms) 0)
                       ))
         ))
  )

(def cntl-synths
  (vec (for [i (range num-operators)]
         (let [parms (cntl-parms i)
               cntl-bus-num (+ (:id (cntl-buses i)) base-cntl-bus-ndx)]
           (cntl-synth [:head fm-early-g]
                       cntl-bus-num
                       (:freq-ratio parms)
                       (:vol parms)
                       ))
         ))
  )

(defsynth fm-oper
  [
   in-mod-bus 3
   out-mod-bus 2
   cntl-bus 7
   action NO-ACTION
   gate 0
   ]
  (let [
        [mod-lvl0
         mod-lvl1
         mod-lvl2
         mod-lvl3
         mod-lvl4
         mod-lvl5
         mod-lvl6
         mod-lvl7
         freq-ratio
         vol
         ] (in:kr cntl-bus num-cntl-buses)
        envelope (env-gen (perc 5.0 5.0) gate 1 0 1 action)
        out-osc (* (sin-osc :freq (+ (* (in:kr base-freq-bus) freq-ratio)
                                     (in:ar in-mod-bus)))
                   envelope
                   )
        ]
    (out:ar out-mod-bus
            [
             (* out-osc mod-lvl0)
             (* out-osc mod-lvl1)
             (* out-osc mod-lvl2)
             (* out-osc mod-lvl3)
             (* out-osc mod-lvl4)
             (* out-osc mod-lvl5)
             (* out-osc mod-lvl6)
             (* out-osc mod-lvl7)
             ])
    (out:ar main-audio-bus (* out-osc (/ vol num-operators)))
    ))

(def fm-voice
  (for [oper-id (range num-operators)]
      (fm-oper [:tail fm-early-g]
               :in-mod-bus (feedback-buses oper-id)
               :out-mod-bus (fm-mod-buses 0)
               :cntl-bus (cntl-buses oper-id)
               )
    ))

(doseq [oper fm-voice]
  (ctl oper :gate 1 :action FREE)
  )

(ctl (cntl-synths 0) :freq-ratio 1)
(ctl (cntl-synths 0) :out-mod-lvl 0)
(ctl (cntl-synths 0) :volume 0)
(ctl (cntl-synths 1) :freq-ratio 1.42)
(ctl (cntl-synths 1) :out-mod-lvl0 500)
(ctl (cntl-synths 2) :volume 1)
(ctl (mod-lvl-synths 2) :out-mod-lvl1 500.00)
(ctl (mod-lvl-synths 7) :out-mod-lvl6 50.0)
(control-bus-get ((cntl-buses 0) 0))
(control-bus-get ((cntl-buses 0) 1))
(control-bus-get ((cntl-buses 0) 2))
(control-bus-get ((cntl-buses 1) 0))
(control-bus-get ((cntl-buses 1) 1))
(control-bus-get ((cntl-buses 1) 2))
(control-bus-set! base-freq-bus 110)
(stop)
