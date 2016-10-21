(ns windrose.core)

(defn navmesh [[x0 y0 w h :as area]]
  (let [x1 (+ x0 w)
        y1 (+ y0 h)]
    {:area area
     :triangles
     [{:points [[x0 y0] [x1 y0] [x1 y1]]}
      {:points [[x0 y0] [x0 y1] [x1 y1]]}]}))

(defn- triangle->svg [{[[x0 y0] [x1 y1] [x2 y2]] :points}]
  (str "<polygon "
       "points=\"" x0 "," y0 " " x1 "," y1 " " x2 "," y2 "\" "
       "style=\"fill:transparent;stroke:black;stroke-width:2\"/>"))

(defn navmesh->svg [{[_ _ w h] :area, triangles :triangles}]
  (str "<svg width=\"" w "\" height=\"" h "\">"
       (apply str (map triangle->svg triangles))
       "</svg>"))
