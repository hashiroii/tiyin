# Tiyin 

A modern Android subscription tracker built with **Jetpack Compose**, **Firebase**, and **Clean Architecture**. Track all your recurring subscriptions, monitor costs, and never miss a payment.

---

## 📸 Screenshots

| Dark — Home | Dark — Details | Dark — Settings | Light — Home |
|:-----------:|:--------------:|:---------------:|:------------:|
| ![Dark Screen Home Page](https://github.com/user-attachments/assets/495234e3-0e64-4245-a573-1943c1b7c1bd) | ![Dark Screen Details](https://github.com/user-attachments/assets/fd447ec5-fe66-45c6-bdaf-2eb650406eae) | ![Dark Screen Settings](https://github.com/user-attachments/assets/62f55610-f158-4199-947d-c8bde264a474) | ![Light Screen Home Page](https://github.com/user-attachments/assets/7b11ca80-bb04-4f6b-9425-ce9730e05b56) |

---

##  Why Tiyin exists

Managing subscriptions is annoying in real life. People use multiple services like Netflix, Spotify, and others, and over time it becomes hard to track how much money is actually being spent every month. 

Sometimes subscriptions renew without notice, and users only realize it after money is already gone. From my experience, most people don’t track this properly and just accept the loss.

Tiyin was created to solve this problem in a simple way. The goal is to give users a clear view of all their subscriptions, show the real total cost, and help avoid unexpected payments.

---

##  Features

-  **Subscription management** — Add, edit, and delete subscriptions with service auto-recognition
-  **Multi-currency support** — Total cost calculated and converted to your preferred currency
-  **Sort & filter** — Sort by expiry date, cost, or name
-  **Progress tracking** — Visual progress bar showing time elapsed in the current billing cycle
-  **Auto-renewal detection** — Subscriptions auto-roll when a period expires
-  **Payment reminders** — Get notified before upcoming renewals
-  **Cloud sync** — Subscriptions backed up and synced via Firestore
-  **Dark / Light theme** — Full Material 3 theming support

---

##  Architecture

Tiyin follows **Clean Architecture** with a multi-module Gradle setup, separating concerns across three layers:

```
app/
├── data/           # Repositories, Firestore, Room, mappers, DI modules
├── domain/         # Models, repository interfaces, use cases
└── feature/
    ├── home/       # HomeScreen, HomeViewModel, HomeUiState, HomeIntent
    ├── analytics/
    ├── settings/
    └── ...
ui/                 # Shared Compose components (SubscriptionCard, ServiceLogo, etc.)
navigation/         # Type-safe navigation destinations (kotlinx.serialization)
```

### Layers

| Layer | Responsibility |
|-------|---------------|
| **Domain** | `Subscription`, `ServiceInfo`, use cases (`GetSubscriptionsUseCase`, `CalculateTotalCostUseCase`, …), repository interfaces |
| **Data** | `FirestoreSubscriptionRepositoryImpl`, `SubscriptionEntity`, mappers, Hilt `DataModule` / `RepositoryModule` |
| **Feature** | MVI-style: `HomeIntent` → `HomeViewModel` → `HomeUiState` consumed by `HomeScreen` |

### State Management (MVI)

Each feature uses a unidirectional data flow:

```
User action → HomeIntent → HomeViewModel → HomeUiState → HomeScreen (Compose)
```

`HomeUiState` is a sealed interface with three states: `Loading`, `Success`, and `Error`.

---

##  Tech Stack

| Category | Library |
|----------|---------|
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, StateFlow, Clean Architecture |
| DI | Hilt |
| Backend | Firebase Firestore, Firebase Auth |
| Local DB | Room |
| Navigation | Navigation Compose (type-safe, `kotlinx.serialization`) |
| Async | Kotlin Coroutines & Flow |
| Images | Coil |

---

##  Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17+
- A Firebase project with **Firestore** and **Authentication** enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/hashiroii/tiyin.git
   cd tiyin
   ```

2. **Add Firebase config**  
   Download `google-services.json` from your Firebase console and place it in the `app/` directory.

3. **Build & run**  
   Open in Android Studio and run on a device or emulator (API 26+).

## 📄 License

```
MIT License — see LICENSE for details.
```
