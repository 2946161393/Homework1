#!/bin/bash


echo "========================================="
echo "Building all microservices..."
echo "========================================="

echo "Building root project..."
mvn clean install -DskipTests

echo "Building discovery-service..."
cd discovery-service && mvn clean package -DskipTests && cd ..

echo "Building config-server..."
cd config-server && mvn clean package -DskipTests && cd ..

echo "Building api-gateway..."
cd api-gateway && mvn clean package -DskipTests && cd ..

echo "Building employee-service..."
cd employee-service && mvn clean package -DskipTests && cd ..

echo "Building department-service..."
cd department-service && mvn clean package -DskipTests && cd ..

echo "Building product-service..."
cd product-service && mvn clean package -DskipTests && cd ..

echo "========================================="
echo "Build completed successfully!"
echo "========================================="