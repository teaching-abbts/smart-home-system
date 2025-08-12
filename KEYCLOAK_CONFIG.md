# Keycloak Configuration for Smart Home Ktor Application

## Realm Information
- **Realm Name**: `smart-home`
- **Keycloak URL**: `http://localhost:8081`
- **Realm URL**: `http://localhost:8081/realms/smart-home`

## Client Configuration
- **Client ID**: `smart-home-app`
- **Client Secret**: `smart-home-client-secret`
- **Redirect URIs**:
  - `http://localhost:8080/*`
  - `http://localhost:8080/auth/callback/*`

## User Accounts

### Regular User
- **Username**: `smarthome-user`
- **Password**: `smarthome123`
- **Email**: `user@smarthome.local`
- **Roles**: `user`

### Admin User
- **Username**: `admin-user`
- **Password**: `admin123`
- **Email**: `admin@smarthome.local`
- **Roles**: `admin`, `user`

## OpenID Connect Endpoints
- **Authorization Endpoint**: `http://localhost:8081/realms/smart-home/protocol/openid-connect/auth`
- **Token Endpoint**: `http://localhost:8081/realms/smart-home/protocol/openid-connect/token`
- **Userinfo Endpoint**: `http://localhost:8081/realms/smart-home/protocol/openid-connect/userinfo`
- **JWKS URI**: `http://localhost:8081/realms/smart-home/protocol/openid-connect/certs`
- **Issuer**: `http://localhost:8081/realms/smart-home`

## Well-known Configuration
You can access the full OpenID Connect configuration at:
`http://localhost:8081/realms/smart-home/.well-known/openid_configuration`

## For Ktor Integration
Add these properties to your `application.yaml`:

```yaml
ktor:
  security:
    oauth:
      keycloak:
        realm: smart-home
        auth-server-url: http://localhost:8081
        client-id: smart-home-app
        client-secret: smart-home-client-secret
        redirect-uri: http://localhost:8080/auth/callback
```

## Admin Console Access
- **URL**: `http://localhost:8081`
- **Username**: `admin`
- **Password**: `admin_password`
- **Realm**: Select "smart-home" from the dropdown
