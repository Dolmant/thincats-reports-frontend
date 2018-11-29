(ns reports.wrappers.ui
  (:refer-clojure :exclude [list stepper])
  (:require-macros [reports.rum-adaptor-macro])
  (:require
   [rum.core :as rum]
   ["@material-ui/core/AppBar" :as AppBar]
   ["@material-ui/core/DialogTitle" :as DialogTitle]
   ["@material-ui/core/Dialog" :as Dialog]
   ["@material-ui/core/DialogContent" :as DialogContent]
   ["@material-ui/core/DialogContent" :as DialogContent]
   ["@material-ui/core/DialogActions" :as DialogActions]
   ["@material-ui/core/Toolbar" :as Toolbar]
   ["@material-ui/core/Checkbox" :as Checkbox]
   ["@material-ui/core/Button" :as Button]
   ["@material-ui/core/Hidden" :as Hidden]
   ["@material-ui/core/FormHelperText" :as FormHelperText]
   ["@material-ui/core/Grid" :as Grid]
   ["@material-ui/core/IconButton" :as IconButton]
   ["@material-ui/core/FormControl" :as FormControl]
   ["@material-ui/core/Drawer" :as Drawer]
   ["@material-ui/core/Divider" :as Divider]
   ["@material-ui/core/Input" :as Input]
   ["@material-ui/core/InputLabel" :as InputLabel]
   ["@material-ui/core/Tab" :as Tab]
   ["@material-ui/core/Tabs" :as Tabs]
   ["@material-ui/core/Paper" :as Paper]
   ["@material-ui/core/LinearProgress" :as LinearProgress]
   ["@material-ui/core/Menu" :as Menu]
   ["@material-ui/core/Menu" :as Menu]
   ["@material-ui/core/MenuItem" :as MenuItem]
   ["@material-ui/core/styles/MuiThemeProvider" :as MuiThemeProvider]
   ["@material-ui/core/styles/createMuiTheme" :as createMuiTheme]
   ["react-jss/lib/JssProvider" :as JssProvider]
   ["react-jss/lib/jss" :as SheetsRegistry]
   ["@material-ui/core/Select" :as Select]
   ["@material-ui/core/SvgIcon" :as SvgIcon]
   ["@material-ui/core/TextField" :as TextField]
   [clojure.walk :refer [postwalk]]
   [clojure.string :as str]
   [sablono.util :refer [camel-case]]))
(def JssProviderer (reports.rum-adaptor-macro/adapt-rum-class JssProvider/default))
(def sheetsRegistry (SheetsRegistry/SheetsRegistry.))
; Its important this is generated every render, so no defonce and this must be a function
(defn generateClassName []
  (def counter (atom 0))
  (fn [rule, sheet] (swap! counter inc) (str/join "" ["stable-" (.-key rule) "-" @counter])))

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
(rum/defc get-jss [child]
  (JssProviderer {:registry sheetsRegistry :generateClassName (generateClassName)} child))
(def toolbar (reports.rum-adaptor-macro/adapt-rum-class Toolbar/default))
(def dialog (reports.rum-adaptor-macro/adapt-rum-class Dialog/default))
(def dialog-actions (reports.rum-adaptor-macro/adapt-rum-class DialogActions/default))
(def dialog-content (reports.rum-adaptor-macro/adapt-rum-class DialogContent/default))
(def dialog-title (reports.rum-adaptor-macro/adapt-rum-class DialogTitle/default))
(def appBar (reports.rum-adaptor-macro/adapt-rum-class AppBar/default))
(def button (reports.rum-adaptor-macro/adapt-rum-class Button/default))
(def checkbox (reports.rum-adaptor-macro/adapt-rum-class Checkbox/default))
(def divider (reports.rum-adaptor-macro/adapt-rum-class Divider/default))
(def drawer (reports.rum-adaptor-macro/adapt-rum-class Drawer/default))
(def linear-progress (reports.rum-adaptor-macro/adapt-rum-class LinearProgress/default))
(def form-control (reports.rum-adaptor-macro/adapt-rum-class FormControl/default))
(def hidden (reports.rum-adaptor-macro/adapt-rum-class Hidden/default))
(def form-helper-text (reports.rum-adaptor-macro/adapt-rum-class FormHelperText/default))
(def grid (reports.rum-adaptor-macro/adapt-rum-class Grid/default))
(def icon-button (reports.rum-adaptor-macro/adapt-rum-class IconButton/default))
(def input (reports.rum-adaptor-macro/adapt-rum-class Input/default))
(def input-label (reports.rum-adaptor-macro/adapt-rum-class InputLabel/default))
(def tabs (reports.rum-adaptor-macro/adapt-rum-class Tabs/default))
(def tab (reports.rum-adaptor-macro/adapt-rum-class Tab/default))
(def paper (reports.rum-adaptor-macro/adapt-rum-class Paper/default))
(def menu (reports.rum-adaptor-macro/adapt-rum-class Menu/default))
(def menu-item (reports.rum-adaptor-macro/adapt-rum-class MenuItem/default))
(def mui-theme-provider (reports.rum-adaptor-macro/adapt-rum-class MuiThemeProvider/default))
(def select (reports.rum-adaptor-macro/adapt-rum-class Select/default))
(def svg-icon (reports.rum-adaptor-macro/adapt-rum-class SvgIcon/default))
(def text-field (reports.rum-adaptor-macro/adapt-rum-class TextField/default))
