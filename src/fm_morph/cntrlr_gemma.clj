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

(ns fm-morph.cntrlr-gemma
  (:require
   [serial.core :refer :all]
   [serial.util :refer :all]
   ;; [serial-port :refer :all]
   [fm-morph.cntrlr-synth-interface :refer [cntrlr-synth-interface]]
   )
  (:import (java.io InputStream))
  )

(def cs-intrf (cntrlr-synth-interface))

(defn cntrlr-ports []
  (list-ports))

(def pot-total (atom 0))


(defn process-port-input
  [msg]
  (println "msg: " msg)
  (let [bank (Integer. (subs msg 0 1))
        btn (Integer. (subs msg 1 2))
        pot-val (Integer. (subs msg 2))
        ]
    (reset! pot-total (+ @pot-total pot-val))
    (println "bank: " bank)
    (println "btn: " btn)
    (println "pot-val: " pot-val)
    (println "pot-total:" @pot-total)
    (println " ")
    )
  )

(defn cntrlr-listen
  [&{:keys [port] :or {port "ttyACM0"}}]
  (let [msg-size 6
        gemma-port (open port)]
    ;; (listen gemma-port #(process-port-input (.read %)))
    (listen! gemma-port (fn [^InputStream in-stream]
                          (println "in avail: " (.available in-stream))
                          (while (< (.available in-stream) msg-size)
                            nil
                            )
                          (process-port-input
                           (apply str
                                  (doall (repeatedly
                                          msg-size
                                          #(char (.read in-stream))))))
                          )
            )
    gemma-port
    )
  )

(defn cntrlr-close
 [port]
 (close! port)
  )
