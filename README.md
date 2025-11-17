# 🌍 World Countries Information

A modern Android application that provides comprehensive information about countries around the world, built with the latest Android development best practices and Jetpack components.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-8.13.0-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-orange.svg)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📱 Features

- **Browse Countries**: View a comprehensive list of all countries worldwide
- **Country Details**: Access detailed information including:
  - Country name, capital, and region
  - Population and calling code
  - Official languages and currencies
  - Interactive map showing country location (powered by OpenStreetMap)
  - Country flag display
- **Offline-First**: Data is cached locally for offline access
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Dark Mode**: Automatic theme support based on system settings

## 🏗️ Architecture

This project follows **Clean Architecture** principles with a modern multi-module structure:

```
worldcountriesinformation/
├── app/                          # Application module (wiring & DI)
├── build-logic/                  # Gradle convention plugins
│   └── convention/
├── core/
│   ├── common/                   # Common utilities & extensions
│   ├── database/                 # Room database
│   ├── designsystem/             # Material 3 theme & components
│   ├── model/                    # Domain models
│   ├── navigation/               # Navigation definitions
│   └── network/                  # Retrofit & networking
├── data/
│   └── countries/                # Repository implementations
├── domain/                       # Use cases & repository interfaces
├── feature/
│   ├── countries/                # Countries list feature
│   └── countrydetails/           # Country details feature
└── tests-shared/                 # Shared test utilities
```

### Architecture Layers

#### Presentation Layer
- **UI**: Jetpack Compose with Material 3
- **ViewModels**: State management with Kotlin Flow
- **Navigation**: Type-safe navigation with Compose Navigation

#### Domain Layer
- **Use Cases**: Business logic encapsulation
- **Repository Interfaces**: Data abstraction
- **Models**: Domain entities

#### Data Layer
- **Repository**: Implements domain interfaces
- **Data Sources**: Remote (API) and Local (Room)
- **Offline-First**: Database as single source of truth

## 🛠️ Tech Stack

### Core
- **Kotlin** 2.2.20 - Programming language
- **Gradle** 8.13.0 - Build system
- **Convention Plugins** - Shared build configuration

### Android
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36
- **Compile SDK**: 36

### UI
- **Jetpack Compose** - Declarative UI framework
- **Material 3** - Design system
- **Compose BOM** 2025.01.00
- **Coil** 2.7.0 - Image loading

### Architecture Components
- **Hilt** 2.57.2 - Dependency injection
- **Navigation Compose** 2.9.5 - Navigation
- **Lifecycle** 2.9.4 - Lifecycle management
- **ViewModel** - State preservation

### Data & Networking
- **Retrofit** 3.0.0 - REST API client
- **OkHttp** 5.2.1 - HTTP client
- **Kotlinx Serialization** 1.7.3 - JSON parsing
- **Room** 2.8.2 - Local database
- **Coroutines** 1.10.2 - Asynchronous programming

### Maps
- **osmdroid** 6.1.20 - OpenStreetMap integration
- No API keys required
- Free and open source

### Testing
- **JUnit** 4.13.2 - Unit testing framework
- **MockK** 1.13.13 - Mocking library
- **Espresso** 3.7.0 - UI testing
- **Hilt Testing** - DI testing utilities

### Code Quality
- **Timber** 5.0.1 - Logging
- **StrictMode** - Runtime checks (debug builds)

## 📋 Prerequisites

- **Android Studio**: Ladybug | 2024.2.1 or later
- **JDK**: 17 or later
- **Gradle**: 8.13.0 (wrapper included)
- **Min Android Version**: 7.0 (API 24)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/WorldCountriesInformation.git
cd WorldCountriesInformation
```

### 2. Open in Android Studio

1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### 3. Build and Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### 4. Run from Android Studio

1. Click the "Run" button (▶️) or press `Shift + F10`
2. Select your target device or emulator
3. Wait for the app to install and launch

## 🔧 Configuration

### No API Keys Required! 🎉

This app uses **OpenStreetMap** for maps, which requires no API keys or registration. Just clone and run!

### Optional: Local Properties

Create `local.properties` in the project root (if it doesn't exist):

```properties
sdk.dir=/path/to/your/Android/sdk
```

## 📦 Module Dependency Graph

```
:app
├── :feature:countries
│   ├── :core:designsystem
│   ├── :core:navigation
│   ├── :core:model
│   └── :domain
├── :feature:countrydetails
│   ├── :core:designsystem
│   ├── :core:model
│   └── :domain
├── :data:countries
│   ├── :core:network
│   ├── :core:database
│   ├── :core:model
│   └── :domain
└── :domain
    └── :core:model
```

## 🧪 Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :domain:test
./gradlew :feature:countries:test
```

### Instrumented Tests

```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run tests on specific module
./gradlew :app:connectedAndroidTest
```

### Test Coverage

The project uses:
- **MockK** for mocking in unit tests
- **Hilt Testing** for dependency injection in tests
- **Espresso** for UI testing
- **JUnit 4** as the test framework

## 🏛️ Design Patterns

This project implements several software design patterns:

- **MVVM (Model-View-ViewModel)**: UI architecture pattern
- **Repository Pattern**: Data abstraction
- **Use Case Pattern**: Business logic encapsulation
- **Dependency Injection**: Via Hilt
- **Observer Pattern**: Via Kotlin Flow and StateFlow
- **Factory Pattern**: ViewModel creation
- **Singleton Pattern**: Repository and API clients

## 📐 Code Organization

### Package Structure

```
com.vamsi.worldcountriesinformation/
├── ui/                    # UI layer (app module)
│   ├── compose/
│   │   └── navigation/
│   └── MainActivity.kt
├── di/                    # Dependency injection modules
├── feature/
│   ├── countries/         # Countries list feature
│   │   ├── CountriesScreen.kt
│   │   └── CountriesViewModel.kt
│   └── countrydetails/    # Country details feature
│       ├── CountryDetailsScreen.kt
│       └── CountryDetailsViewModel.kt
├── data/
│   └── countries/         # Data layer
│       ├── repository/
│       ├── mappers/
│       └── di/
├── domain/                # Domain layer
│   ├── countries/
│   │   ├── GetCountriesUseCase.kt
│   │   └── CountriesRepository.kt
│   └── core/
│       ├── FlowUseCase.kt
│       └── ApiResponse.kt
└── core/
    ├── database/          # Room database
    ├── network/           # Retrofit & API
    ├── model/             # Data models
    └── designsystem/      # UI components & theme
```

## 🎨 UI Components

### Screens

1. **Countries List Screen**
   - Displays all countries in a scrollable list
   - Search functionality (if implemented)
   - Pull-to-refresh support
   - Offline indicator

2. **Country Details Screen**
   - Country flag display
   - Interactive map with country marker
   - Detailed country information
   - Back navigation

### Design System

- **Theme**: Material 3 with custom color scheme
- **Typography**: Material 3 type scale
- **Components**: Reusable composables in `:core:designsystem`

## 🌐 Data Source

This app fetches country data from the **REST Countries API v3.1** (latest version):
- Base URL: `https://restcountries.com/v3.1/`
- **Version**: 3.1 (upgraded from v2)
- Free to use
- No authentication required
- No rate limits

### API v3.1 Features
- **Improved Data Structure**: More detailed country information
- **Better Currency Support**: Currency data as key-value pairs
- **Enhanced Language Data**: Language codes mapped to names
- **Multiple Capitals**: Support for countries with multiple capital cities
- **IDD Information**: International Direct Dialing codes
- **Map Links**: Direct links to Google Maps and OpenStreetMap

### Migration from v2 to v3.1
This project uses the latest REST Countries API v3.1, which offers improved data structure compared to v2:
- **currencies**: Changed from List to Map for better access
- **languages**: Changed from List to Map with language codes
- **name**: Now includes common, official, and native names
- **capital**: Changed from String to List<String> for multiple capitals
- **flags**: Includes PNG, SVG, and alt text

## 🗺️ Maps Integration

### OpenStreetMap (osmdroid)

This app uses **osmdroid** for maps instead of Google Maps:

**Advantages:**
- ✅ Completely free and open source
- ✅ No API keys required
- ✅ No usage limits or quotas
- ✅ Privacy-friendly (no tracking)
- ✅ Offline map support

**Attribution:**
Maps are provided by OpenStreetMap contributors. The app automatically displays the required copyright notice: "© OpenStreetMap contributors"

**License:** OpenStreetMap data is available under the Open Database License (ODbL)

## 📱 Minimum Requirements

### Device Requirements
- Android 7.0 (API 24) or higher
- Internet connection (for initial data fetch)
- ~50 MB storage space

### Recommended
- Android 12 (API 31) or higher
- Stable internet connection
- GPS/Location services (optional, for map features)

## 🔨 Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```
- Includes debugging tools
- Timber logging enabled
- StrictMode enabled
- No obfuscation

### Release Build
```bash
./gradlew assembleRelease
```
- Optimized and obfuscated
- No logging
- Smaller APK size

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Write unit tests for new features
- Update documentation as needed

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

### Third-Party Licenses

- **OpenStreetMap**: [ODbL License](https://www.openstreetmap.org/copyright)
- **osmdroid**: [Apache 2.0](https://github.com/osmdroid/osmdroid/blob/master/LICENSE)
- **REST Countries API**: Free to use

## 🙏 Acknowledgments

- **REST Countries API** - For providing free country data
- **OpenStreetMap** - For free and open map tiles
- **osmdroid** - For the Android map library
- **Material Design** - For design guidelines
- **Jetpack Compose** - For modern UI toolkit

## 📞 Contact

**Developer**: Vamsi
**Project Link**: [https://github.com/iVamsi/WorldCountriesInformation](https://github.com/iVamsi/WorldCountriesInformation)

## 🗺️ Roadmap

### Current Version (v1.0.0)
- ✅ Browse countries list
- ✅ View country details
- ✅ Interactive maps
- ✅ Offline support
- ✅ Material 3 UI

### Future Enhancements
- 🔄 Search and filter countries
- 🔄 Favorite countries
- 🔄 Dark/Light theme toggle
- 🔄 Multi-language support
- 🔄 Country comparison feature
- 🔄 Export country data
- 🔄 Widget support

## 📊 Project Stats

- **Languages**: Kotlin 100%
- **Modules**: 12
- **Min SDK**: API 24
- **Target SDK**: API 36
- **Build System**: Gradle with Kotlin DSL
