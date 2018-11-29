(ns reports.rum-adaptor
  (:refer-clojure :exclude [list stepper])
  (:require
   [sablono.util :refer [camel-case-keys]]))

(def props-kebab->camel->js (comp clj->js camel-case-keys))

(defn create-mui-cmp
  ([react-class args]
   (let [first-arg (first args)
         args (if (or (map? first-arg) (nil? first-arg)) args (cons {} args))]
     (apply js/React.createElement react-class
            (props-kebab->camel->js (first args)) (rest args))))
  ([root-obj type args]
   (create-mui-cmp (aget root-obj type) args)))
