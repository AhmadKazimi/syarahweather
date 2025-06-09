# 🌤️ Syarah Weather App

A modern Android weather application built with **Clean Architecture** principles, providing current weather information and location management with a beautiful **Jetpack Compose** UI.

## 📱 Features

- **Current Location Weather**: Automatic weather updates for your current location
- **Location Management**: Save and manage multiple locations  
- **Weather Forecast**: Detailed weather information with forecasts
- **Places Search**: Search and add new locations using Google Places API
- **Modern UI**: Beautiful, responsive design built with Jetpack Compose
- **Offline Support**: Smart caching for offline weather data access

## 🏗️ Architecture

This project follows **Clean Architecture** principles with **SOLID** design patterns:

- **Domain Layer**: Pure Kotlin business logic with use cases and repository interfaces
- **Data Layer**: Repository implementations, API services, and local data sources  
- **UI Layer**: MVVM pattern with Jetpack Compose and reactive state management

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI
- **Clean Architecture** - Maintainable and testable code structure
- **MVVM Pattern** - Reactive state management
- **Hilt** - Dependency injection

### Key Libraries
- **Retrofit & OkHttp** - Networking and API communication
- **Kotlin Coroutines & Flow** - Asynchronous programming
- **DataStore** - Modern data storage
- **Google Play Services** - Location and Places API
- **Material Design 3** - Modern UI components
- **JUnit & Mockito** - Testing framework

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11+
- Android SDK API 24+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/syarah-weather.git
   ```

2. **Add API Keys**
   Create `local.properties` in root directory:
   ```properties
   API_KEY_OPEN_WERATHER=your_openweather_api_key
   GOOGLE_PLACES_API_KEY=your_google_places_api_key
   ```

3. **Build and Run**
   ```bash
   ./gradlew build
   ```

## 🏛️ Project Structure

```
com.kazimi.syarahweather/
├── domain/          # Business logic, use cases, repository interfaces
├── data/           # Repository implementations, API services, local 
├── ui/             # Compose screens, ViewModels, navigation
└── di/             # Dependency injection modules
```

## 📱 Permissions

- **Location Access** - For current weather data
- **Internet** - For API calls

## 👨‍💻 Author

**Kazimi** - Android Developer

---

Built with ❤️ using Clean Architecture and Jetpack Compose 
