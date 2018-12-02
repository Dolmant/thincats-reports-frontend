(ns reports.pages.Home
  (:require
   [rum.core :as rum]
   [clojure.string :as str]
   [reports.wrappers.ui :as ui]
   [reports.State :as State]))

(defn logger [item]
  (println item)
  item)

(defn trunc
  [s n]
  (if (> (count s) n)
    (str/join "" [(subs s 0 (min (count s) n)) "..."])
    s))

(rum/defcs TextCell < (rum/local false ::key) [state content]
  (let [local-atom (::key state)]
    [:div {:on-click (fn [_] (reset! local-atom (not @local-atom)))}
     (if @local-atom
       content
       (trunc content 150))]))

(rum/defcs Home < rum/reactive (rum/local false ::loan) (rum/local false ::investor) [state]
  (let [investor-atom (::investor state) loan-atom (::loan state)]
    [:div.home
     [:h1 "Reports"]
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "investor-balance"))} "Investor balance")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "capital-outstanding"))} "Capital Outstanding")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "investor-transactions"))} "Investor Transactions")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "loan-transactions"))} "Loan Transactions")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "membership-list"))} "Membership List")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "lender-summary"))} "Lender Summary")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "most-recent-bid-listing"))} "Most Recent Bid Listing")
     (ui/Button {:variant "contained" :on-click (fn [e] (State/GetReport "loan-loss"))} "Loan Loss")
     [:h1.marginTop "Loan Data"]
     (ui/Button {:variant "contained" :on-click (fn [_] (reset! loan-atom (not @loan-atom)))} (if @loan-atom "Hide" "Show"))
     (if @loan-atom
       (ui/Table
        (ui/TableHead
         (ui/TableRow
          (map (fn [item] (ui/TableCell (name item))) (keys (first ((rum/react State/State) :loans))))))
        (ui/TableBody
         (map (fn [item] (ui/TableRow
                          (map (fn [cols] (ui/TableCell (TextCell (.stringify js/JSON (clj->js cols))))) (vals item))))
              ((rum/react State/State) :loans)))))
     [:h1.marginTop "Investor Data"]
     (ui/Button {:variant "contained" :on-click (fn [_] (reset! investor-atom (not @investor-atom)))} (if @investor-atom "Hide" "Show"))
     (if @investor-atom
       (ui/Table
        (ui/TableHead
         (ui/TableRow
          (map (fn [item] (ui/TableCell (name item))) (keys (first ((rum/react State/State) :investors))))))
        (ui/TableBody
         (map (fn [item] (ui/TableRow
                          (map (fn [cols] (ui/TableCell (TextCell (.stringify js/JSON (clj->js cols))))) (vals item))))
              ((rum/react State/State) :investors)))))]))
