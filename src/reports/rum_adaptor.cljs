(ns reports.rum-adaptor
  (:refer-clojure :exclude [list stepper])
  (:require
   [sablono.util :refer [camel-case-keys]])
  (:require-macros [reports.rum-adaptor]))

(defn create-mui-cmp
  ([react-class args]
   (let [first-arg (first args)
         args (if (or (map? first-arg) (nil? first-arg)) args (cons {} args))]
     (apply js/React.createElement react-class
            ((comp clj->js camel-case-keys) (first args)) (rest args))))
  ([root-obj type args]
   (create-mui-cmp (aget root-obj type) args)))
