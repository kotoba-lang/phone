(ns kotoba.phone.export
  "Operator-facing export for a telecom-access actor.

  Renders E.164 validation, CDRs and SMS records to CSV and JSON for billing
  audit and downstream reporting. Pure data → text: no network."
  (:require [clojure.string :as str]
            [kotoba.phone :as phone]))

(defn- csv-cell [v]
  (let [s (str (if (nil? v) "" v))]
    (if (re-find #"[\",\n]" s)
      (str "\"" (str/replace s "\"" "\"\"") "\"")
      s)))

(defn- csv-row [vals] (str/join "," (map csv-cell vals)))

(defn- json-str [v]
  (-> (str (if (nil? v) "" v))
      (str/replace "\\" "\\\\")
      (str/replace "\"" "\\\"")
      (str/replace "\n" "\\n")))

(defn numbers->csv [numbers]
  (str/join "\n"
    (cons (csv-row ["number" "valid" "normalized"])
          (for [n numbers]
            (let [r (phone/validate-e164 n)]
              (csv-row [n
                        (if (:phone/valid? r) "yes" "no")
                        (or (:phone/normalized r) "")]))))))

(defn cdrs->csv [cdrs]
  (str/join "\n"
    (cons (csv-row ["cdr_id" "calling" "called" "direction" "duration"])
          (for [c cdrs]
            (csv-row [(:phone/cdr-id c)
                      (or (:phone/calling c) "")
                      (or (:phone/called c) "")
                      (name (:phone/direction c))
                      (:phone/duration c)])))))

(defn sms->csv [messages]
  (str/join "\n"
    (cons (csv-row ["sms_id" "from" "to" "body"])
          (for [m messages]
            (csv-row [(:phone/sms-id m)
                      (or (:phone/from m) "")
                      (or (:phone/to m) "")
                      (:phone/body m)])))))

(defn numbers->json [numbers]
  (str "["
       (str/join ","
                 (for [n numbers]
                   (let [r (phone/validate-e164 n)]
                     (str "{\"number\":\"" (json-str n) "\","
                          "\"valid\":" (if (:phone/valid? r) "true" "false") ","
                          "\"normalized\":\"" (json-str (:phone/normalized r)) "\"}"))))
       "]"))

(defn cdrs->json [cdrs]
  (str "["
       (str/join ","
                 (for [c cdrs]
                   (str "{\"cdr_id\":\"" (json-str (:phone/cdr-id c)) "\","
                        "\"calling\":\"" (json-str (:phone/calling c)) "\","
                        "\"called\":\"" (json-str (:phone/called c)) "\","
                        "\"direction\":\"" (name (:phone/direction c)) "\","
                        "\"duration\":" (or (:phone/duration c) 0) "}")))
       "]"))
