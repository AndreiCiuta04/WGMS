# WGMS — FR14 QA Form: One-Click Expense Calculator / Bill Scanner

## Scope

This QA form checks **FR14 — One-Click Expense Calculator / Bill Scanner**.

FR14 is an extended UC-03 feature. It should make expense entry faster while preserving the deterministic expense/split/payment logic. It must not break FR05 shared expense tracking or FR06 payment status/settlement.

This QA form avoids repeating the full general QA/GDPR checklist. Shared requirements are checked once in the NFR section.

---

# FR14-01 — Bill Input / Scan / Calculator Flow

## Requirement

The user should be able to create a shared expense faster through a one-click calculator or bill-scanner-style flow.

## Checklist

- [ ] Entry point is visible from the expense flow.
- [ ] User can input or scan bill data according to implemented scope.
- [ ] Extracted/manual bill data can be reviewed before saving.
- [ ] User can correct parsed amount/title/category/participants before expense creation.
- [ ] Invalid bill input is rejected.
- [ ] Manual fallback exists if scanning/parsing fails.
- [ ] User is not forced to save incorrect parsed data.

## Test coverage required

### Backend

- [ ] Test for calculator/bill request validation.
- [ ] Test for invalid amount/empty description/missing participants.
- [ ] Test for successful conversion into an Expense.
- [ ] Test for rounding with `BigDecimal` / two decimal places.

### Frontend

- [ ] Component test opens calculator/scanner flow.
- [ ] Component test reviews/corrects parsed/manual data.
- [ ] Component test shows validation errors.

### E2E

- [ ] User creates an expense through the one-click flow.
- [ ] User reviews the generated data before saving.
- [ ] Created expense appears in expense list/payment status.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
Frontend tests:
E2E tests:
Manual QA:
```

---

# FR14-02 — Expense Calculation Correctness

## Requirement

Expenses created through FR14 must follow the same deterministic logic as normal expense creation.

## Checklist

- [ ] Total amount is stored correctly.
- [ ] Splits are calculated correctly.
- [ ] Equal split works.
- [ ] Unequal/custom split works if supported.
- [ ] Rounding is deterministic.
- [ ] Payment status/balances update correctly.
- [ ] Generated expense does not duplicate accidentally on repeated clicks.

## Test coverage required

### Backend

- [ ] Unit test for equal split from generated expense.
- [ ] Unit test for rounding edge case.
- [ ] Integration test verifies payment status after generated expense.
- [ ] Regression test proves normal expense creation still works.

### Frontend

- [ ] UI displays calculated total/splits before saving.
- [ ] UI displays created expense after saving.

### E2E

- [ ] Generated expense updates balances/payment status.
- [ ] Repeated click does not create unintended duplicate expense.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
Frontend tests:
E2E tests:
Manual QA:
```

---

# FR14-03 — Integration with Expense and Shopping History

## Requirement

FR14 must not break existing expense tracking, settlement, payments, or shopping history if bill data is created from shopping items.

## Checklist

- [ ] Existing manual expense creation still works.
- [ ] Existing edit/delete expense behavior still works where implemented.
- [ ] Payment status overview still works.
- [ ] Settlement calculations still work.
- [ ] History shows generated expenses correctly.
- [ ] If created from shopping items, shopping history remains consistent.

## Test coverage required

- [ ] Regression test for normal expense creation.
- [ ] Regression test for payment status after expense creation.
- [ ] Regression test for history display.
- [ ] Regression test for shopping-to-expense path if implemented.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Regression evidence:
```

---

# FR14-04 — Shared NFR Coverage

## Requirement

FR14 must satisfy the shared NFRs without duplicating the full general checklist.

## Checklist

### NFR-01 Security & Access Control

- [ ] Requires authentication.
- [ ] Backend derives payer/actor from auth context where appropriate.
- [ ] User cannot create expenses for another WG.
- [ ] Direct API calls cannot bypass validation.

### NFR-02 Data Privacy / GDPR

- [ ] Bill content is limited to necessary expense data.
- [ ] Uploaded/scanned bill data is not stored longer than needed unless documented.
- [ ] Logs do not contain full private bill/payment details.
- [ ] No real banking/payment data is processed.

### NFR-03 Reliability

- [ ] Failed parsing/scanning does not create partial expenses.
- [ ] Database state remains consistent after failed save.
- [ ] Created expenses persist after refresh/re-login.

### NFR-04 Usability & Compatibility

- [ ] Manual fallback is available if scan/parser is not supported.
- [ ] User can understand and correct generated data.
- [ ] Flow works on desktop reference viewport; mobile limitations are documented if scanning is limited.

### NFR-05 Maintainability

- [ ] Parsing/calculation logic is separated from UI.
- [ ] Expense creation reuses existing expense/settlement services where possible.
- [ ] No duplicated settlement logic.

### NFR-06 Auditability

- [ ] Generated expense has timestamp and responsible user.
- [ ] History/audit trail identifies it as an expense like any other expense.
- [ ] If scanner/calculator source is stored, it is documented.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
NFR evidence:
```

---

# Final FR14 QA Summary

| Area | Status | Evidence |
|---|---|---|
| Calculator/scanner flow | [ ] Pass [ ] Partial [ ] Fail | |
| Calculation correctness | [ ] Pass [ ] Partial [ ] Fail | |
| Expense/payment/history regression | [ ] Pass [ ] Partial [ ] Fail | |
| Backend tests | [ ] Pass [ ] Partial [ ] Fail | |
| Frontend tests | [ ] Pass [ ] Partial [ ] Fail | |
| E2E tests | [ ] Pass [ ] Partial [ ] Fail | |
| Shared NFRs | [ ] Pass [ ] Partial [ ] Fail | |

## Required verification commands

```bash
cd backend
./mvnw test

cd ../frontend
npm test -- --watch=false
npm run build

cd ../e2e
npx cypress run
```

## QA sign-off

```text
Reviewer:
Date:
Branch / commit:
Result:
```
