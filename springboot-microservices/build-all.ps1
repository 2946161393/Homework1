

Write-Host "=========================================" -ForegroundColor Green
Write-Host "Building all microservices..." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

Write-Host "Building root project..." -ForegroundColor Yellow
mvn clean install -DskipTests

Write-Host "Building discovery-service..." -ForegroundColor Yellow
Set-Location discovery-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building config-server..." -ForegroundColor Yellow
Set-Location config-server
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building api-gateway..." -ForegroundColor Yellow
Set-Location api-gateway
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building employee-service..." -ForegroundColor Yellow
Set-Location employee-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building department-service..." -ForegroundColor Yellow
Set-Location department-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building product-service..." -ForegroundColor Yellow
Set-Location product-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "=========================================" -ForegroundColor Green
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green