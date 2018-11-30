(ns reports.home.Home
  (:require
   [rum.core :as rum]
   [reports.home.State :as state]))

(rum/defc Home []
  [:footer
   [:div.footer-container.container
    [:div.contact_me
     [:a#contact_overlay {:on-click (fn [e] (DynamicReactState/SetOverlay :contact))} "Contact Me!"]]
    [:div.contact
     [:ul
      [:li [:a "Member of the Australian Institute of Medical and Biological Illustration"]]]]
    [:div.social
     [:ul
      [:li
       [:a {:target "_blank" :rel "noopener noreferrer" :href "http://leotide.tumblr.com/"}
        [:img {:alt "It's not loading!" :src "./assets/icons/tumblricon.png"}]]]
      [:li
       [:a {:target "_blank" :rel "noopener noreferrer" :href "https://twitter.com/leotidelh?lang=en"}
        [:img {:alt "It's not loading!" :src "./assets/icons/twittericon.png"}]]]
      [:li
       [:a {:target "_blank" :rel "noopener noreferrer" :href "https://www.instagram.com/leo_tide/"}
        [:img {:alt "It's not loading!" :src "./assets/icons/instagramicon.png"}]]]]]]])


(ns lweb.DynamicReact.NavMenu
  (:require [rum.core :as rum]
            [lweb.DynamicReact.State :as DynamicReactState]
            [lweb.consts :as consts]
            [lweb.wrappers.ui :as ui]
            [lweb.wrappers.ic :as ic]))

(defn scroll-into-view [element]
  (def bodyOffset (.-top (.getBoundingClientRect (.-body js/document))))
  (def offset (- (.-top (.getBoundingClientRect (.getElementById js/document element))) 50))
  (.scrollTo js/window (js-obj "behavior" "smooth" "top" (- offset bodyOffset))))

(defn oncatClick [id]
  (DynamicReactState/SetCategory id)
  (DynamicReactState/SetAttr :touchmenu_active false))

(defn sorter [cat1 cat2]
  (if (= cat1 :CHECKOUT) 1
      (if (= cat2 :CHECKOUT) -1
          (if (= cat1 :MODELS) 1
              (if (= cat2 :MODELS) -1
                  (if (and (= cat1 :ADVERTISING) (= cat2 :ALL)) 1
                      (if (and (= cat1 :ALL) (= cat2 :ADVERTISING)) -1
                          (if (> cat1 cat2) 1
                              (if (< cat1 cat2) -1
                                  0)))))))))

(def before [(ui/button {:key "before1" :on-click (fn [] (scroll-into-view "about")) :id 1} "ABOUT")
             (ui/button {:key "before2" :on-click (fn [e] (DynamicReactState/SetOverlay :contact)) :id 2} "CONTACT")])
(def after [(if (not consts/isTouch) (ui/button {:key "after1" :on-click (fn [e] (DynamicReactState/SetOverlay :models)) :id 3} "MODELS"))
            (ui/button {:key "after2" :on-click (fn [e] (DynamicReactState/SetOverlay :checkout)) :id 4} "CHECKOUT")])
(def bundled [:NATURE, :SCIENCE, :ANATOMY, :TYPOGRAPHY, :FACTS, :MISC, :CHECKOUT])
(defn filterfn [item] (not (some #(= item %) bundled)))

(rum/defc NavMenuNoKey < rum/reactive []
  (defn navMapper [item]
    (if (= item :ALL)
      (ui/button {:key item :on-click (fn [] (scroll-into-view "content") (oncatClick item)) :id item} "ILLUSTRATIONS")
      (ui/button {:key item :on-click (fn [] (scroll-into-view "content") (oncatClick item)) :id item} (name item))))
  (if consts/isTouch
    (concat before after)
    (concat before (map navMapper (sort sorter (filter filterfn (keys consts/projectListLabels)))) after)))

(defn NavMenu [] (rum/with-key (NavMenuNoKey) "navmenu"))

(rum/defc TabMenu < rum/reactive []
  (defn tabMapper [item]
    (if (= item :ALL)
      (ui/tab {:key (name item) :value :ALL :label "ILLUSTRATIONS" :id item})
      (ui/tab {:key (name item) :value (name item) :label (name item) :id item})))
  (ui/tabs {:key "navmenu" :centered true :value ((rum/react DynamicReactState/State) :category) :indicatorColor "primary" :textColor "primary" :onChange (fn [event value] (oncatClick (keyword value)))}
           (map tabMapper (sort sorter (filter filterfn (keys consts/projectListLabels))))))

(ns lweb.DynamicReact.Overlay
  (:require [rum.core :as rum]
            [lweb.DynamicReact.State :as DynamicReactState]
            [lweb.Shop.CartManagement.AddToCart :as AddToCart]
            [goog.dom.forms :as gforms]
            [cljs-http.client :as http]
            [lweb.consts :as consts]
            [lweb.DynamicReact.ModelViewer :as ModelViewer]
            [lweb.Shop.CartManagement.Checkout :as Checkout]
            [lweb.wrappers.ui :as ui]
            [lweb.wrappers.ic :as ic]
            ["/gen/lazySizes/index" :default Lazy]
            [clojure.string :as str]
            [lweb.rum-adaptor-macro]
            [cljs.core.async :refer [<!]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [lweb.rum-adaptor-macro :as m]))

(def LazySizes (m/adapt-rum-class Lazy))

(defn oncatClick [id]
  (DynamicReactState/SetCategory id)
  (DynamicReactState/SetAttr :touchmenu_active false))

(defonce formError (atom false))

(rum/defc Overlay < rum/reactive []
  (def overlay ((rum/react DynamicReactState/State) :overlay))
  (def overlay_image_num ((rum/react DynamicReactState/State) :overlay_image_num))
  (def overlay_image_src ((rum/react DynamicReactState/State) :overlay_image_src))
  (def overlay_thumb_src ((rum/react DynamicReactState/State) :overlay_thumb_src))
  (def overlay_types ((rum/react DynamicReactState/State) :overlay_types))
  (def overlay_txt ((rum/react DynamicReactState/State) :overlay_txt))
  (defn formOverride [e]
    (.preventDefault e)
    (def formData (js->clj (.-map_ (gforms/getFormDataMap (.getElementById js/document "contact-form")))))
    (def convertedData (reduce #(update-in %1 [(first %2)] (fn [_] (first (last %2)))) formData formData))
    (if (= "" (convertedData "Contact Details"))
      (reset! formError true)
      (do
        (reset! formError false)
        (DynamicReactState/CloseOverlay)
        (go (let [response (<! (http/post "https://us-central1-lweb-176107.cloudfunctions.net/sendLWEBMail"
                                          {:with-credentials? false
                                           :form-params convertedData}))])))))
  (defn backgroundOverlayClick [e]
    (cond
      (str/includes? (.-className (.-target e)) "overlay_container") (println "Overlay not closing, not outside target")
      (str/includes? (.-className (.-target e)) "downnav_overlay") (println "Overlay not closing, not outside target")
      (str/includes? (.-className (.-target e)) "upnav_overlay") (println "Overlay not closing, not outside target")
      (str/includes? (.-className (.-target e)) "rightnav_overlay") (println "Overlay not closing, not outside target")
      (str/includes? (.-className (.-target e)) "leftnav_overlay") (println "Overlay not closing, not outside target")
      :else (DynamicReactState/CloseOverlay)))
  (defn CloseButtonClick [e]
    (.preventDefault e)
    (DynamicReactState/CloseOverlay))
  (cond
    (= (overlay :name) :image)
    [:div.overlay_top
     [:div#backgroundOverlay.backgroundOverlay {:on-click (fn [e] (backgroundOverlayClick e))}]
     [:div.overlay_container
      [:a {:class "closebutton strokeme" :on-click (fn [e] (CloseButtonClick e))} "✖"]
      [:div.overlayimagecontrol
       [:div
        (if consts/isTouch [:div.img-wrap-up-overlay
                            (if (get-in overlay [:arrows :left]) (ic/chevronLeft {:on-click #(DynamicReactState/NavOverlayImage "left")}))
                            (if (get-in overlay [:arrows :up]) (ic/keyboardArrowUp {:on-click #(DynamicReactState/NavOverlayImage "up")}))
                            (if (get-in overlay [:arrows :down]) (ic/keyboardArrowDown {:on-click #(DynamicReactState/NavOverlayImage "down")}))
                            (if (get-in overlay [:arrows :right]) (ic/chevronRight {:on-click #(DynamicReactState/NavOverlayImage "right")}))]
            [(if (get-in overlay [:arrows :left]) [:div.img-wrap-left-overlay
                                                   (ic/chevronLeft {:on-click #(DynamicReactState/NavOverlayImage "left")})])
             (if (get-in overlay [:arrows :right]) [:div.img-wrap-right-overlay
                                                    (ic/chevronRight {:on-click #(DynamicReactState/NavOverlayImage "right")})])
             (if (get-in overlay [:arrows :up]) [:div.img-wrap-up-overlay
                                                 (ic/keyboardArrowUp {:on-click #(DynamicReactState/NavOverlayImage "up")})
                                                 (if (get-in overlay [:arrows :down])
                                                   (ic/keyboardArrowDown {:className "marginLeft" :on-click #(DynamicReactState/NavOverlayImage "down")}))])
             (if (and (not (get-in overlay [:arrows :up])) (get-in overlay [:arrows :down])) [:div.img-wrap-down-overlay
                                                                                              (ic/keyboardArrowDown {:on-click #(DynamicReactState/NavOverlayImage "down")})])])]
       [:h2 overlay_txt]
       (if (overlay :is_video)
         [:video.overlay-video {:autoPlay "1" :loop "1" :controls "1"}
          [:source {:src overlay_image_src :type "video/mp4"}]
          "Your browser does not support the video tag."]
         [:div.img-wrap-overlay
          (LazySizes {:dataSizes "auto" :alt "It's not loading!" :className "scale-img blur-up overlayimage" :src overlay_thumb_src :dataSrc overlay_image_src})])
       (if (overlay :is_video)
         [:div]
         [:div.overlaytext "This image is large and will remain obfuscated until downloaded"])
       (AddToCart/AddToCart false overlay_image_num overlay_types)]]]
    (= (overlay :name) :contact)
    (ui/dialog {:PaperProps {:className "overlayform"} :open true :onClose (fn [] (reset! formError false) (DynamicReactState/CloseOverlay))}
               (ui/dialog-title "The Leo Signal")
               (ui/dialog-content
                [:div
                 [:form#contact-form {:on-submit formOverride :target "self" :class "topLabel"}
                  [:form-head.form-spaced
                   [:h6 "Fill out the form below to get in contact with me!"]]
                  [:div.form-spaced
                   [:div
                    [:span
                     (ui/text-field {:id "element_2" :name "Firstname" :label "First Name"})]
                    [:span
                     (ui/text-field {:id "element_3" :name "Lastname" :label "Last Name"})]]]
                  [:div.form-spaced
                   (ui/text-field {:onChange (fn [e] (if (= "" (.-value (.-target e))) (reset! formError true) (reset! formError false))) :error (rum/react formError) :id "element_7" :name "Contact Details" :label "Contact Details*" :helperText (if (rum/react formError) "Please enter your contact details!" "")})]
                  [:div.form-spaced
                   (ui/text-field {:id "element_8" :name "Message" :multiline true :rows "10" :label "Message*"})]
                  [:div.submit
                   [:input {:value "Submit" :type "submit"}]]]
                 [:input {:name "embed" :value "form" :type "hidden"}]
                 [:input {:name "http_referer" :value "http://www.leotide.com/" :type "hidden"}]]))
    (= (overlay :name) :checkout)
    (ui/dialog {:PaperProps {:className "checkoutoverlay"} :open true :onClose (fn [] (reset! formError false) (DynamicReactState/CloseOverlay))}
               (ui/dialog-content (Checkout/Checkout)))
    (= (overlay :name) :models)
    (ui/dialog {:PaperProps {:className "modeloverlay"} :open true :onClose (fn [] (reset! formError false) (DynamicReactState/CloseOverlay))}
               (ui/dialog-title "Model Viewer")
               (ui/dialog-content (ModelViewer/ModelViewer)))))

(ns lweb.DynamicReact.PageContainer
  (:require-macros [lweb.rum-adaptor-macro :as m])
  (:require [rum.core :as rum]
            [lweb.DynamicReact.NavMenu :as NavMenu]
            [lweb.DynamicReact.State :as DynamicReactState]
            [lweb.consts :as consts]
            ; ["react-slick" :as Slick]
            [clojure.string :as str]))

; (def Slider (m/adapt-rum-class Slick/default))

; (rum/defc LeftNavButton []
;   [:button [:div.slick-next-div]])

; (rum/defc RightNavButton []
;   [:button [:div.slick-prev-div]])

(defn onImageClick [index]
  (DynamicReactState/UpdateOverlayImage index)
  (DynamicReactState/SetOverlay :image))

(rum/defc PageContainer < rum/reactive []
  (def itemList ((rum/react DynamicReactState/State) :list))
  (def category ((rum/react DynamicReactState/State) :category))
  (def page ((rum/react DynamicReactState/State) :page))
  (def listItems [:ul.projects {:key "ul"}
                  (map (fn [item]
                         [:li {:key (item :item_number) :on-click (fn [] (onImageClick (get item :item_number)))}
                          [:div.img-wrap
                           [:div {:style {:height "100%" :width "100%" :backgroundImage (str/join "" ["url(" (item :thumbs_src) ")"]) :backgroundPosition "center" :backgroundRepeat "no-repeat" :backgroundSize "contain"}}]]])
                       itemList)])
  (def listCarousel
    (map (fn [item]
           [:div.carousel-img-wrap {:key (item :item_number)}
            [:div
             {:style {:backgroundImage (str/join "" ["url(" (item :thumbs_src) ")"]) :backgroundPosition "center" :backgroundRepeat "no-repeat" :backgroundSize "cover" :height "50vh" :width (if consts/isTouch "40vw" "20vw")}}]])
         (consts/projectList :HomeInitial)))
  [:div
   [:div.desc_holder#about
    ; [:img {:src "/assets/webImages/headshot.jpg"}]
    [:div
     [:div.desc_text.divider
      [:div "Leonie (Leo) Herson is a multi-disciplinary, multi-talented and multi-limbed biomedical animator and illustrator/graphic designer from Sydney. She has collaborated to create award-winning works with companies, research institutions, engineers (even banks!) from across the world."]]
     [:div.desc_text.divider
      [:div.news
       [:div
        "Beautiful and dangerous: animating deadly viruses at Vivid Sydney, link " [:strong [:a {:rel "noopener noreferrer" :target "_blank" :href "https://blog.csiro.au/beautiful-and-dangerous-animating-deadly-viruses-at-vivid-sydney/"} "here!"]]
        [:br]
        "Mastering biomedical science by design, link " [:strong [:a {:rel "noopener noreferrer" :target "_blank" :href "http://newsroom.uts.edu.au/news/2017/10/mastering-biomedical-science-design"} "here!"]]]]]]
            ; [:div.sidescroller
            ;     (Slider {:nextArrow (LeftNavButton)
            ;             :prevArrow (RightNavButton)
            ;             :dots true
            ;             :infinite true
            ;             :speed 500
            ;             :slidesToShow (if consts/isTouch 2 4)
            ;             :slidesToScroll 1
            ;             :arrows true
            ;             :lazyLoad false
            ;             :autoplay true
            ;             :autoplaySpeed 10000}
            ;             [listCarousel])]
            ; [:div.demoreel
            ;     [:h2 "Check out the demo reel below!"]
            ;     [:video {:preload "none" :autoPlay "" :loop "" :controls "1"}
            ;         [:source {:src "./assets/images/LHDemoReel18.mp4" :type "video/mp4"}]
            ;         "Your browser does not support the video tag"
            ;     ]
            ; ]
    ]
   [:div.desc
    ; [:h2 "My Work"]
    (NavMenu/TabMenu)]
   [:div#content
    [listItems]]])
