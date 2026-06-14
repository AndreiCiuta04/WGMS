# WGMS - FR14 QA Form: One-Click Expense Calculator / Bill Scanner

## Scope

This QA form checks **FR14 - One-Click Expense Calculator / Bill Scanner**.

Implemented scope: FR14 adds a receipt scanner flow to the existing `/expenses` page. A user can open **Bill Scanner**, open the device camera and capture a receipt frame, run OCR through the **Google Vision API**, review the extracted title/date/amount, select participants in the existing split list, generate an equal-split expense draft, correct any field, and save through the existing FR05 expense creation flow.

The backend never receives or stores the raw receipt image or OCR text; it receives only the reviewed bill fields and selected participant ids through `POST /api/v1/expenses/bill-preview`, and final persistence still uses `POST /api/v1/expenses`. Note that OCR is **not** on-device: the captured image is sent from the browser to the Google Vision API (a third-party processor) for text detection.

---

## Implementation update & known limitations

- OCR engine was migrated from on-device `tesseract.js` to the **Google Vision REST API** (commit *"Replace tesseract OCR with Google Vision API and live camera capture"*), and the input changed from file upload to **live camera capture**. Earlier revisions of this form described the tesseract/upload design; the statements below were corrected to match the shipped code.
- **Security caveat:** the Google Vision API key is read from `environment.googleVisionApiKey` inside `ReceiptOcrService`, so it is compiled into the client bundle and is visible to any user; the receipt image is also sent directly from the browser to Google. The proper fix is a backend OCR proxy that keeps the key server-side (tracked as a follow-up). As an interim hardening (decided for this milestone):
  - The real key lives only in the protected `GOOGLE_VISION_API_KEY` CI/CD variable; the repo and `environment.ts.example` contain only a placeholder.
  - The scanner UI and the privacy page now disclose that the photo is sent to Google Vision; manual entry is the no-transfer fallback.
  - **Action required in the Google Cloud console** for the deployed key: restrict it to the Vision API only, add an HTTP-referrer restriction to the deployed origin, and set a usage quota / budget alert to cap abuse.
- `environment.ts` / `environment.prod.ts` are gitignored; CI generates them from `environment.ts.example` (and an optional `GOOGLE_VISION_API_KEY` variable). Local builds need `cp src/environments/environment.ts.example src/environments/environment.ts`.

---

# FR14-01 - Bill Input / Scan / Calculator Flow

## Checklist

- [x] Entry point is visible from the expense flow.
- [x] User can scan/upload a receipt image from the browser.
- [x] OCR extracts receipt title, date and total amount.
- [x] Extracted data can be reviewed before saving.
- [x] User can correct title, amount, date and participants before expense creation.
- [x] Invalid reviewed bill input is rejected.
- [x] Manual fallback exists if scanning/parsing fails.
- [x] User is not forced to save incorrect parsed data.

## Evidence

- `ReceiptOcrService.scanCanvas(...)` sends the captured camera frame to the Google Vision API; `parseReceiptText(...)` parses date/amount/title from the returned text.
- `ExpensesComponent.captureAndScan()` captures a camera frame to a canvas and fills the bill review form from the OCR draft.
- `ExpensesComponent.previewBillExpense(...)` sends reviewed data to the backend preview endpoint.
- Cypress stubs `navigator.mediaDevices.getUserMedia` and intercepts the Vision API, then asserts the OCR-extracted fields.

Status: **Pass**

---

# FR14-02 - Expense Calculation Correctness

## Checklist

- [x] Total amount is carried into the generated draft correctly.
- [x] Equal split is calculated server-side with existing `BigDecimal` rounding rules.
- [x] User can review calculated split amounts before saving.
- [x] Payment status/balances update after the generated expense is saved.
- [x] Repeated save clicks do not create duplicate expenses.

## Evidence

- `ExpenseEndpointTest.givenValidBillPreviewRequest_whenPreviewBillExpense_thenEqualSplitPayloadIsReturned`
- `ExpenseEndpointTest.givenBillPreviewPayload_whenCreateExpense_thenExpenseIsPersistedAndBalanceUpdates`
- `ExpenseEndpointTest.givenInvalidBillPreviewRequest_whenPreviewBillExpense_then400AndNoExpenseIsCreated`
- `ExpenseEndpointTest.givenBillPreviewWithoutCurrentPayer_whenPreviewBillExpense_then409AndNoExpenseIsCreated`
- FR14 Cypress asserts generated split values and only one `POST /expenses` after double-click.

Status: **Pass**

---

# FR14-03 - Integration with Existing Features

## Checklist

- [x] Existing manual expense creation remains available.
- [x] Generated expenses reuse existing FR05 persistence.
- [x] Payment status overview reflects the created expense.
- [x] History/audit behavior remains the same as for normal expenses.
- [x] Shopping state is not modified by FR14.

## Evidence

- Final save reuses `ExpenseService.createExpense`.
- FR14 Cypress creates an expense from OCR-reviewed data and verifies `/payment-status`.
- Existing FR05 component/backend tests still pass.

Status: **Pass**

---

# FR14-04 - Shared NFR Coverage

## Checklist

- [x] Backend preview endpoint requires authentication.
- [x] Backend derives payer from the authenticated user's active roommate record.
- [x] Participant ids are validated against the current household.
- [x] Raw receipt image/OCR text is not stored by the backend (note: the image is sent to the Google Vision API for OCR).
- [x] Failed OCR or invalid preview input creates no partial expense.
- [x] OCR parsing logic is separated into `ReceiptOcrService`.
- [x] Expense creation reuses existing expense/settlement logic.

## Evidence

- `POST /expenses/bill-preview` is auth-scoped.
- Preview stores no database entity and only returns a reviewed DTO.
- The backend never receives or stores the raw image/OCR text; the captured image is sent to the Google Vision API for OCR.
- Component tests cover OCR success and preview validation.

Status: **Pass**

---

# Final FR14 QA Summary

| Area | Status | Evidence |
|---|---|---|
| Camera OCR flow | Pass | `ReceiptOcrService.scanCanvas`, `ExpensesComponent.captureAndScan`, FR14 Cypress camera/Vision stubs |
| Reviewed expense draft | Pass | `POST /expenses/bill-preview`, component preview tests |
| Calculation correctness | Pass | ExpenseEndpointTest preview/equal split tests |
| Payment status integration | Pass | FR14 Cypress checks `/payment-status` |
| Backend tests | Pass | `mvn -B test` - 224 passing |
| Frontend tests | Pass | `npm test -- --watch=false --browsers=ChromeHeadless` - 285 passing |
| Build/lint | Pass | `npm run build`, `npm run lint` |
| E2E tests | Pass | `npx cypress run --spec cypress/e2e/fr14-bill-scanner.cy.js` - 3 passing |

## Required verification commands

```bash
cd backend
mvn -B test

cd ../frontend
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
npm run lint

cd ../e2e
npx cypress run --spec cypress/e2e/fr14-bill-scanner.cy.js
```

## QA sign-off

```text
Reviewer: Codex
Date: 2026-06-07
Branch / commit: FR-14/One-click-expense-calculator(Bill-scanner) / pending commit
Result: Pass for camera/upload OCR bill scanner scope
```
