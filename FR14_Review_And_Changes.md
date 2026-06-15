# FR-14 Bill Scanner — Code Review & Changes

**Branch:** `FR-14/One-click-expense-calculator(Bill-scanner)` → target `dev`
**Scope:** review the FR-14 feature for correctness and consistency, and fix the failing pipeline (`e2e` and `frontend` test stages) before merge.

---

## 1. Feature summary

**Requirement (PA):** FR-14 "One-click expense generation" (UC-03, *Extended*). The project assignment frames it as a *one-click expense from shopping*.

**As implemented:** a **receipt / bill scanner** on the `/expenses` page:

1. Open **Bill Scanner** → open the device camera → capture a frame.
2. OCR runs through the **Google Vision API** and extracts title / date / total.
3. The user reviews and corrects the fields and selects participants from the existing split list.
4. `POST /api/v1/expenses/bill-preview` returns an equal-split draft (persists nothing).
5. Final save reuses the existing FR-05 flow: `POST /api/v1/expenses`.

The bill-scanner interpretation is reasonable (the feature title includes "Bill Scanner"), but note it has **no shopping-list integration** and is not literally "one-click" (camera → capture → review → participants → generate → create).

---

## 2. Pipeline failure — root cause & fix

### Root cause
When OCR was switched to the Google Vision API, `frontend/src/environments/environment.ts` and `environment.prod.ts` were **deleted and added to `.gitignore`** (they can hold the `googleVisionApiKey`); only `environment.ts.example` is tracked. Both `main.ts` and `receipt-ocr.service.ts` import `environment`, so on a fresh checkout the Angular build cannot resolve it:

- `test-frontend` (`npm run build`) → bundle generation fails.
- `e2e-test` (`npm start`) → dev server fails to compile → Cypress never runs.

This was reproduced locally: with no `environment.ts`, `npm run build` fails with `Cannot find module '../../environments/environment'`; with the file present, it passes.

### Fix
`.gitlab-ci.yml` now generates the environment files from the tracked example in both the `test-frontend` and `e2e-test` jobs, injecting the real key from a protected `GOOGLE_VISION_API_KEY` CI/CD variable when it is set:

```yaml
- cp src/environments/environment.ts.example src/environments/environment.ts
- 'if [ -n "${GOOGLE_VISION_API_KEY:-}" ]; then sed -i "s|YOUR_GOOGLE_VISION_API_KEY|${GOOGLE_VISION_API_KEY}|" src/environments/environment.ts; fi'
- "sed 's/production: false/production: true/' src/environments/environment.ts > src/environments/environment.prod.ts"
```

Verified: the generated files build cleanly in both the development and production configurations.

> **Local development note:** because the env files are gitignored, a fresh clone needs
> `cp src/environments/environment.ts.example src/environments/environment.ts` once before building.

---

## 3. Review findings

A review across six areas (OCR parsing, frontend component, backend, build/CI config, security, documentation) produced **39 confirmed findings**. The significant ones:

### Blocking

| # | Area | Finding |
|---|------|---------|
| B1 | Config | `environment.ts` gitignored/deleted but required by the build → `test-frontend` and `e2e-test` fail. |
| B2 | Tests | `expenses.component.spec.ts` mocked `ReceiptOcrService.scanReceipt` and called `component.scanReceiptImage(...)` — neither exists after the camera migration — so `npm test` failed to compile (the "276 passing" claim was false). |

### OCR parsing (`receipt-ocr.service.ts`)

| # | Sev | Finding |
|---|-----|---------|
| O1 | High | Whole-euro totals were dropped — the amount regex forced a decimal group, so `EUR 18` parsed as `null`. |
| O2 | High | Thousands separators truncated the amount — `1.234,56` parsed as `234.56` (order-of-magnitude error). |
| O3 | High | Dates were read as money — `07.06.2026` matched as `7.06` and corrupted the detected total. |
| O4 | Medium | On a total line with several numbers, the *last* number was kept instead of the total. |
| O5 | Medium | Two-digit-year European dates (`dd.mm.yy`) never matched and silently fell back to "today". |

### Frontend component (`expenses.component.ts`)

| # | Sev | Finding |
|---|-----|---------|
| C1 | High | Closing the Bill Scanner panel (toggle) or a successful preview left the **live camera stream running** (`isCameraActive` desynced). |
| C2 | Medium | `captureAndScan()` could send a 0×0 blank canvas to OCR when the video frame was not ready; `getContext('2d')!` could throw. |

### Documentation (`FR14_QA_Form_…md`)

Multiple stale claims from the previous (tesseract.js / file-upload) design: wrong OCR engine, nonexistent method names, "file upload" wording, and incorrect test counts ("276" frontend, "2" e2e).

### Backend (`ExpenseServiceImpl` / `ExpenseEndpoint`) — sound, minor notes only

The `bill-preview` endpoint is correctly secured (`@PreAuthorize`, server-derived payer, household-scoped participants, persists nothing). Minor notes: an outsider participant returns 404 (validation order) rather than 409; the duplicate-participant and "too-small-to-split" branches are untested; a couple of message-wording / status-code nits. None block merge.

### Security — see §6.

---

## 4. Changes applied

| File | Change |
|------|--------|
| `.gitlab-ci.yml` | Generate `environment.ts` / `environment.prod.ts` in `test-frontend` and `e2e-test`; inject key from a protected CI variable. |
| `frontend/src/app/services/receipt-ocr.service.ts` | Rewrote amount/date parsing: locale-aware number parsing (handles whole numbers, `,`/`.` decimals and thousands separators), date tokens excluded from amount scanning, largest figure used on a labelled total line, two-digit-year dates supported. The merchant title is taken from the first non-total line containing letters, so a title with digits (e.g. "FR14 MARKET") is not skipped in favour of a date line. |
| `frontend/src/app/services/receipt-ocr.service.spec.ts` | Added edge-case tests (whole-euro total, thousands separator, date-not-an-amount, multi-amount total line, two-digit-year date). |
| `frontend/src/app/components/expenses/expenses.component.ts` | Central `closeBillCalculator()` stops the camera on panel close and on a successful preview; `captureAndScan()` also handles a null canvas context. |
| `frontend/src/app/components/expenses/expenses.component.spec.ts` | Replaced the stale upload tests with the camera flow (`scanCanvas` / `captureAndScan`): scan success, no-amount, scan error, not-ready frame, and camera-stop-on-close. |
| `frontend/src/app/components/expenses/expenses.component.html` | Added an in-flow disclosure that the captured photo is sent to Google Vision. |
| `frontend/src/app/components/privacy/privacy.component.html` | Added a "Third-party processing (receipt scanning)" section. |
| `frontend/package.json` / `package-lock.json` | Removed the unused `tesseract.js` dependency. |
| `e2e/cypress/fixtures/fr14-receipt.png` | Removed (dead fixture from the abandoned upload flow). |
| `FR14_QA_Form_…md` | Corrected the OCR engine, method names, capture flow, and test counts; added an implementation-update / security note. |

---

## 5. Verification

| Check | Result |
|-------|--------|
| `npm run lint` | Pass |
| `npm test -- --watch=false --browsers=ChromeHeadless` | Pass — **285** specs |
| `npm run build` (development) and production build | Pass |
| `mvn test` (backend, unchanged) | Pass |
| `.gitlab-ci.yml` | Valid; env files generated for `test-frontend` and `e2e-test` |
| `fr14-bill-scanner.cy.js` | 3 / 3 passing — run locally against the backend and dev server |

The Cypress spec stubs the camera and the Vision API, so the e2e stage does not need a real key or network access. All three FR-14 specs were executed locally against the running backend and the Angular dev server and pass.

---

## 6. Security — Vision API key handling

**Issue:** `ReceiptOcrService` reads `environment.googleVisionApiKey` in the browser, so the key is compiled into the shipped bundle and is visible to any user (it can be replayed and billed to the project's Google Cloud account). The receipt image is also sent directly from the browser to Google. The backend itself is not the source of the risk — it never receives the image and derives the payer from the authenticated user. (Today the repository only contains a placeholder key; the real exposure occurs when a live key is deployed.)

**Decision for this milestone — harden client-side, with a proxy as follow-up:**

- The real key is kept only in the protected `GOOGLE_VISION_API_KEY` CI/CD variable; the repository and `environment.ts.example` contain only a placeholder.
- The scanner UI and the privacy page disclose the transfer to Google Vision; manual entry is the no-transfer fallback.
- **Remaining action (Google Cloud console):** restrict the deployed key to the Vision API only, add an HTTP-referrer restriction to the deployed origin, and set a usage quota / budget alert.

**Recommended follow-up:** move OCR behind an authenticated backend proxy so the key stays server-side, the image is processed server-to-server and discarded, and the third-party transfer becomes a documented processor relationship.

---

## 7. Merge readiness

The two blocking issues (the pipeline build failure and the non-compiling unit spec) are resolved, the backend is unchanged and green, the frontend lint/test/build all pass, and the FR-14 end-to-end suite passes (3 / 3, run locally against the backend and dev server). The branch is ready to merge into `dev`. The security item is documented with an interim mitigation and a tracked follow-up.
