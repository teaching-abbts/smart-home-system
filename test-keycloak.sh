#!/bin/bash

# Keycloak Smart Home System - Authentication Test Script

# Configuration Variables
KEYCLOAK_URL="http://localhost:8081"
REALM_NAME="smart-home"
CLIENT_ID="smart-home-app"
CLIENT_SECRET="smart-home-client-secret"

# User Credentials
REGULAR_USERNAME="smarthome-user"
REGULAR_PASSWORD="smarthome123"
ADMIN_USERNAME="admin-user"
ADMIN_PASSWORD="admin123"

# Test Credentials (for security test)
WRONG_USERNAME="wrong-user"
WRONG_PASSWORD="wrong-pass"

# Derived URLs
TOKEN_ENDPOINT="${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/token"
AUTH_ENDPOINT="${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/auth"
REALM_ENDPOINT="${KEYCLOAK_URL}/realms/${REALM_NAME}"

echo "🏠 Smart Home Keycloak Authentication Test"
echo "=========================================="
echo

# Test regular user authentication
echo "1. Testing Regular User Authentication..."
echo "Username: $REGULAR_USERNAME"
echo "Password: $REGULAR_PASSWORD"
echo

REGULAR_USER_RESPONSE=$(curl -s -X POST "$TOKEN_ENDPOINT" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${REGULAR_USERNAME}&password=${REGULAR_PASSWORD}")

if echo "$REGULAR_USER_RESPONSE" | grep -q "access_token"; then
    echo "✅ Regular user authentication: SUCCESS"
    # Extract and decode the JWT payload to show user info
    ACCESS_TOKEN=$(echo "$REGULAR_USER_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
    # Extract payload (second part of JWT)
    PAYLOAD=$(echo "$ACCESS_TOKEN" | cut -d'.' -f2)
    # Add padding if needed and decode
    echo "   User roles: $(echo "$PAYLOAD=" | base64 -d 2>/dev/null | grep -o '"roles":\[[^]]*\]' || echo 'Could not extract roles')"
else
    echo "❌ Regular user authentication: FAILED"
    echo "   Response: $REGULAR_USER_RESPONSE"
fi

echo

# Test admin user authentication
echo "2. Testing Admin User Authentication..."
echo "Username: $ADMIN_USERNAME"
echo "Password: $ADMIN_PASSWORD"
echo

ADMIN_USER_RESPONSE=$(curl -s -X POST "$TOKEN_ENDPOINT" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}")

if echo "$ADMIN_USER_RESPONSE" | grep -q "access_token"; then
    echo "✅ Admin user authentication: SUCCESS"
    # Extract and decode the JWT payload to show user info
    ACCESS_TOKEN=$(echo "$ADMIN_USER_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
    # Extract payload (second part of JWT)
    PAYLOAD=$(echo "$ACCESS_TOKEN" | cut -d'.' -f2)
    # Add padding if needed and decode
    echo "   User roles: $(echo "$PAYLOAD=" | base64 -d 2>/dev/null | grep -o '"roles":\[[^]]*\]' || echo 'Could not extract roles')"
else
    echo "❌ Admin user authentication: FAILED"
    echo "   Response: $ADMIN_USER_RESPONSE"
fi

echo

# Test realm accessibility
echo "3. Testing Realm Accessibility..."
REALM_RESPONSE=$(curl -s "$REALM_ENDPOINT")

if echo "$REALM_RESPONSE" | grep -q "$REALM_NAME"; then
    echo "✅ Realm accessibility: SUCCESS"
    echo "   Realm: $REALM_NAME"
    echo "   Token service: $(echo "$REALM_RESPONSE" | grep -o '"token-service":"[^"]*' | cut -d'"' -f4)"
else
    echo "❌ Realm accessibility: FAILED"
    echo "   Response: $REALM_RESPONSE"
fi

echo

# Test incorrect credentials
echo "4. Testing Security (Wrong Credentials)..."
WRONG_CREDS_RESPONSE=$(curl -s -X POST "$TOKEN_ENDPOINT" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${WRONG_USERNAME}&password=${WRONG_PASSWORD}")

if echo "$WRONG_CREDS_RESPONSE" | grep -q "error"; then
    echo "✅ Security test: SUCCESS (correctly rejected invalid credentials)"
else
    echo "❌ Security test: FAILED (should reject invalid credentials)"
fi

echo
echo "🎉 Keycloak Setup Complete!"
echo
echo "📋 Summary for Ktor Integration:"
echo "  • Keycloak URL: $KEYCLOAK_URL"
echo "  • Realm: $REALM_NAME"
echo "  • Client ID: $CLIENT_ID"
echo "  • Client Secret: $CLIENT_SECRET"
echo "  • Token Endpoint: $TOKEN_ENDPOINT"
echo "  • Auth Endpoint: $AUTH_ENDPOINT"
echo
echo "👥 Test Users:"
echo "  • Regular: $REGULAR_USERNAME / $REGULAR_PASSWORD (roles: user)"
echo "  • Admin: $ADMIN_USERNAME / $ADMIN_PASSWORD (roles: admin, user)"
