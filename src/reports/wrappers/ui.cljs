(ns reports.wrappers.ui
  (:refer-clojure :exclude [list stepper])
  (:require
   [rum.core :as rum]
   [reports.rum-adaptor :refer (adapt-rum-class)]
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/Hidden" :as hidden]
   ["@material-ui/core/Divider" :as divider]
   ["@material-ui/core/Input" :as input]
   ["@material-ui/core/InputLabel" :as inputLabel]
   ["@material-ui/core/TableRow" :as tableRow]
   ["@material-ui/core/Paper" :as paper]
   ["@material-ui/core/TableCell" :as tableCell]
   ["@material-ui/core/Table" :as table]
   ["@material-ui/core/TableHead" :as tableHead]
   ["@material-ui/core/TableBody" :as tableBody]
   ["@material-ui/core/styles/MuiThemeProvider" :as muiThemeProvider]
   ["@material-ui/core/styles/createMuiTheme" :as createMuiTheme]
   ["@material-ui/core/Select" :as select]
   ["@material-ui/core/TextField" :as textField]
   [clojure.walk :refer [postwalk]]
   [clojure.string :as str]
   [sablono.util :refer [camel-case]]))

(defn transform-keys [t coll]
  "Recursively transforms all map keys in coll with t."
  (letfn [(transform [[k v]] [(t k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))

(defn get-mui-theme
  ([]  (get-mui-theme (createMuiTheme/default (js-obj))))
  ([raw-theme]  (->> raw-theme
                     (transform-keys camel-case)
                     clj->js
                     createMuiTheme/default)))

(def Button (adapt-rum-class button/default))
(def Divider (adapt-rum-class divider/default))
(def Hidden (adapt-rum-class hidden/default))
(def Input (adapt-rum-class input/default))
(def Input-label (adapt-rum-class inputLabel/default))
(def Table (adapt-rum-class table/default))
(def TableRow (adapt-rum-class tableRow/default))
(def TableCell (adapt-rum-class tableCell/default))
(def TableBody (adapt-rum-class tableBody/default))
(def TableHead (adapt-rum-class tableHead/default))
(def Paper (adapt-rum-class paper/default))
(def MuiThemeProvider (adapt-rum-class muiThemeProvider/default))
(def Select (adapt-rum-class select/default))
(def TextField (adapt-rum-class textField/default))
