#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
E2E_DIR="$PROJECT_ROOT/e2e"

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:4200}"
BACKEND_PROFILE="${BACKEND_PROFILE:-generateData}"
BACKEND_DB_URL="${BACKEND_DB_URL:-jdbc:h2:mem:e2e-runner;DB_CLOSE_DELAY=-1}"

BACKEND_PID=""
FRONTEND_PID=""

usage() {
  cat <<'USAGE'
WGMS E2E Test Runner

Usage:
  ./run-e2e.sh                         Open interactive menu
  ./run-e2e.sh --all                   Run the complete Cypress suite against an already running app
  ./run-e2e.sh --all --with-stack      Start backend/frontend, run the complete suite, then stop them
  ./run-e2e.sh --spec <pattern>        Run one Cypress spec or glob
  ./run-e2e.sh --fr                    Run all FR specs
  ./run-e2e.sh --usecases              Run all use-case specs

Options:
  --with-stack       Build/start backend and start Angular dev server before Cypress
  --open             Open Cypress UI instead of headless run
  --backend-url URL  Backend health URL base, default http://localhost:8080
  --frontend-url URL Frontend URL base, default http://localhost:4200
  -h, --help         Show this help

Environment overrides:
  BACKEND_PROFILE=generateData
  BACKEND_DB_URL='jdbc:h2:mem:e2e-runner;DB_CLOSE_DELAY=-1'
USAGE
}

require_dirs() {
  for dir in "$BACKEND_DIR" "$FRONTEND_DIR" "$E2E_DIR"; do
    if [ ! -d "$dir" ]; then
      echo "ERROR: required directory not found: $dir" >&2
      exit 1
    fi
  done
}

cleanup() {
  if [ -n "$FRONTEND_PID" ] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
    kill "$FRONTEND_PID" 2>/dev/null || true
    wait "$FRONTEND_PID" 2>/dev/null || true
  fi
  if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
  fi
}

wait_for_url() {
  local label="$1"
  local url="$2"
  local attempts="${3:-90}"

  for _ in $(seq 1 "$attempts"); do
    if curl --silent --fail "$url" >/dev/null; then
      echo "$label is ready: $url"
      return 0
    fi
    sleep 2
  done

  echo "ERROR: timed out waiting for $label at $url" >&2
  return 1
}

ensure_e2e_deps() {
  if [ ! -d "$E2E_DIR/node_modules" ]; then
    echo "Installing E2E dependencies..."
    (cd "$E2E_DIR" && npm ci --legacy-peer-deps)
  fi
}

start_stack() {
  echo "Building backend jar..."
  (cd "$BACKEND_DIR" && mvn -B -DskipTests package)

  echo "Starting backend with profile '$BACKEND_PROFILE'..."
  mkdir -p "$BACKEND_DIR/log"
  (
    cd "$BACKEND_DIR"
    java -jar target/backend-0.0.1-SNAPSHOT.jar \
      --spring.profiles.active="$BACKEND_PROFILE" \
      --wgms.mail.verification-enabled=false \
      "--spring.datasource.url=$BACKEND_DB_URL" \
      --spring.datasource.username=sa \
      "--spring.datasource.password=" \
      --spring.jpa.hibernate.ddl-auto=create \
      --logging.file.name=./log/e2e-runner-backend.log
  ) &
  BACKEND_PID=$!

  echo "Starting frontend dev server..."
  (cd "$FRONTEND_DIR" && npm start -- --host 0.0.0.0 --port 4200) &
  FRONTEND_PID=$!

  wait_for_url "Backend" "$BACKEND_URL/health"
  wait_for_url "Frontend" "$FRONTEND_URL/"
}

run_headless() {
  local spec="$1"

  ensure_e2e_deps
  cd "$E2E_DIR"

  if [ "$spec" = "ALL" ]; then
    npx cypress run
  else
    npx cypress run --spec "$spec"
  fi
}

run_interactive() {
  local spec="$1"

  ensure_e2e_deps
  cd "$E2E_DIR"

  if [ "$spec" = "ALL" ]; then
    npx cypress open
  else
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
  echo "  2) Interactive Cypress UI"
  echo "  b) Back to main menu"
  echo ""

  read -rp "Enter mode: " mode

  case "$mode" in
    1)
      echo "Running headless: $label"
      run_headless "$spec"
      ;;
    2)
      echo "Opening interactive Cypress UI for: $label"
      run_interactive "$spec"
      ;;
    b|B)
      main_menu
      ;;
    *)
      echo "Invalid mode: $mode" >&2
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
  echo "- Backend is running on $BACKEND_URL"
  echo "- Frontend is running on $FRONTEND_URL"
  echo ""
  echo "For a self-contained full run, use:"
  echo "  ./run-e2e.sh --all --with-stack"
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
  echo " 12) FR-10 Shopping Suggestions"
  echo " 13) FR-11 Notifications"
  echo " 14) FR-12 Dashboard"
  echo " 15) FR-13 History"
  echo " 16) FR-14 Bill Scanner"
  echo " 17) FR-15 Fair Chore Balancing"
  echo " 18) FR-16 Statistical History"
  echo " 19) FR-17 Swipe Settlement Approval"
  echo " 20) FR-18 Shopping QR Scan"
  echo ""
  echo " 21) All use-case specs"
  echo " 22) UC-01 Onboarding + Household Invite"
  echo " 23) UC-02 Task Lifecycle"
  echo " 24) UC-03 Cleaning Schedule to Task"
  echo " 25) UC-04 Shopping Collaboration"
  echo " 26) UC-05 Expense Tracking + Payment Status + History"
  echo " 27) UC-06 Dashboard Overview"
  echo " 28) UC-07 Navigation Smoke"
  echo ""
  echo "  q) Quit"
  echo ""

  read -rp "Enter choice: " choice

  case "$choice" in
    1) run_selected "All E2E tests" "ALL" ;;
    2) run_selected "All FR feature specs" "cypress/e2e/fr*.cy.js" ;;
    3) run_selected "FR-01 Auth" "cypress/e2e/fr01-auth.cy.js" ;;
    4) run_selected "FR-02 Household Invitations" "cypress/e2e/fr02-household-invitations.cy.js" ;;
    5) run_selected "FR-03 Tasks" "cypress/e2e/fr03-tasks.cy.js" ;;
    6) run_selected "FR-04 Cleaning Schedules" "cypress/e2e/fr04-cleaning-schedules.cy.js" ;;
    7) run_selected "FR-05 Expenses" "cypress/e2e/fr05-expenses.cy.js" ;;
    8) run_selected "FR-06 Payment Status" "cypress/e2e/fr06-payment-status.cy.js" ;;
    9) run_selected "FR-07 Shopping CRUD" "cypress/e2e/fr07-shopping-crud.cy.js" ;;
    10) run_selected "FR-08 Targeted Shopping" "cypress/e2e/fr08-shopping-targeted.cy.js" ;;
    11) run_selected "FR-09 Shopping Aggregation/History" "cypress/e2e/fr09-shopping-aggregation-history.cy.js" ;;
    12) run_selected "FR-10 Shopping Suggestions" "cypress/e2e/fr10-shopping-suggestions.cy.js" ;;
    13) run_selected "FR-11 Notifications" "cypress/e2e/fr11-notifications.cy.js" ;;
    14) run_selected "FR-12 Dashboard" "cypress/e2e/fr12-dashboard.cy.js" ;;
    15) run_selected "FR-13 History" "cypress/e2e/fr13-history.cy.js" ;;
    16) run_selected "FR-14 Bill Scanner" "cypress/e2e/fr14-bill-scanner.cy.js" ;;
    17) run_selected "FR-15 Fair Chore Balancing" "cypress/e2e/fr15-fair-chore-balancing.cy.js" ;;
    18) run_selected "FR-16 Statistical History" "cypress/e2e/fr16-statistical-history-view.cy.js" ;;
    19) run_selected "FR-17 Swipe Settlement Approval" "cypress/e2e/fr17-swipe-settlement-approval.cy.js" ;;
    20) run_selected "FR-18 Shopping QR Scan" "cypress/e2e/fr18-shopping-qr-scan.cy.js" ;;
    21) run_selected "All use-case specs" "cypress/e2e/usecases/*.cy.js" ;;
    22) run_selected "UC-01 Onboarding + Household Invite" "cypress/e2e/usecases/uc01-onboarding-household.cy.js" ;;
    23) run_selected "UC-02 Task Lifecycle" "cypress/e2e/usecases/uc02-task-lifecycle.cy.js" ;;
    24) run_selected "UC-03 Cleaning Schedule to Task" "cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js" ;;
    25) run_selected "UC-04 Shopping Collaboration" "cypress/e2e/usecases/uc04-shopping-collaboration.cy.js" ;;
    26) run_selected "UC-05 Expense Tracking + Payment Status + History" "cypress/e2e/usecases/uc05-expense-history.cy.js" ;;
    27) run_selected "UC-06 Dashboard Overview" "cypress/e2e/usecases/uc06-dashboard-overview.cy.js" ;;
    28) run_selected "UC-07 Navigation Smoke" "cypress/e2e/usecases/uc07-navigation-smoke.cy.js" ;;
    q|Q) echo "Cancelled."; exit 0 ;;
    *) echo "Invalid choice: $choice" >&2; exit 1 ;;
  esac
}

run_direct() {
  local spec="ALL"
  local open_ui=false
  local with_stack=false

  while [ "$#" -gt 0 ]; do
    case "$1" in
      --all)
        spec="ALL"
        shift
        ;;
      --fr)
        spec="cypress/e2e/fr*.cy.js"
        shift
        ;;
      --usecases)
        spec="cypress/e2e/usecases/*.cy.js"
        shift
        ;;
      --spec)
        if [ "$#" -lt 2 ]; then
          echo "ERROR: --spec requires a spec path or glob" >&2
          exit 1
        fi
        spec="$2"
        shift 2
        ;;
      --with-stack)
        with_stack=true
        shift
        ;;
      --open)
        open_ui=true
        shift
        ;;
      --backend-url)
        BACKEND_URL="$2"
        shift 2
        ;;
      --frontend-url)
        FRONTEND_URL="$2"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "ERROR: unknown argument: $1" >&2
        usage
        exit 1
        ;;
    esac
  done

  if [ "$with_stack" = true ]; then
    trap cleanup EXIT INT TERM
    start_stack
  fi

  if [ "$open_ui" = true ]; then
    run_interactive "$spec"
  else
    run_headless "$spec"
  fi
}

require_dirs

if [ "$#" -eq 0 ]; then
  main_menu
else
  run_direct "$@"
fi

echo ""
echo "========================================"
echo " Done"
echo "========================================"
