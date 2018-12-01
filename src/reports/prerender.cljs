(ns reports.prerender
  (:require   [rum.core :as rum]
              [clojure.string :as str]
              [cljs-node-io.core :as io :refer [slurp spit]]
              [cljs-node-io.fs :as fs]
              [reports.template.index :as htmlTemplate]
              [cljs.core.async :refer [<!]]
              ["react-dom/server" :as reactDOM])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(goog-define DEV false)

;prerender and hash the appropriate files
(defn prerender []
    ;Clear any previously hashed files
  (doall (map
          #(if (str/includes? %1 ".css")
             (if (not (str/includes? %1 "styles.css"))
               (fs/rm (str/join "" ["dist/" %1]))))
          (fs/readdir "dist")))
  (doall (map
          #(if (str/includes? %1 ".js")
             (if (not (str/includes? %1 "main.js"))
               (fs/rm (str/join "" ["dist/" %1]))))
          (fs/readdir "dist")))
    ;Generate hashed files
  (def css (remove nil? (map
                         #(if (str/includes? %1 "styles.css")
                            (do
                              (def content (.toString (fs/readFile (str/join "" ["dist/" %1]) "")))
                              (def newName (str/join "" [(first (str/split %1 #".css")) "." (hash content) ".css"]))
                              (fs/writeFile (str/join "" ["dist/" newName]) content {})
                              newName))
                         (fs/readdir "dist"))))
  (def jsmap (remove nil? (map
                           #(if (str/includes? %1 "main.js")
                              (do
                                (def content (.toString (fs/readFile (str/join "" ["dist/" %1]) "")))
                                (def newName (str/join "" [(first (str/split %1 #".js")) "." (hash content) ".js"]))
                                (fs/writeFile (str/join "" ["dist/" newName]) content {})
                                newName))
                           (fs/readdir "dist"))))
  (def data (str/join "" ["<!DOCTYPE html>" (reactDOM/renderToString [(htmlTemplate/Template (if DEV ["styles.css"] css) (if DEV ["main.js"] jsmap))])]))
  (go
    (let [[err] (<! (io/aspit "dist/index.html" data))]
      (if-not err
        (println "you've successfully written to 'dist/index.html'")
        (println "there was an error writing: " err)))))