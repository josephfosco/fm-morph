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

(ns fm-morph.core
  (:gen-class)
  (:require
   [overtone.live :refer :all]
   [fm-morph.cntrlr-gemma :refer [cntrlr-close cntrlr-listen]]
   [fm-morph.synth-cntrl :refer [set-cntl-bus-vals]]
   [fm-morph.synth-utils :refer :all]
   ))

(def cntrlr-port (atom nil))

(defn open-cntrlr
  []
  (if (not @cntrlr-port)
    (do
      (reset! cntrlr-port (cntrlr-listen))
      (set-cntl-bus-vals)
      )
    (println "ERROR: Controller port already opened")
    )
  )

(defn close-cntrlr
  []
  (if @cntrlr-port
    (do
      (cntrlr-close @cntrlr-port)
      (reset! cntrlr-port nil))
    (println "ERROR: Comtroller port not open")
    )
  )
