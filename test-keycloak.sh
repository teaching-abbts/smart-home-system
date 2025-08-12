#!/bin/bash

# Keycloak Smart Home System - Authentication Test Script

echo "🏠 Smart Home Keycloak Authentication Test"
echo "=========================================="
echo

# Test regular user authentication
echo "1. Testing Regular User Authentication..."
echo "Username: smarthome-user"
echo "Password: smarthome123"
echo

REGULAR_USER_RESPONSE=$(curl -s -X POST "http://localhost:8081/realms/smart-home/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=smart-home-app&client_secret=smart-home-client-secret&username=smarthome-user&password=smarthome123")

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
echo "Username: admin-user"
echo "Password: admin123"
echo

ADMIN_USER_RESPONSE=$(curl -s -X POST "http://localhost:8081/realms/smart-home/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=smart-home-app&client_secret=smart-home-client-secret&username=admin-user&password=admin123")

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
REALM_RESPONSE=$(curl -s "http://localhost:8081/realms/smart-home")

if echo "$REALM_RESPONSE" | grep -q "smart-home"; then
    echo "✅ Realm accessibility: SUCCESS"
    echo "   Realm: smart-home"
    echo "   Token service: $(echo "$REALM_RESPONSE" | grep -o '"token-service":"[^"]*' | cut -d'"' -f4)"
else
    echo "❌ Realm accessibility: FAILED"
    echo "   Response: $REALM_RESPONSE"
fi

echo

# Test incorrect credentials
echo "4. Testing Security (Wrong Credentials)..."
WRONG_CREDS_RESPONSE=$(curl -s -X POST "http://localhost:8081/realms/smart-home/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=smart-home-app&client_secret=smart-home-client-secret&username=wrong-user&password=wrong-pass")

if echo "$WRONG_CREDS_RESPONSE" | grep -q "error"; then
    echo "✅ Security test: SUCCESS (correctly rejected invalid credentials)"
else
    echo "❌ Security test: FAILED (should reject invalid credentials)"
fi

echo
echo "🎉 Keycloak Setup Complete!"
echo
echo "📋 Summary for Ktor Integration:"
echo "  • Keycloak URL: http://localhost:8081"
echo "  • Realm: smart-home"
echo "  • Client ID: smart-home-app"
echo "  • Client Secret: smart-home-client-secret"
echo "  • Token Endpoint: http://localhost:8081/realms/smart-home/protocol/openid-connect/token"
echo "  • Auth Endpoint: http://localhost:8081/realms/smart-home/protocol/openid-connect/auth"
echo
echo "👥 Test Users:"
echo "  • Regular: smarthome-user / smarthome123 (roles: user)"
echo "  • Admin: admin-user / admin123 (roles: admin, user)"
