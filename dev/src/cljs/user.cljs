(ns cljs.user
  (:require [windrose.core :as wr]))

(enable-console-print!)

(def navmesh
  (wr/navmesh [0 0 500 500]))

(set! (.-innerHTML (js/document.getElementById "navmesh"))
      (wr/navmesh->svg navmesh))

(prn [[400 100] :in (wr/find-containing-triangle navmesh [400 100])])
(prn [[100 400] :in (wr/find-containing-triangle navmesh [100 400])])
