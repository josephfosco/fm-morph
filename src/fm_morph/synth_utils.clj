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

(ns fm-morph.synth-utils
  (:require
   [clojure.java.io :refer [reader writer]]
   [clojure.pprint :refer [pprint]]
   [overtone.live :refer :all]
   [fm-morph.settings :as settings]
   [fm-morph.synth :refer [cntl-buses cntl-synths env-synths mod-lvl-synths]]
   )
  )

(defn reset-cbuses
  [synth-parms]
  (dotimes [oper settings/num-operators]
    (let [parms (synth-parms oper)]
      (println parms)
      (ctl (mod-lvl-synths oper)
           :out-mod-lvl0 (or (:out-mod-lvl0 parms) 0)
           :out-mod-lvl1 (or (:out-mod-lvl1 parms) 0)
           :out-mod-lvl2 (or (:out-mod-lvl2 parms) 0)
           :out-mod-lvl3 (or (:out-mod-lvl3 parms) 0)
           :out-mod-lvl4 (or (:out-mod-lvl4 parms) 0)
           :out-mod-lvl5 (or (:out-mod-lvl5 parms) 0)
           :out-mod-lvl6 (or (:out-mod-lvl6 parms) 0)
           :out-mod-lvl7 (or (:out-mod-lvl7 parms) 0)
         )
    (ctl (env-synths oper)
         :env-d-l (or (:env-d-l parms) 1.0)
         :env-s-l (or (:env-s-l parms) 0.5)
         :env-dly-t (or (:env-dly-t parms) 0.0)
         :env-a-t (or (:env-a-t parms) 0.3)
         :env-d-t (or (:env-d-t parms) 0.3)
         :env-r-t (or (:env-r-t parms) 0.3)
         :env-a-c (or (:env-a-c parms) 5.0)
         :env-d-c (or (:env-d-c parms) 5.0)
         :env-r-c (or (:env-r-c parms) 5.0)
         )
    (ctl (cntl-synths oper)
         :env-bias (or (:env-bias parms) 0.0)
         :freq-ratio (or (:freq-ratio parms) 1.0)
         :volume (or (:vol parms) 0.0)
         )
      ))
  )

(defn print-cbuses
  []
  (doseq [cb cntl-buses] (println (control-bus-get cb)))
  )

(defn get-synth-values
  []
   (vec
    (for [cntl-bus cntl-buses]
      (let [bus-vals (control-bus-get cntl-bus)]
        {:out-mod-lvl0 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 0))
         :out-mod-lvl1 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 1))
         :out-mod-lvl2 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 2))
         :out-mod-lvl3 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 3))
         :out-mod-lvl4 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 4))
         :out-mod-lvl5 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 5))
         :out-mod-lvl6 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 6))
         :out-mod-lvl7 (nth bus-vals (+ settings/base-mod-lvl-bus-ndx 7))
         :env-d-l (nth bus-vals (+ settings/base-env-bus-ndx 0))
         :env-s-l (nth bus-vals (+ settings/base-env-bus-ndx 1))
         :env-dly-t (nth bus-vals (+ settings/base-env-bus-ndx 2))
         :env-a-t (nth bus-vals (+ settings/base-env-bus-ndx
                                   settings/env-a-t-ndx))
         :env-d-t (nth bus-vals (+ settings/base-env-bus-ndx 4))
         :env-r-t (nth bus-vals (+ settings/base-env-bus-ndx
                                   settings/env-r-t-ndx))
         :env-a-c (nth bus-vals (+ settings/base-env-bus-ndx 6))
         :env-d-c (nth bus-vals (+ settings/base-env-bus-ndx 7))
         :env-r-c (nth bus-vals (+ settings/base-env-bus-ndx 8))
         :env-bias (nth bus-vals (+ settings/base-cntrl-bus-ndx 0))
         :freq-ratio (nth bus-vals (+ settings/base-cntrl-bus-ndx
                                      settings/cntrl-ratio-ndx))
         :vol (nth bus-vals (+ settings/base-cntrl-bus-ndx
                               settings/cntrl-vol-ndx))
         }
        ))
    )
  )

(defn save-synth
  [filename]
  (with-open
    [w (writer (str settings/patch-directory
                     "/"
                     filename
                     ".clj"))
     ]
    (binding [*out* w] (pprint (get-synth-values)))
    )
  )

(defn read-synth
  [filename]
  (with-open
    [r (java.io.PushbackReader.
        (reader (str settings/patch-directory
                     "/"
                     filename
                     ".clj")))
     ]
    (binding [*read-eval* false] (read r))
    )
  )

(defn load-synth
  [filename]
  (reset-cbuses (read-synth filename))
  )
