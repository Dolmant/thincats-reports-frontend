(ns reports.template.index
  (:require [rum.core :as rum]
            [clojure.string :as str]))

(rum/defc Template [css jsmap]
  (rum/defc cssSheet [href] [:link {:rel "stylesheet" :href href}])
  (rum/defc jsImport [src] [:script {:defer true :src src}])
  (def cssTemplate (map cssSheet css))
  (def jsTemplate (map jsImport jsmap))
  [:html#html
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width"}]
    [:meta {:name "author" :content "NoFavorite"}]
    [:meta {:name "theme-color" :content "#000000"}]
    [:meta {:name "description" :content "Thincats Admin Reporting"}]
    [:meta {:name "keywords" :content ""}]
    jsTemplate
    cssTemplate
    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css?family=Raleway:400,700,800"}]
    [:title "ThinCats Admin Reporting"]]
   [:body {:id "body" :style {:padding-right "0px !important"}}
    [:div#app]]])
