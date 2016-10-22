(ns cljs.user
  (:require [windrose.core :as wr]))

(enable-console-print!)

(set! (.-innerHTML (js/document.getElementById "navmesh"))
      (wr/navmesh->svg (wr/navmesh [0 0 500 500])))
