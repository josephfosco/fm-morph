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

(defn int-from-string
  [int-string]
  (Integer. (re-find #"\d+" int-string))
  )

(defn cntrlr-ports []
  (list-ports))

(defn process-port-input
  [val]
  (let [int-val (int-from-string val)]
    (when (= int-val 0)
      (do
        (println "RESET")
        ((cs-intrf :process-cntrlr-input) int-val)
        ))
       (println int-val))
  )

(defn cntrlr-listen
  [&{:keys [port] :or {port "ttyACM0"}}]
  (let [num-bytes 3
        gemma-port (open port)]
    ;; (listen gemma-port #(process-port-input (.read %)))
    (listen gemma-port (fn [^InputStream in-stream]
                         (if (>= (.available in-stream) num-bytes)
                           (process-port-input
                            (apply str
                                   (doall (repeatedly
                                           num-bytes
                                           #(char (.read in-stream))))))
                           )
                         )
            )
    gemma-port
    )
  )

(defn cntrlr-close
 [port]
 (close! port)
  )
