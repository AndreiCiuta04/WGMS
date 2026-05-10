#!/bin/bash
mvn spring-boot:run -Dspring-boot.run.profiles=generateData > backend_output.log 2>&1 &
PID=$!

echo "Waiting for backend to start..."
while ! nc -z localhost 8080; do   
  sleep 1
done

echo "Triggering registration..."
curl -v -X POST http://localhost:8080/api/v1/users -H "Content-Type: application/json" -d '{"email":"test500@example.com","password":"Password123!","firstName":"Test","lastName":"User"}'

kill $PID
