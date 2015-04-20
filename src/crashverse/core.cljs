(ns ^:figwheel-always crashverse.core
    (:require
     [sablono.core :as sab :include-macros true]
     [om.core :as om :include-macros true]
     [cljs.core.async :refer [timeout]])
    (:require-macros
     [cljs.core.async.macros :refer [go]]
     [crashverse.macros :refer [logmaster]]))

(enable-console-print!)

(defn time-now []
  (.getTime (js/Date.)))

(def init-planets [{:x 0
                    :y 180
                    :speed 0.001
                    :color "red"
                    :width 20}
                   {:x 0
                    :y -60
                    :speed -0.005
                    :color "#666"
                    :width 20}
                   {:x 0
                    :y -100
                    :speed 0.002
                    :color "blue"
                    :width 20}
                   {:x 145
                    :y 145
                    :speed 0.0023
                    :color "yellow"
                    :width 20}
                   {:x 100
                    :y -100
                    :speed -0.0023
                    :color "red"
                    :width 20}                                    
                   {:x 0
                    :y 0
                    :speed 0
                    :color "yellow"
                    :width 40 }])

(defonce app-state (atom {:universe { :width 500
                                      :height 500
                                     :start-time (time-now)}
                          :rocket {:x 250
                                   :y 470
                                   :width 20}
                          :planets init-planets}))

(defn reset-planets! []
  (swap! app-state assoc :planets init-planets))

(defn remove-planets! []
  (swap! app-state assoc :planets []))

(defn add-planet! [pl]
  (swap! app-state update-in [:planets] conj pl))

(defn reset-game! []
  (swap! app-state assoc
         :rocket {:x 250
                  :y 476
                  :width 20}))

(defn reset-all! []
    (reset-game!)
    (reset-planets!))

#_(reset-game!)
#_(reset-planets!)
#_(reset-all!)
#_(remove-planets!)

(def round js/Math.round)

(defn center [{:keys [width height]}]
  {:x (round (/ width 2))
   :y (round (/ height 2)) })

;; rotation
;; x' = x \cos \theta - y \sin \theta\,,
;; y' = x \sin \theta + y \cos \theta\,.

;; stage

#_(prn @app-state)

#_(prn (center {:width 300 :height 200 }))

#_(remove-planets!)

#_(reset-planets!)
;; edit some planet properties


(defn universe-position [u-data {:keys [x y] :as point}]
  (let [center-p (center u-data)
        offset   (round (- (/ (:width point) 2)))]
    (assoc point
           :x-verse (+ (:x center-p) x offset)
           :y-verse (+ (:y center-p) y offset))))

(defn rotate [theta {:keys [x y] :as point} ]
  (let [sint (js/Math.sin theta)
        cost (js/Math.cos theta)]
    (assoc point
           :x (round (- (* x cost) (* y sint)))
           :y (round (+ (* x sint) (* y cost))))))

#_(prn (center {:width 500 :height 400}))

#_(prn (rotate js/Math.PI {:x 100 :y 100} ))

(defn planet-position [u-data p-data]
  (let [theta (* (:speed p-data) (:time u-data))]
    (universe-position u-data
                       (rotate
                        theta p-data)))
  ;; theta =  speed * time
  )

(defn planet [u-data p-data]
  ;; DEBUGGING
  #_(prn p-data)
  (let [{:keys [x-verse y-verse]} (planet-position u-data p-data)]
    (sab/html [:div.planet {:style { :top (str y-verse "px")
                                     :left (str x-verse "px")
                                     :background-color (:color p-data)
                                     :width (str (:width p-data) "px")
                                     :height (str (:width p-data) "px")}}])))

;; live coding macros

(logmaster "hay" #_@app-state)

#_(defn)

#_(this is cool)

;; SHOW second browser
;; BROADCAST

#_(remove-planets!)

#_(reset-planets!)

;; Multiple builds


(defn universe-time [u-data]
  (- (time-now) (:start-time u-data)))

(defn planet-collision [data planet]
  (let [{:keys [x-verse y-verse width]}
        (planet-position (:universe data) planet)
        {:keys [x y]} (:rocket data)
        offset (+ 5 (/ width 2))]
    (and (> (+ x-verse offset) x (- x-verse offset))
         (> (+ y-verse offset) y (- y-verse offset)))))

(defn collision [data]
  (some (partial planet-collision data)
        (:planets data)))

(defn render-planets [data]
  (map (partial planet (:universe data))
         (:planets data)))

(defn render-rocket [r-data]
  (let [{:keys [x y]} r-data]
    (if (:explosion (:state r-data))
      (sab/html [:div.rocket.explosion {:style { :top (- y 62)
                                                 :left (- x 85)}
                                        }
                 [:img {:src (str "explosion.gif?x=" (:explosion (:state r-data)))}]])
      (sab/html [:div.rocket {:style {:top (- y 62)
                                      :left (- x 85)}}
                 [:div.capsule]
                 [:img.rocker {:src "rocket.gif"}]]))))

(defn rand-offset [n]
  (- (round (* n (js/Math.random))) (round (/ n 2))))

(defn move-it [rocket]
  (let [{:keys [x y]} rocket]
    (assoc rocket
           :x (+ x (rand-offset 80))
           :y (- y 20))))

#_(prn (rand-offset 100))

(defn move [r-data]
  (om/transact! r-data move-it))

#_(reset-game!)

(defn collision-transitions []
  (when-not (-> @app-state :rocket :state :explosion)
    (go
      (swap! app-state assoc-in [:rocket :state :explosion] (time-now))
      (<! (timeout 1000))
      (reset-game!))))

(defn score-transitions []
  (when-not (-> @app-state :rocket :state :score)
    (go
      (swap! app-state assoc-in [:rocket :state :score] (time-now))
      (<! (timeout 1000))
      (reset-game!))))

(defn get-start-time! [owner]
  (if-let [start-time (om/get-state owner :start-time)]
    start-time
    (let [cur-time (time-now)]
      (om/set-state! owner :start-time cur-time)
      cur-time)))

(defn update-time! [owner]
  (let [start (get-start-time! owner)]
    (let [passed (- (time-now) start)]
      (om/set-state! owner :current-time passed)
      passed)))

(defn game-board [data owner]
  (reify
    om/IRender
    (render [_]
      (let [passed-time (update-time! owner)
            data (assoc-in data [:universe :time] passed-time)]
        (when (<= (get-in data [:rocket :y]) 10)
          (score-transitions))
        (when (collision data)
          (collision-transitions))
        (sab/html [:div.board {:onClick (fn [] (move (:rocket data)))} 
                   (render-planets data)
                   (render-rocket (:rocket data))])))))


(defn main []
  (om/root
   #'game-board
   app-state
   {:target (. js/document (getElementById "app"))}))


(main)
