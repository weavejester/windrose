(ns cljs.user
  (:require [windrose.core :as wr]))

(enable-console-print!)

(def navmesh
  (-> (wr/navmesh [0 0 500 500])
      (wr/add-triangle [[300 200] [300 100] [400 200]])
      (wr/add-triangle [[400 100] [400 200] [300 100]])))

(set! (.-innerHTML (js/document.getElementById "navmesh"))
      (wr/navmesh->svg navmesh))

(println "Number of triangles =" (count (:triangles navmesh)))
