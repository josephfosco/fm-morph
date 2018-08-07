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

(ns fm-morph.synth-util
  (:require
   [clojure.java.io :refer [reader writer]]
   [clojure.pprint :refer [pprint]]
   [overtone.live :refer :all]
   [fm-morph.settings :as settings]
   [fm-morph.synth :refer [cntl-buses cntl-synths env-synths mod-lvl-synths]]
   )
  )

(defn reset-cbuses
  []
  (dotimes [oper settings/num-operators]
    (let [parms (settings/cntl-parms oper)]
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
         :env-d-l (:env-d-l parms)
         :env-s-l (:env-s-l parms)
         :env-dly-t (:env-dly-t parms)
         :env-a-t (:env-a-t parms)
         :env-d-t (:env-d-t parms)
         :env-r-t (:env-r-t parms)
         :env-a-c (:env-a-c parms)
         :env-d-c (:env-d-c parms)
         :env-r-c (:env-r-c parms)
         )
    (ctl (cntl-synths oper)
         :env-bias (or (:env-bias parms) 0)
         :freq-ratio (:freq-ratio parms)
         :volume (:vol parms)
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
         :env-a-t (nth bus-vals (+ settings/base-env-bus-ndx 3))
         :env-d-t (nth bus-vals (+ settings/base-env-bus-ndx 4))
         :env-r-t (nth bus-vals (+ settings/base-env-bus-ndx 5))
         :env-a-c (nth bus-vals (+ settings/base-env-bus-ndx 6))
         :env-d-c (nth bus-vals (+ settings/base-env-bus-ndx 7))
         :env-r-c (nth bus-vals (+ settings/base-env-bus-ndx 8))
         :env-bias (nth bus-vals (+ settings/base-cntl-bus-ndx 0))
         :freq-ratio (nth bus-vals (+ settings/base-cntl-bus-ndx 1))
         :vol (nth bus-vals (+ settings/base-cntl-bus-ndx 2))
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

(defn load-synth
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
