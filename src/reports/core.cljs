(ns reports.core
  (:require-macros [reports.rum-adaptor-macro])
  (:require   [rum.core :as rum]
              [reports.wrappers.ui :as ui]
              [reports.wrappers.ic :as ic]
              [reports.home.Home :as Home]))

(enable-console-print!)

(rum/defc home-page < rum/reactive []
  [:div (Home/Home)])
(defonce sheetsManager (js/Map.))

(rum/defc current-page []
  (ui/get-jss (ui/mui-theme-provider
               {:theme (ui/get-mui-theme) :sheetsManager sheetsManager}
               (home-page))))

(defn mountRoot []
  (def targetElement (.getElementById js/document "app"))
  (do (js/console.log "rendered") (rum/mount (current-page) targetElement)))

(defn reload! []
  (mountRoot))

(mountRoot)
