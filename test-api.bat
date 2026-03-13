@echo off
curl -s -X POST "http://127.0.0.1:4089/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "client_id=keycloak-jwt" --data-urlencode "grant_type=client_credentials" --data-urlencode "client_secret=WeOVrZbqnkSBCdnbPLt5acJ3TfYxjREG" > token.json

for /f "delims=" %%a in ('powershell -Command "(Get-Content token.json | ConvertFrom-Json).access_token"') do set TOKEN=%%a

curl -s "http://localhost:8090/api/users/all?page=0&size=12" -H "Authorization: Bearer %TOKEN%"
