(ns reports.App
  (:require
   [rum.core :as rum]
   [reports.DynamicReact.State :as DynamicReactState]
   [reports.DynamicReact.Intro :as Intro]
   [reports.DynamicReact.IntroHeader :as IntroHeader]
   [reports.DynamicReact.Overlay :as Overlay]
   [reports.DynamicReact.PageContainer :as PageContainer]
   [reports.DynamicReact.Footer :as Footer]))

(rum/defc App < rum/reactive []
  (def overlay ((rum/react DynamicReactState/State) :overlay))
  [:div
   [:div.intro#intro (Intro/Intro)]
   [:div.introHeader#introHeader (IntroHeader/IntroHeader)]
   (if (overlay :state)
     (Overlay/Overlay)
     [:div])
   [:div.page-container {:id "page container"} (PageContainer/PageContainer)]
   [:div.footer#footer (Footer/Footer)]])
