# kotoba-phone

[![CI](https://github.com/kotoba-lang/phone/actions/workflows/ci.yml/badge.svg)](https://github.com/kotoba-lang/phone/actions/workflows/ci.yml)

**Telephony numbering, SIP URIs, call records and SMS in pure Clojure.** A
[kotoba-lang](https://github.com/kotoba-lang) capability library that gives
the [`cloud-itonami-6190`](https://github.com/gftdcojp/cloud-itonami-6190)
community telecommunications-access open business the records a telecom
operator keeps: E.164 (ITU-T) international subscriber numbers with
country-code splitting, SIP (RFC 3261) URI parsing, call detail records
(CDR) and SMS message records.

No network, no I/O. The library expects international-form input (a leading
`+` or an international access prefix `00` / `011`); national trunk-prefix
translation is the operator's numbering-plan concern, not the contract layer.
Portable `.cljc` across JVM / ClojureScript / SCI / GraalVM.

## Contract

```clojure
(require '[kotoba.phone :as phone])

(phone/normalize-e164 "+44 20 7946 0958")      ; => "+442079460958"
(phone/parse-e164 "+819012345678")             ; => {:phone/country-code "81" ...}
(phone/parse-sip-uri "sip:alice@example.com;transport=tcp?subject=hi")
(phone/cdr "C1" "+8190A" "+8190B" :outbound 120)
(phone/sms "S1" "+8190A" "+8190B" "hi")
(phone/validate-e164 "x")                      ; => {:phone/valid? false ...}
```

## Operator console (UI/UX)

A read-only HTML dashboard renders E.164 validation, recent calls (CDR) and SMS for an operator. Built on
[`kotoba-lang/html`](https://github.com/kotoba-lang/html) (Hiccup→HTML) +
[`kotoba-lang/css`](https://github.com/kotoba-lang/css) (EDN→CSS). Pure data
→ markup; the console never exposes a write surface (no `<form>`/`<button>`)
— writes stay behind the governor.

```clojure
(require '[kotoba.phone.ui :as ui])

(ui/dashboard
  {:numbers ["+442079460958" "bad"]
   :cdrs [(phone/cdr "C1" "+8190A" "+8190B" :outbound 120)]
   :sms [(phone/sms "S1" "+8190A" "+8190B" "hi")]})
;; => "<html>...read-only · governor-gated...</html>"
```

## Export (CSV / JSON)

Audit-grade CSV (RFC-4180 quoting) and JSON (quote/backslash/newline
escaped) for E.164 validation, CDRs and SMS.

```clojure
(require '[kotoba.phone.export :as ex])

(ex/numbers->csv numbers)  ; valid/normalized
(ex/cdrs->csv cdrs)
(ex/numbers->json numbers)
```

## Why

A community telecom operator must prove, before a call or message is
committed and billed, that the calling and called numbers are valid E.164
addresses and that the SIP endpoint is well-formed. `kotoba-phone` is the
pure-data layer a `PolicyGovernor` checks against; the actor
(`cloud-itonami-6190`) decides permission, the audit ledger records proof.

## License

Apache License 2.0.
