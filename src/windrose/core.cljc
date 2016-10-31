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

(defn- distance-squared [[x0 y0] [x1 y1]]
  (let [dx (- x1 x0)
        dy (- y1 y0)]
    (+ (* dx dx) (* dy dy))))

(defn- distance-squared-to-side [[[x0 y0 :as a] [x1 y1 :as b]] [x y]]
  (let [dist (distance-squared a b)
        dot  (/ (+ (* (- x x0) (- x1 x0))
                   (* (- y y0) (- y1 y0)))
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

(defn- close-to-point? [a b]
  (or (= a b) (<= (distance-squared a b) error-margin-squared)))

(defn- close-to-corner? [triangle point]
  (some #(close-to-point? point %) (:points triangle)))

(defn- close-to-side [{[a b c] :points} point]
  (let [close-side #(if (<= (distance-squared-to-side % point) error-margin-squared) %)]
    (or (close-side [a b])
        (close-side [b c])
        (close-side [c a]))))

(defn in-triangle? [triangle point]
  (and (in-bounding-box? triangle point)
       (or (naive-in-triangle? triangle point)
           (close-to-side triangle point))))

(defn- project-point-onto-line [[[x0 y0] [x1 y1]] [x y]]
  (let [dx (- x1 x0)
        dy (- y1 y0)
        t  (/ (+ (* (- x x0) dx)
                 (* (- y y0) dy))
              (+ (* dx dx)
                 (* dy dy)))]
    [(+ x0 (* t dx))
     (+ y0 (* t dy))]))

(defn- split-triangle-at-side [triangles id {[a b c] :points} p side next-id]
  (let [p'      (project-point-onto-line side p)
        points1 (if (= side [a b]) [p' b c] [p' a b])
        points2 (if (= side [a c]) [p' b c] [p' a c])]
    (-> triangles
        (dissoc id)
        (assoc (+ next-id 0) {:points points1})
        (assoc (+ next-id 1) {:points points2}))))

(defn- split-triangle-at-point [triangles id {[a b c] :points} p next-id]
  (-> triangles
      (dissoc id)
      (assoc (+ next-id 0) {:points [a b p]})
      (assoc (+ next-id 1) {:points [a p c]})
      (assoc (+ next-id 2) {:points [p b c]})))

(defn- add-point-to-triangle [{:keys [next-id] :as navmesh} id triangle point]
  (if (close-to-corner? triangle point)
    navmesh
    (if-let [side (close-to-side triangle point)]
      (-> navmesh
          (update :triangles split-triangle-at-side id triangle point side next-id)
          (assoc :next-id (+ next-id 2)))
      (-> navmesh
          (update :triangles split-triangle-at-point id triangle point next-id)
          (assoc :next-id (+ next-id 3))))))

(defn add-point [navmesh point]
  (reduce-kv
   (fn [navmesh id triangle]
     (cond-> navmesh
       (in-triangle? triangle point)
       (add-point-to-triangle id triangle point)))
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

(defn add-line [navmesh line]
  (reduce-kv
   (fn [navmesh id triangle]
     (if-let [points (intersects-triangle triangle line)]
       (reduce #(add-point-to-triangle %1 id triangle %2) navmesh points)
       navmesh))
   navmesh
   (:triangles navmesh)))

(defn- triangle-midpoint [{[[x0 y0] [x1 y1] [x2 y2]] :points}]
  [(/ (+ x0 x1 x2) 3.0)
   (/ (+ y0 y1 y2) 3.0)])

(defn- update-blocked [triangles points]
  (let [blocking-triangle {:points points}
        blocked?          #(in-triangle? blocking-triangle (triangle-midpoint %))]
    (reduce-kv
     (fn [triangles id triangle]
       (assoc triangles id (cond-> triangle (blocked? triangle) (assoc :blocked? true))))
     {}
     triangles)))

(defn add-triangle [navmesh [a b c :as points]]
  (-> navmesh
      (add-point a)
      (add-point b)
      (add-point c)
      (add-line [a b])
      (add-line [b c])
      (add-line [c a])
      (update :triangles update-blocked points)))

(defn- triangle->svg [{[[x0 y0] [x1 y1] [x2 y2]] :points, blocked? :blocked?}]
  (str "<polygon "
       "points=\"" x0 "," y0 " " x1 "," y1 " " x2 "," y2 "\" "
       "style=\"fill:" (if blocked? "red" "transparent")
       ";stroke:black;stroke-width:2\"/>"))

(defn navmesh->svg [{[_ _ w h] :area, triangles :triangles}]
  (str "<svg width=\"" w "\" height=\"" h "\">"
       (apply str (map triangle->svg (vals triangles)))
       "</svg>"))
