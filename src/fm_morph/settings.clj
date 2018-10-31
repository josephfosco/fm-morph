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

(ns fm-morph.settings)

(def num-operators 8)
(def num-cntl-buses 20)

;; These indexes must match the order of the control-buses The specific -ndx(s)
;; are used in the various control synths to set a base for their outputs
(def base-mod-lvl-bus-ndx 0)
(def base-env-bus-ndx num-operators)
(def base-cntrl-bus-ndx (+ base-env-bus-ndx 9))

(def cntrl-ratio-ndx 1)
(def cntrl-vol-ndx 2)

(def patch-directory "./patches/")
