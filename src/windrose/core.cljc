(ns windrose.core)

(defn navmesh [[x0 y0 w h :as area]]
  (let [x1 (+ x0 w)
        y1 (+ y0 h)]
    {:area area
     :next-id 2
     :triangles
     {0 {:points [[x0 y0] [x1 y0] [x1 y1]]}
      1 {:points [[x0 y0] [x0 y1] [x1 y1]]}}}))

(defn in-triangle? [{[[x0 y0] [x1 y1] [x2 y2]] :points} [x y]]
  (let [denom (float (+ (* (- y1 y2) (- x0 x2))
                        (* (- x2 x1) (- y0 y2))))
        a     (/ (+ (* (- y1 y2) (- x x2))
                    (* (- x2 x1) (- y y2)))
                 denom)
        b     (/ (+ (* (- y2 y0) (- x x2))
                    (* (- x0 x2) (- y y2)))
                 denom)
        c     (- 1 a b)]
    (and (<= 0 a 1)
         (<= 0 b 1)
         (<= 0 c 1))))

(defn find-containing-triangle-id [{:keys [triangles]} point]
  (some (fn [[k v]] (if (in-triangle? v point) k)) triangles))

(defn- split-triangle [triangles id [x y] next-id]
  (let [{[[x0 y0] [x1 y1] [x2 y2]] :points} (triangles id)]
    (-> triangles
        (dissoc id)
        (assoc next-id       {:points [[x0 y0] [x1 y1] [x y]]})
        (assoc (+ next-id 1) {:points [[x0 y0] [x y] [x2 y2]]})
        (assoc (+ next-id 2) {:points [[x y] [x1 y1] [x2 y2]]}))))

(defn add-point [{:keys [next-id] :as navmesh} point]
  (let [id (find-containing-triangle-id navmesh point)]
    (-> navmesh
        (update :triangles split-triangle id point next-id)
        (update :next-id + 3))))

(defn- triangle->svg [{[[x0 y0] [x1 y1] [x2 y2]] :points}]
  (str "<polygon "
       "points=\"" x0 "," y0 " " x1 "," y1 " " x2 "," y2 "\" "
       "style=\"fill:transparent;stroke:black;stroke-width:2\"/>"))

(defn navmesh->svg [{[_ _ w h] :area, triangles :triangles}]
  (str "<svg width=\"" w "\" height=\"" h "\">"
       (apply str (map triangle->svg (vals triangles)))
       "</svg>"))
