#!/bin/bash
set -e

# Start frontend
echo "Starting frontend..."
cd frontend
npm run start > frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# Start backend
echo "Starting backend..."
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=generateData > backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "Waiting for services to start..."
sleep 30

# Run cypress
echo "Running Cypress E2E tests..."
cd e2e
npx cypress run > cypress.log 2>&1
CYPRESS_EXIT=$?

# Kill background processes
echo "Cleaning up..."
kill $FRONTEND_PID
kill $BACKEND_PID

exit $CYPRESS_EXIT
