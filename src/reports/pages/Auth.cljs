(ns reports.pages.Auth
  (:require
   [rum.core :as rum]
   [reports.wrappers.ui :as ui]
   [reports.State :as State]))

(rum/defc Auth < rum/reactive []
  [:div.auth
   [:form#login
    [:div
     [:label {:for "user"} "Admin Username"]
     [:input {:name "user" :placeholder "Enter Username"}]]
    [:div
     [:label {:for "pass"} "Password"]
     [:input {:name "pass" :placeholder "Enter Password" :type "password"}]]]

   (ui/Button {:variant "contained" :on-click (fn [e]
                                                (def targetElement (.getElementById js/document "login"))
                                                (def data (js/window.FormData. targetElement))
                                                (State/Login data))} "Submit")
   [:div.error ((rum/react State/State) :error)]])
