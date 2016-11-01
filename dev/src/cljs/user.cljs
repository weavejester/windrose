(ns cljs.user
  (:require [windrose.core :as wr]))

(enable-console-print!)

(def navmesh
  (-> (wr/navmesh [0 0 500 500])
      (wr/add-triangle [[300 200] [300 100] [400 200]])
      (wr/add-triangle [[400 100] [400 200] [300 100]])
      (wr/add-triangle [[250 250] [250 150] [350 250]])
      (wr/add-triangle [[350 150] [250 150] [350 250]])))

(set! (.-innerHTML (js/document.getElementById "navmesh"))
      (wr/navmesh->svg navmesh))

(println "Number of triangles =" (count (:triangles navmesh)))
