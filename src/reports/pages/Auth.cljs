(ns reports.pages.Auth
  (:require
   [rum.core :as rum]
   [reports.wrappers.ui :as ui]
   [reports.State :as State]))

(rum/defc Auth < rum/reactive []
  [:form#login
   [:label {:for "pass"} "Password"]
   [:input {:name "pass" :placeholder "Enter Password" :type "password"}]
   [:label {:for "user"} "Admin Username"]
   [:input {:name "user" :placeholder "Enter Username"}]
   [:button {:on-click (fn [e]
                         (def targetElement (.getElementById js/document "login"))
                         (def data (.FormData js/document targetElement))
                         (State/Login data))}]]
  [:div.error ((rum/react State/State) :error)])
