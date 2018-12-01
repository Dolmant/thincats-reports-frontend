(ns reports.pages.Home
  (:require
   [rum.core :as rum]
   [reports.wrappers.ui :as ui]
   [reports.State :as State]))

(rum/defc Home < rum/reactive []
  [:div
   [:h1 "Reports"]
   [:button {:on-click (fn [e] (State/GetReport "investor-balance"))} "Investor balance"]
   [:h1 "Loan Data"]
   (ui/Paper
    (ui/Table
     (ui/TableHead
      (ui/TableRow
       (map (fn [item] (ui/TableCell (item :value))) (keys (first ((rum/react State/State) :loans))))))
     (ui/TableBody
      (map (fn [item] (ui/TableRow
                       (map (fn [cols] (ui/TableRow cols)) (vals item))))
           ((rum/react State/State) :loans)))))
   [:h1 "Investor Data"]
   (ui/Paper
    (ui/Table
     (ui/TableHead
      (ui/TableRow
       (map (fn [item] (ui/TableCell (item :value))) (keys (first ((rum/react State/State) :investors))))))
     (ui/TableBody
      (map (fn [item] (ui/TableRow
                       (map (fn [cols] (ui/TableRow cols)) (vals item))))
           ((rum/react State/State) :investors)))))])
