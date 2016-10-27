(ns windrose.core)

(defn navmesh [[x0 y0 w h :as area]]
  (let [x1 (+ x0 w)
        y1 (+ y0 h)]
    {:area area
     :next-id 2
     :triangles
     {0 {:points [[x0 y0] [x1 y0] [x1 y1]]}
      1 {:points [[x0 y0] [x0 y1] [x1 y1]]}}}))

(def error-margin 1.0)

(def error-margin-squared
  (* error-margin error-margin))

(defn- in-bounding-box? [{[[x0 y0] [x1 y1] [x2 y2]] :points} [x y]]
  (and (<= (- (min x0 x1 x2) error-margin) x (+ (max x0 x1 x2) error-margin))
       (<= (- (min y0 y1 y2) error-margin) y (+ (max y0 y1 y2) error-margin))))

(defn- naive-in-triangle? [{[[x0 y0] [x1 y1] [x2 y2]] :points} [x y]]
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

(defn- distance-squared-to-side [[[x0 y0] [x1 y1]] [x y]]
  (let [dx   (- x1 x0)
        dy   (- y1 y0)
        dist (+ (* dx dx) (* dy dy))
        dot  (/ (+ (* (- x x0) dx)
                   (* (- y y0) dy))
                dist)]
    (cond
      (neg? dot)
      (+ (* (- x x0) (- x x0))
         (* (- y y0) (- y y0)))
      (<= dot 1)
      (- (+ (* (- x0 x) (- x0 x))
            (* (- y0 y) (- y0 y)))
         (* dot dot dist))
      :else
      (+ (* (- x x1) (- x x1))
         (* (- y y1) (- y y1))))))

(defn- close-to-side? [{[a b c] :points} point]
  (or (<= (distance-squared-to-side [a b] point) error-margin-squared)
      (<= (distance-squared-to-side [b c] point) error-margin-squared)
      (<= (distance-squared-to-side [c a] point) error-margin-squared)))

(defn in-triangle? [triangle point]
  (and (in-bounding-box? triangle point)
       (or (naive-in-triangle? triangle point)
           (close-to-side? triangle point))))

(defn- split-triangle [triangles id {[[x0 y0] [x1 y1] [x2 y2]] :points} [x y] next-id]
  (-> triangles
      (dissoc id)
      (assoc next-id       {:points [[x0 y0] [x1 y1] [x y]]})
      (assoc (+ next-id 1) {:points [[x0 y0] [x y] [x2 y2]]})
      (assoc (+ next-id 2) {:points [[x y] [x1 y1] [x2 y2]]})))

(defn add-point [navmesh point]
  (reduce-kv
   (fn [navmesh id triangle]
     (if (in-triangle? triangle point)
       (-> navmesh
           (update :triangles split-triangle id triangle point (:next-id navmesh))
           (update :next-id + 3))
       navmesh))
   navmesh
   (:triangles navmesh)))

(defn intersects-line [[[ax0 ay0] [ax1 ay1]] [[bx0 by0] [bx1 by1]]]
  (let [denom (float (- (* (- ax1 ax0) (- by1 by0))
                        (* (- bx1 bx0) (- ay1 ay0))))]
    (when-not (zero? denom)
      (let [s (/ (- (* (- ax1 ax0) (- ay0 by0))
                    (* (- ay1 ay0) (- ax0 bx0)))
                 denom)
            t (/ (- (* (- bx1 bx0) (- ay0 by0))
                    (* (- by1 by0) (- ax0 bx0)))
                 denom)]
        (when (and (<= 0 t 1) (<= 0 s 1))
          [(+ ax0 (* t (- ax1 ax0)))
           (+ ay0 (* t (- ay1 ay0)))])))))

(defn intersects-triangle [{[a b c] :points} line]
  (into #{}
        (comp (map #(intersects-line line %))
              (remove nil?))
        [[a b] [b c] [c a]]))

(defn- triangle->svg [{[[x0 y0] [x1 y1] [x2 y2]] :points}]
  (str "<polygon "
       "points=\"" x0 "," y0 " " x1 "," y1 " " x2 "," y2 "\" "
       "style=\"fill:transparent;stroke:black;stroke-width:2\"/>"))

(defn navmesh->svg [{[_ _ w h] :area, triangles :triangles}]
  (str "<svg width=\"" w "\" height=\"" h "\">"
       (apply str (map triangle->svg (vals triangles)))
       "</svg>"))
