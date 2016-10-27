(ns cljs.user
  (:require [windrose.core :as wr]))

(enable-console-print!)

(def navmesh
  (-> (wr/navmesh [0 0 500 500])
      (wr/add-point [300 200])
      (wr/add-point [300 100])
      (wr/add-point [400 200])
      (wr/add-point [350 150])
      (wr/add-point [400 100])))

(set! (.-innerHTML (js/document.getElementById "navmesh"))
      (wr/navmesh->svg navmesh))

(prn (wr/intersects-line [[0 0] [2 2]] [[2 0] [0 2]]))
(prn (wr/intersects-line [[0 0] [2 0]] [[0 0] [0 2]]))
(prn (wr/intersects-line [[0 0] [2 1]] [[2 0] [0 2]]))
