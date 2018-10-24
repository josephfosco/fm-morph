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

(ns fm-morph.cntrlr-synth-interface
  (:require [fm-morph.synth-cntrl :refer [trigger-synth]])
  )

(defn process-cntrlr-input
  [bank btn val]
  (cond
    (= 9 bank btn) (trigger-synth val)
    :else (println "ERROR: Invalid controller values - bank:" bank, "btn:" btn, "val:" val)
    )
  )



(defn cntrlr-synth-interface
  []
  (let [
        current-trigger-val (atom 0)
        toggle-trigger (fn [trig-val] (if (= trig-val 0)
                               1
                               0)
                         )
        process-cntrlr-input(fn [cntrlr-val]
                              (trigger-synth (swap! current-trigger-val
                                                    toggle-trigger))
                              )
        ]
    (fn [m]
      (cond
        (= m :process-cntrlr-input) process-cntrlr-input
        )
      )
    )
  )
