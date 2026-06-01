#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
E2E_DIR="$PROJECT_ROOT/e2e"

if [ ! -d "$E2E_DIR" ]; then
  echo "ERROR: e2e directory not found at: $E2E_DIR"
  exit 1
fi

cd "$E2E_DIR"

run_headless() {
  local spec="$1"

  if [ "$spec" = "ALL" ]; then
    npx cypress run
  else
    npx cypress run --spec "$spec"
  fi
}

run_interactive() {
  local spec="$1"

  if [ "$spec" = "ALL" ]; then
    npx cypress open
  else
    echo ""
    echo "Opening Cypress UI."
    echo "The selected spec should be available there:"
    echo "$spec"
    echo ""
    npx cypress open --e2e --browser chrome --config "specPattern=$spec"
  fi
}

run_selected() {
  local label="$1"
  local spec="$2"

  echo ""
  echo "Selected suite:"
  echo "  $label"
  echo ""

  echo "Choose run mode:"
  echo ""
  echo "  1) Headless terminal run"
  echo "     - faster"
  echo "     - good for proof / final full run"
  echo "     - you only see terminal output"
  echo ""
  echo "  2) Interactive Cypress UI"
  echo "     - opens Cypress visually"
  echo "     - you can watch the browser clicking, typing, waiting, asserting"
  echo "     - best for demos / debugging"
  echo ""
  echo "  b) Back to main menu"
  echo ""

  read -rp "Enter mode: " mode

  case "$mode" in
    1)
      echo ""
      echo "Running headless: $label"
      run_headless "$spec"
      ;;
    2)
      echo ""
      echo "Opening interactive Cypress UI for: $label"
      run_interactive "$spec"
      ;;
    b|B)
      main_menu
      ;;
    *)
      echo "Invalid mode: $mode"
      exit 1
      ;;
  esac
}

main_menu() {
  echo "========================================"
  echo " WGMS E2E Test Runner"
  echo "========================================"
  echo ""
  echo "Make sure before running:"
  echo "- Backend is running on http://localhost:8080"
  echo "- Frontend is running on the expected dev port"
  echo ""
  echo "Choose what to run:"
  echo ""
  echo "  1) All E2E tests"
  echo "  2) All FR feature specs"
  echo "  3) FR-01 Auth"
  echo "  4) FR-02 Household Invitations"
  echo "  5) FR-03 Tasks"
  echo "  6) FR-04 Cleaning Schedules"
  echo "  7) FR-05 Expenses"
  echo "  8) FR-06 Payment Status"
  echo "  9) FR-07 Shopping CRUD"
  echo " 10) FR-08 Targeted Shopping"
  echo " 11) FR-09 Shopping Aggregation/History"
  echo " 12) FR-12 Dashboard"
  echo " 13) FR-13 History"
  echo ""
  echo " 14) All use-case specs"
  echo " 15) UC-01 Onboarding + Household Invite"
  echo " 16) UC-02 Task Lifecycle"
  echo " 17) UC-03 Cleaning Schedule to Task"
  echo " 18) UC-04 Shopping Collaboration"
  echo " 19) UC-05 Expense Tracking + Payment Status + History"
  echo " 20) UC-06 Dashboard Overview"
  echo " 21) UC-07 Navigation Smoke"
  echo ""
  echo "  q) Quit"
  echo ""

  read -rp "Enter choice: " choice

  case "$choice" in
    1)
      run_selected "All E2E tests" "ALL"
      ;;

    2)
      run_selected "All FR feature specs" "cypress/e2e/fr*.cy.js"
      ;;

    3)
      run_selected "FR-01 Auth" "cypress/e2e/fr01-auth.cy.js"
      ;;

    4)
      run_selected "FR-02 Household Invitations" "cypress/e2e/fr02-household-invitations.cy.js"
      ;;

    5)
      run_selected "FR-03 Tasks" "cypress/e2e/fr03-tasks.cy.js"
      ;;

    6)
      run_selected "FR-04 Cleaning Schedules" "cypress/e2e/fr04-cleaning-schedules.cy.js"
      ;;

    7)
      run_selected "FR-05 Expenses" "cypress/e2e/fr05-expenses.cy.js"
      ;;

    8)
      run_selected "FR-06 Payment Status" "cypress/e2e/fr06-payment-status.cy.js"
      ;;

    9)
      run_selected "FR-07 Shopping CRUD" "cypress/e2e/fr07-shopping-crud.cy.js"
      ;;

    10)
      run_selected "FR-08 Targeted Shopping" "cypress/e2e/fr08-shopping-targeted.cy.js"
      ;;

    11)
      run_selected "FR-09 Shopping Aggregation/History" "cypress/e2e/fr09-shopping-aggregation-history.cy.js"
      ;;

    12)
      run_selected "FR-12 Dashboard" "cypress/e2e/fr12-dashboard.cy.js"
      ;;

    13)
      run_selected "FR-13 History" "cypress/e2e/fr13-history.cy.js"
      ;;

    14)
      run_selected "All use-case specs" "cypress/e2e/usecases/*.cy.js"
      ;;

    15)
      run_selected "UC-01 Onboarding + Household Invite" "cypress/e2e/usecases/uc01-onboarding-household.cy.js"
      ;;

    16)
      run_selected "UC-02 Task Lifecycle" "cypress/e2e/usecases/uc02-task-lifecycle.cy.js"
      ;;

    17)
      run_selected "UC-03 Cleaning Schedule to Task" "cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js"
      ;;

    18)
      run_selected "UC-04 Shopping Collaboration" "cypress/e2e/usecases/uc04-shopping-collaboration.cy.js"
      ;;

    19)
      run_selected "UC-05 Expense Tracking + Payment Status + History" "cypress/e2e/usecases/uc05-expense-history.cy.js"
      ;;

    20)
      run_selected "UC-06 Dashboard Overview" "cypress/e2e/usecases/uc06-dashboard-overview.cy.js"
      ;;

    21)
      run_selected "UC-07 Navigation Smoke" "cypress/e2e/usecases/uc07-navigation-smoke.cy.js"
      ;;

    q|Q)
      echo "Cancelled."
      exit 0
      ;;

    *)
      echo "Invalid choice: $choice"
      exit 1
      ;;
  esac
}

main_menu

echo ""
echo "========================================"
echo " Done"
echo "========================================"
