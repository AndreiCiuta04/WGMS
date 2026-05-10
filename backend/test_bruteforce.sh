#!/bin/bash
# Start backend on port 18080
mvn spring-boot:run -Dspring-boot.run.profiles=generateData -Dspring-boot.run.arguments="--server.port=18080" &
PID=$!

echo "Waiting for backend on port 18080..."
while ! nc -z localhost 18080; do   
  sleep 1
done
echo "Backend is up on 18080!"

echo "Registering user..."
curl -s -X POST http://localhost:18080/api/v1/users -H "Content-Type: application/json" -d '{"email":"brute3@example.com","password":"Password123!","firstName":"Test","lastName":"User"}' > /dev/null

echo "Attempting 5 failed logins..."
for i in {1..5}; do
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:18080/api/v1/authentication -H "Content-Type: application/json" -d '{"email":"brute3@example.com","password":"wrong"}')
  echo "Attempt $i response code: $RESPONSE"
done

echo "Attempting 1 correct login (should fail because locked)..."
curl -v -X POST http://localhost:18080/api/v1/authentication -H "Content-Type: application/json" -d '{"email":"brute3@example.com","password":"Password123!"}'

kill $PID
