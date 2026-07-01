(ns kotoba.phone.ui
  "Operator-facing console for a telecommunications-access actor.

  Renders an HTML read-only panel of E.164 validation, recent CDRs and SMS
  records, using kotoba-lang/html + css. Pure data → markup: no network.
  The governor gates provisioning/billing; this view only observes."
  (:require [html.core :as html]
            [css.core :as css]
            [kotoba.phone :as phone]))

(def ^:private sheet
  {:rules
   {"body" {:font-family "system-ui,-apple-system,sans-serif" :margin 0 :color "#1a1a1a" :background "#fafafa"}
    "header.bar" {:display :flex :align-items :center :gap 12 :padding "12px 20px" :background "#fff" :border-bottom "1px solid #e5e5e5"}
    "header.bar h1" {:font-size 18 :margin 0 :font-weight 600}
    "header.bar .badge" {:margin-left :auto :font-size 12 :color "#666"}
    "main" {:max-width 960 :margin "24px auto" :padding "0 20px"}
    ".card" {:background "#fff" :border "1px solid #e5e5e5" :border-radius 8 :padding 16 :margin-bottom 16}
    "h2" {:margin-top 0 :font-size 15}
    "table" {:width "100%" :border-collapse :collapse :font-size 14}
    "th, td" {:text-align :left :padding "8px 10px" :border-bottom "1px solid #f0f0f0"}
    "th" {:font-weight 600 :color "#555" :font-size 12 :text-transform :uppercase :letter-spacing "0.04em"}
    "td.amt" {:font-variant-numeric :tabular-nums :text-align :right}
    ".ok" {:color "#137a3f"}
    ".err" {:color "#b3261e" :background "#fbe9e7" :padding "2px 6px" :border-radius 4}
    ".muted" {:color "#888"}}})

(defn- stylesheet [] (html/->html (css/style-node sheet)))

(defn- e164-rows [nums]
  (for [n nums]
    (let [r (phone/validate-e164 n)]
      [:tr [:td (if (:phone/valid? r) [:span.ok "✓"] [:span.err "✕"])]
           [:td (str n)]
           [:td (or (:phone/normalized r) "—")]])))

(defn- cdr-rows [cdrs]
  (for [c cdrs]
    [:tr [:td (:phone/cdr-id c)]
     [:td (or (:phone/calling c) "—")]
     [:td (or (:phone/called c) "—")]
     [:td (name (:phone/direction c))]
     [:td.amt (:phone/duration c)]]))

(defn- sms-rows [messages]
  (for [m messages]
    [:tr [:td (:phone/sms-id m)]
     [:td (or (:phone/from m) "—")]
     [:td (or (:phone/to m) "—")]
     [:td (str (:phone/body m))]]))

(defn dashboard
  "Render a full HTML console for a telecom-access operator."
  [{:keys [numbers cdrs sms] :as ctx}]
  (html/->html
    [:html
     [:head [:meta {:charset "utf-8"}] [:title "cloud-itonami · telecom"]
      [:hiccup/raw (stylesheet)]]
     [:body
      [:header.bar [:h1 "Telecommunications Access — Operator Console"] [:span.badge "read-only · governor-gated"]]
      [:main
       (when (seq numbers)
         [:section.card [:h2 "E.164 validation"]
          [:table [:thead [:tr [:th ""] [:th "Number"] [:th "Normalized"]]]
           [:tbody (e164-rows numbers)]]])
       (when (seq cdrs)
         [:section.card [:h2 "Recent calls (CDR)"]
          [:table [:thead [:tr [:th "ID"] [:th "Calling"] [:th "Called"] [:th "Direction"] [:th.amt "Duration (s)"]]]
           [:tbody (cdr-rows cdrs)]]])
       (when (seq sms)
         [:section.card [:h2 "Recent SMS"]
          [:table [:thead [:tr [:th "ID"] [:th "From"] [:th "To"] [:th "Body"]]]
           [:tbody (sms-rows sms)]]])]]]))
