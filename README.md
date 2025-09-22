# OpenWeather Demo Android App

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-OpenWeather-orange.svg)](https://openweathermap.org/api)
## 📖 Table of Contents

* [✨ Features](#-features)
* [🛠️ Technologies & Libraries Used](#️-technologies--libraries-used)
* [🏗️ Architecture](#️-architecture)
* [⚙️ Setup & Installation](#️-setup--installation)
* [🔑 API Key Setup](#-api-key-setup)
* [🤔 Project Motivation & Learning](#-project-motivation--learning)
* [🚀 Future Enhancements (To-Do)](#-future-enhancements-to-do)
* [🤝 Contributing](#-contributing-optional)

## ✨ Features

* Displays current weather conditions for a list of popular US locations.
* Shows Temperature(Celcius/Fahrenheit), General Weather Description, Administrative Area, Time Observed At.
* User-friendly interface to view weather information.
* Error handling for incorrect searchs and when API tokens have run out for the API Key.
* Back-end loading of data on initialization and UI Progress indicator when querying data.

## 🛠️ Technologies & Libraries Used

* **Programming Language:** Kotlin
* **Core Android Jetpack:**
    * ViewModel - For managing UI-related data in a lifecycle-conscious way.
    * LiveData/Kotlin Flow - For observable data patterns.
* **Networking:**
    * Retrofit - For type-safe HTTP calls to the OpenWeather API.
    * OkHttp - HTTP client.
    * Gson/Moshi - For parsing JSON responses.
* **Asynchronous Programming:**
    * Kotlin Coroutines - For managing background threads and simplifying async code.
* **Dependency Injection:**
    * Hilt - "Manual Dependency Injection".
* **UI:**
    * Jetpack Compose
* **Testing:**
    * JUnit - For unit tests.
    * HiltAndroidTest - For UI tests.

## 🏗️ Architecture

This project aims to follow the MVVM (Model-View-ViewModel) architecture pattern.

* **Model:** Represents the data layer (e.g., data classes for weather info, Repository that fetches data from the OpenWeather API).
* **View:** Represents the UI layer (Activities/Fragments) that observes data from the ViewModel and displays it.
* **ViewModel:** Acts as a bridge between the Model and the View, holding UI-related data and business logic, and surviving configuration changes.

## ⚙️ Setup & Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/andrev91/AccuWeatherDemo.git](https://github.com/andrev91/AccuWeatherDemo.git)
    cd AccuWeatherDemo
    ```
2.  **Open in Android Studio:**
    * Open Android Studio.
    * Click on "Open an Existing Project".
    * Navigate to the cloned `AccuWeatherDemo` directory and select it.
3.  **API Key:** Follow the instructions in the [API Key Setup](#-api-key-setup) section below.
4.  **Build and Run:**
    * Let Android Studio sync the Gradle files.
    * Build the project (Build > Make Project).
    * Run the app on an emulator or a physical Android device.

## 🔑 API Key Setup

This project requires an API key from OpenWeather to fetch weather data.

1.  **Get an API Key:**
    * Go to the [OpenWeather Developer Portal](https://openweathermap.org/api) and register for a free API key.
2.  **Store the API Key:**
    * Create a file named `local.properties` in the root directory of the project (the same level as `gradle.properties` and `settings.gradle`).
    * **Important:** Add `local.properties` to your `.gitignore` file if it's not already there to prevent your key from being committed to Git.
        ```
        # .gitignore
        ...
        local.properties
        ```
    * Add your API key to `local.properties` like this:
        ```properties
        OPENWEATHER_API_KEY="YOUR_ACTUAL_OPENWEATHER_API_KEY"
        ```
    The project is configured to read this key from `local.properties` via the `build.gradle` file.

## 🤔 Project Motivation & Learning

* **Why I built this:** This project was developed as a personal initiative to practice and demonstrate my Android development skills, particularly in areas like API integration, modern Android architecture (MVVM), and asynchronous programming with Kotlin Coroutines.
* **What I learned:** Through this project, I gained hands-on experience with Retrofit for network requests, designing a reactive UI with LiveData/Flow, and structuring an app using the MVVM pattern. I also focused on creating a clean and maintainable codebase.

## 🚀 Future Enhancements (To-Do)

While this demo showcases core functionality, here are some potential future improvements:

* [ ] Implement location search functionality for users to get weather for any city.
* [ ] Use device GPS to fetch weather for the current location (with proper permission handling).
* [ ] Add more detailed weather information (e.g., hourly forecasts, UV index).
* [ ] Improve UI/UX with more polished visuals and animations.
* [ ] Implement a local database (e.g., Room) to cache weather data for offline access or faster loading.
* [ ] Write comprehensive unit and UI tests.
* [ ] Add support for different themes (e.g., dark mode).

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! If you have suggestions for improvements or find any bugs, please feel free to open an issue or submit a pull request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

<img width="400" height="960" alt="image" src="https://github.com/user-attachments/assets/d9ed9ad6-6c33-4d9d-9b24-3ce8b2f4fda6" />
<img width="400" height="960" alt="image" src="https://github.com/user-attachments/assets/6e6c9df3-5ae6-4979-a995-f42d6257c5f4" />
<img width="400" height="960" alt="image" src="https://github.com/user-attachments/assets/8af0270f-5fbc-4b59-a38e-34280a00f65e" />
<img width="400" height="960" alt="image" src="https://github.com/user-attachments/assets/dbc1bd23-0d12-4573-aa2d-bca44723a06e" />
