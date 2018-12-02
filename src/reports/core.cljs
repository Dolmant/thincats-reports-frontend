(ns reports.core
  (:require   [rum.core :as rum]
              [reports.wrappers.ui :as ui]
              [reports.State :as State]
              [reports.pages.Home :as Home]
              [reports.pages.Auth :as Auth]))

(enable-console-print!)


(rum/defc current-page < rum/reactive []
  (ui/MuiThemeProvider
   {:theme (ui/get-mui-theme)}
   (case ((rum/react State/State) :page)
     :home (Home/Home)
     :auth (Auth/Auth))))

(defn mountRoot []
  (def targetElement (.getElementById js/document "app"))
  (rum/mount (current-page) targetElement)
  (js/console.log "rendered"))

(defn reload! []
  (mountRoot))

(mountRoot)
