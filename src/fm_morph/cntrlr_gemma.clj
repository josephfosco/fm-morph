;    Copyright (C) 2016  Joseph Fosco. All Rights Reserved
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
;;   [serial-port :refer :all]
   )
  )

(defn cntrlr-ports []
  (list-ports))

(defn process-port-input
  [val]
  (when (= val \0)
    (println "RESET"))
  (println val)
  )

(defn cntrlr-listen
  [&{:keys [port] :or {port "ttyACM0"}}]
  (let [gemma-port (open port)]
    (listen gemma-port #(process-port-input (char (int (.read %)))))
    gemma-port
    )
  )

(defn cntrlr-close
 [port]
 (close! port)
  )
