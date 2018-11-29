(ns reports.wrappers.ic
  (:require
   [reports.rum-adaptor :as m]
   ["@material-ui/icons/ArrowDropDownCircle" :as ArrowDropDownCircle]
   ["@material-ui/icons/ChevronLeft" :as ChevronLeft]
   ["@material-ui/icons/ChevronRight" :as ChevronRight]
   ["@material-ui/icons/KeyboardArrowUp" :as KeyboardArrowUp]
   ["@material-ui/icons/KeyboardArrowDown" :as KeyboardArrowDown]
   ["@material-ui/icons/Add" :as Add]
   ["@material-ui/icons/Remove" :as Remove]
   ["@material-ui/icons/ShoppingCart" :as ShoppingCart]
   ["@material-ui/icons/ExpandMore" :as ExpandMore]))

(defn arrowDropDownCircle [& args] (m/create-mui-cmp ArrowDropDownCircle/default args))
(defn chevronLeft [& args] (m/create-mui-cmp ChevronLeft/default args))
(defn chevronRight [& args] (m/create-mui-cmp ChevronRight/default args))
(defn keyboardArrowUp [& args] (m/create-mui-cmp KeyboardArrowUp/default args))
(defn keyboardArrowDown [& args] (m/create-mui-cmp KeyboardArrowDown/default args))
(defn addIcon [& args] (m/create-mui-cmp Add/default args))
(defn removeIcon [& args] (m/create-mui-cmp Remove/default args))
(defn expandMoreIcon [& args] (m/create-mui-cmp ExpandMore/default args))
(defn shoppingCartIcon [& args] (m/create-mui-cmp ShoppingCart/default args))
