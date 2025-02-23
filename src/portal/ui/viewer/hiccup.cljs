(ns portal.ui.viewer.hiccup
  (:require [portal.colors :as c]
            [portal.ui.inspector :as ins]
            [portal.ui.select :as select]
            [portal.ui.styled :as s]
            [portal.ui.theme :as theme]
            [portal.ui.viewer.code :as code]))

(defn- header-styles [theme]
  {:color (::c/namespace theme)
   :padding-top (:padding theme)
   :padding-bottom (:padding theme)
   :margin-bottom (* 2 (:padding theme))})

(defn- hiccup-styles [theme]
  (let [h (header-styles theme)
        border-bottom
        {:border-bottom
         (str "1px solid " (::c/border theme))}]
    {:h1 (merge h border-bottom)
     :h2 (merge h border-bottom)

     :h3 h :h4 h :h5 h :h6 h

     :a {:color (::c/uri theme)}

     :p {:font-family "-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji"
         :font-size (:font-size theme)
         :line-height "1.5em"
         :margin-top 0
         :margin-bottom (* 2 (:padding theme))}

     :img {:max-width "100%"}

     :code
     {:background (::c/background2 theme)
      :border-radius (:border-radius theme)
      :box-sizing :border-box
      :padding [0 (* 1.5 (:padding theme))]}

     :pre
     {:overflow :auto
      :background (::c/background2 theme)
      :border-radius (:border-radius theme)}

     :table {:color (::c/text theme)
             :font-size (:font-size theme)
             :max-width "100%"
             :width :max-content
             :overflow :auto
             :border-spacing 0
             :border-collapse :collapse}
     :th {:padding (:padding theme)
          :border [1 :solid (::c/border theme)]}
     :td {:padding (:padding theme)
          :border [1 :solid (::c/border theme)]}}))

(defn inspect-code [& args]
  (let [[_ attrs code] (second args)
        theme          (theme/use-theme)]
    [s/div
     {:style
      {:margin-top    (:padding theme)
       :margin-bottom (:padding theme)}}
     [code/inspect-code attrs code]]))

(def tap->viewer
  {:pre inspect-code})

(defn- process-hiccup [context hiccup]
  (if-not (vector? hiccup)
    hiccup
    (let [[tag & args]  hiccup
          style         (get-in context [:styles tag])
          component     (or (get-in context [:viewers tag :component])
                            (tap->viewer tag))
          has-attrs?    (map? (first args))]
      (if component
        [ins/dec-depth
         [select/with-position
          {:row 0 :column 0}
          (into [component] args)]]
        (into
         [s/styled
          tag
          (cond->
           (if-not has-attrs?
             {:style style}
             (update (first args) :style #(merge style (s/parse-style %))))
            (or (= tag :a) (= tag "a"))
            (assoc :target "_blank"))]
         (map #(process-hiccup context %)
              (if-not has-attrs? args (rest args))))))))

(defn inspect-hiccup [value]
  (let [theme   (theme/use-theme)
        styles  (hiccup-styles theme)
        viewers (ins/viewers-by-name @ins/viewers)]
    [ins/with-key
     :portal.viewer/hiccup
     (process-hiccup {:styles styles :viewers viewers} value)]))

(defn hiccup? [value]
  (and (vector? value)
       (keyword? (first value))))

(def viewer
  {:predicate hiccup?
   :component inspect-hiccup
   :name :portal.viewer/hiccup})
