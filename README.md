# Smart Home System

A tutoring project in NDS Software Engineering at the ABB Technikerschule in Baden, Switzerland.

## Requirements

- [JDK 21 LTS](https://adoptium.net/en-GB/temurin/releases/?os=any&arch=any&version=21)
- [Node.js 22 LTS *(with npm)*](https://nodejs.org/en/download)

## Running the Project

- Open a terminal in the project root directory.
- Run `gradlew run` to start the **Ktor Backend**.
- Open another terminal in the `src/main/vue-project` directory.
- Run `npm install` to install the Vue.js dependencies.
- Run `npm run dev` to start the Vue.js frontend.

## Various

- Refresh Dependencies: `./gradlew clean --refresh-dependencies`
- Build the Project: `./gradlew build`
- Build the Project continuously: `./gradlew build --continuous`
- Run the Project: `./gradlew run`
- Create a keystore: `keytool -keystore keystore.jks -alias sampleAlias -genkeypair -keyalg RSA -keysize 4096 -validity 3 -dname 'CN=localhost, OU=ktor, O=ktor, L=Unspecified, ST=Unspecified, C=US' -ext 'SAN:c=DNS:localhost,IP:127.0.0.1'`
