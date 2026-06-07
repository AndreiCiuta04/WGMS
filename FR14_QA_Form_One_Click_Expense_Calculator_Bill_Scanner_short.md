# WGMS - FR14 QA Form: One-Click Expense Calculator / Bill Scanner

## Scope

This QA form checks **FR14 - One-Click Expense Calculator / Bill Scanner**.

Implemented scope: FR14 adds a receipt scanner flow to the existing `/expenses` page. A user can open **Bill Scanner**, take/upload a receipt image, run client-side OCR, review the extracted title/date/amount, select participants in the existing split list, generate an equal-split expense draft, correct any field, and save through the existing FR05 expense creation flow.

The raw receipt image and OCR text are not persisted. The backend receives only the reviewed bill fields and selected participant ids through `POST /api/v1/expenses/bill-preview`; final persistence still uses `POST /api/v1/expenses`.

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

- `ReceiptOcrService` runs `tesseract.js` client-side and parses date/amount/title.
- `ExpensesComponent.scanReceiptImage(...)` fills the bill review form from OCR output.
- `ExpensesComponent.previewBillExpense(...)` sends reviewed data to the backend preview endpoint.
- Cypress uploads `e2e/cypress/fixtures/fr14-receipt.png` and waits for OCR-extracted fields.

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
- [x] Raw receipt image/OCR text is not stored.
- [x] Failed OCR or invalid preview input creates no partial expense.
- [x] OCR parsing logic is separated into `ReceiptOcrService`.
- [x] Expense creation reuses existing expense/settlement logic.

## Evidence

- `POST /expenses/bill-preview` is auth-scoped.
- Preview stores no database entity and only returns a reviewed DTO.
- Raw image stays in the browser file input/OCR flow.
- Component tests cover OCR success and preview validation.

Status: **Pass**

---

# Final FR14 QA Summary

| Area | Status | Evidence |
|---|---|---|
| Camera/upload OCR flow | Pass | `ReceiptOcrService`, `ExpensesComponent.scanReceiptImage`, FR14 Cypress fixture upload |
| Reviewed expense draft | Pass | `POST /expenses/bill-preview`, component preview tests |
| Calculation correctness | Pass | ExpenseEndpointTest preview/equal split tests |
| Payment status integration | Pass | FR14 Cypress checks `/payment-status` |
| Backend tests | Pass | `mvn -B test` - 224 passing |
| Frontend tests | Pass | `npm test -- --watch=false --browsers=ChromeHeadless` - 276 passing |
| Build/lint | Pass | `npm run build`, `npm run lint` |
| E2E tests | Pass | `npx cypress run --spec cypress/e2e/fr14-bill-scanner.cy.js` - 2 passing |

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
