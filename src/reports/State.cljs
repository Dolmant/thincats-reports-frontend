(ns reports.State
  (:require [rum.core :as rum]
            [reports.consts :as consts]
            [goog.dom.forms :as gforms]
            [cljs-http.client :as http]
            [clojure.string :as str]
            [cljs.core.async :refer [<!]]
            [clojure.string :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce State
  (atom {:page :home}))

(defn update-vals [map mf]
  (reduce #(update-in % [%2] (fn [_] (mf %2))) map (keys mf)))

(defn SetAttr [attr, value]
  (reset! State
          (update-in @State [attr] (fn [_] value))))

(defn SetAttrs [attrs]
  (reset! State
          (update-vals @State attrs)))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn SetPage [page]
  (SetAttr :page page))

(defn SetCreds [data]
  (SetAttr :creds data))

(defn SetLoans [loans]
  (SetAttr :loans loans))

(defn SetInvestors [investors]
  (SetAttr :investors investors))

(defn SetError [error]
  (SetAttr :error error))

(defn GetReport [report]
  (go (let [response (<! (http/post (str/join "" ["https://todo investors/" report])
                                    {:basic-auth (@State :creds)}))]
        (if (not (:success response))
          (SetError "Failed to load report")))))

(defn GetInvestors []
  (go (let [response (<! (http/post "https://todo investors"
                                    {:basic-auth (@State :creds)}))]
        (if (:success response)
          (SetInvestors (response :body))
          (SetError "Failed to load data")))))

(defn Login [data]
  (go (let [response (<! (http/post "https://todo loans"
                                    {:basic-auth {:username "hello" :password "world"}}))]
        (if (:success response)
          (do (SetPage :home)
              (SetCreds data)
              (SetLoans (response :body))
              (GetInvestors))
          (SetError "Failed to login")))))


  ; (defn formOverride [e]
  ;   (.preventDefault e)
  ;   (def formData (js->clj (.-map_ (gforms/getFormDataMap (.getElementById js/document "contact-form")))))
  ;   (def convertedData (reduce #(update-in %1 [(first %2)] (fn [_] (first (last %2)))) formData formData))
  ;   (if (= "" (convertedData "Contact Details"))
  ;     (reset! formError true)
  ;     (do
  ;       (reset! formError false)
  ;       (State/CloseOverlay)
  ;       (go (let [response (<! (http/post "https://us-central1-lweb-176107.cloudfunctions.net/sendLWEBMail"
  ;                                         {:with-credentials? false
  ;                                          :form-params convertedData}))])))))