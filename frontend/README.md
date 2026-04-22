# SmartBus Accra — React Native Mobile Application

**MSc Computing Project | University of the West of Scotland**
**Student:** Janet Owusu | B01820760

---

## Overview

A cross-platform React Native mobile application for the Smart Bus Tracking and Mobile Ticketing
system serving the Greater Accra Region of Ghana. The app connects to a Spring Boot backend via
REST APIs and STOMP WebSocket for real-time bus tracking.

**Two user roles:**
- **Passenger** — browse routes, view schedules, estimate fares, book and pay for tickets, track buses live on a map
- **Driver** — view assigned trips, mark departure and arrival events

> Bus location updates are managed entirely by the Spring Boot backend scheduler.
> The driver app does **not** send any location data.

---

## Tech Stack

| Concern | Library |
|---|---|
| Framework | React Native 0.73 via Expo SDK 50 |
| Navigation | React Navigation v6 (Stack + Bottom Tabs) |
| HTTP client | Axios 1.6 with JWT interceptor |
| Real-time | @stomp/stompjs 7 over SockJS |
| Maps | Leaflet.js via react-native-webview (OpenStreetMap) |
| Auth storage | @react-native-async-storage/async-storage |
| State | React Context API + Hooks |
| Icons | @expo/vector-icons (Ionicons) |

---

## Prerequisites

- Node.js 18+ and npm
- Expo CLI: `npm install -g expo-cli`
- For physical device: Expo Go app (iOS / Android)
- For emulator: Android Studio (Android) or Xcode (iOS/macOS only)

---

## Setup

```bash
# 1. Install dependencies
npm install

# 2. Configure the backend URL
#    Edit src/constants/index.js and set:
#
#    API_BASE_URL = 'http://<your-backend-host>:8080'
#    WS_BASE_URL  = 'http://<your-backend-host>:8080/ws'
#
#    Common values:
#      Android emulator  →  http://10.0.2.2:8080
#      iOS simulator     →  http://localhost:8080
#      Physical device   →  http://<machine-LAN-IP>:8080

# 3. Start the development server
npx expo start

# 4. Scan the QR code with Expo Go, or press:
#      a  → open Android emulator
#      i  → open iOS simulator
```

---

## Project Structure

```
SmartBusApp/
├── App.js                         Entry point — provider tree
├── app.json                       Expo config (name, bundle ID, permissions)
├── babel.config.js
├── package.json
└── src/
    ├── constants/
    │   └── index.js               Colours, typography, spacing, API URLs
    ├── api/
    │   ├── axiosClient.js         Axios instance + JWT + 401 interceptors
    │   ├── authApi.js             POST /api/auth/login|signup
    │   ├── passengerApi.js        Routes, schedules, fare, tickets
    │   ├── trackingApi.js         GET /api/tracking/buses|routes/{id}
    │   └── driverApi.js           Trips, depart, arrive, status
    ├── context/
    │   ├── AuthContext.js         JWT decode, AsyncStorage, login/logout
    │   └── TrackingContext.js     STOMP lifecycle, busLocations state
    ├── hooks/
    │   ├── useAuth.js
    │   └── useTracking.js
    ├── services/
    │   └── websocketService.js    STOMP client factory + subscribeToLocations()
    ├── navigation/
    │   ├── RootNavigator.js       Auth gate + role-based routing
    │   ├── PassengerTabNavigator.js  Home/Tracking/Tickets tabs + nested stacks
    │   └── DriverStackNavigator.js   Dashboard + ActiveTrip
    ├── screens/
    │   ├── auth/
    │   │   ├── LoginScreen.js
    │   │   └── SignupScreen.js
    │   ├── passenger/
    │   │   ├── HomeScreen.js          Route list + local/backend search
    │   │   ├── RouteDetailScreen.js   Route info, stops, schedule picker
    │   │   ├── FareEstimateScreen.js  Fare form + calculation
    │   │   ├── BusTrackingScreen.js   MapView + WebSocket live markers
    │   │   ├── TicketBookingScreen.js Sequential book → pay flow
    │   │   ├── MyTicketsScreen.js     Filtered ticket list
    │   │   └── TicketDetailScreen.js  Full ticket + QR placeholder
    │   └── driver/
    │       ├── DriverDashboardScreen.js  Trip list for assigned bus
    │       └── ActiveTripScreen.js       Depart/arrive actions + timeline
    └── components/
        ├── common/
        │   ├── AppButton.js       Primary/secondary/destructive/ghost variants
        │   ├── AppInput.js        Labelled input, password toggle, error display
        │   ├── LoadingSpinner.js  Full-screen + inline; SkeletonCard/List
        │   └── EmptyState.js      EmptyState, ErrorBanner, WsBanner
        ├── passenger/
        │   ├── RouteCard.js       Route list item
        │   └── TicketCard.js      Ticket list item with status badge
        └── driver/
            └── TripStatusBadge.js Coloured status pill
```

---

## Navigation Map

```
RootNavigator
│
├── AuthStack              (unauthenticated)
│   ├── LoginScreen
│   └── SignupScreen
│
└── AppNavigator           (authenticated, role-determined)
    │
    ├── PassengerTabNavigator     [role === 'PASSENGER']
    │   ├── Home tab
    │   │   ├── HomeScreen
    │   │   ├── RouteDetailScreen
    │   │   ├── FareEstimateScreen
    │   │   ├── TicketBookingScreen
    │   │   └── TicketDetailScreen
    │   ├── Tracking tab
    │   │   └── BusTrackingScreen
    │   └── Tickets tab
    │       ├── MyTicketsScreen
    │       └── TicketDetailScreen
    │
    └── DriverStackNavigator      [role === 'DRIVER']
        ├── DriverDashboardScreen
        └── ActiveTripScreen
```

---

## API Endpoints Used

### Authentication
| Method | Endpoint |
|---|---|
| POST | /api/auth/login |
| POST | /api/auth/signup |

### Passenger
| Method | Endpoint |
|---|---|
| GET | /api/passenger/routes |
| GET | /api/passenger/routes/{routeId} |
| POST | /api/passenger/search-route |
| GET | /api/passenger/schedules |
| GET | /api/passenger/schedules/{scheduleId} |
| GET | /api/passenger/fare-estimate |
| POST | /api/passenger/fare-estimate |
| POST | /api/passenger/tickets/book |
| POST | /api/passenger/tickets/{ticketId}/pay |
| GET | /api/passenger/tickets |
| GET | /api/passenger/tickets/{ticketId} |

### Tracking
| Method | Endpoint |
|---|---|
| GET | /api/tracking/buses |
| GET | /api/tracking/routes/{routeId} |

### Driver
| Method | Endpoint |
|---|---|
| GET | /api/trips/bus/{busId} |
| GET | /api/trips/{tripId} |
| PUT | /api/trips/{tripId}/status |
| POST | /api/trips/{tripId}/depart |
| POST | /api/trips/{tripId}/arrive |

### WebSocket
| Topic | Direction | Consumer |
|---|---|---|
| /topic/tracking/locations | Server → Client | Passenger only |

---

## JWT Token Claims Expected

The app decodes the JWT at login and reads the following claims:

```json
{
  "sub":    "user@email.com",
  "name":   "Ama Owusu",
  "role":   "PASSENGER",
  "userId": 42,
  "busId":  7,
  "exp":    1234567890
}
```

- `role` drives navigation routing (`PASSENGER` → tabs, `DRIVER` → stack)
- `busId` is used by the driver app to fetch assigned trips
- If `busId` is absent from the token, `userId` is used as fallback

---

## Real-Time Tracking Architecture

```
Spring Boot Backend
  └── Scheduler (every N seconds)
        └── Simulates GPS updates
              └── Broadcasts to /topic/tracking/locations

React Native App (Passenger - BusTrackingScreen)
  ├── Mount: GET /api/tracking/buses   ← seed map (prevent empty flash)
  ├── Mount: STOMP connect → SockJS → /ws
  ├── Subscribe: /topic/tracking/locations
  │     └── Message received → update TrackingContext.busLocations
  │               └── MapView markers re-render reactively
  └── Unmount: unsubscribe + stompClient.deactivate()
```

---

## Configuration Reference

**`src/constants/index.js`** — all values you may need to change:

```js
export const API_BASE_URL = 'http://10.0.2.2:8080';    // ← change this
export const WS_BASE_URL  = 'http://10.0.2.2:8080/ws'; // ← change this
```

The colour palette, typography scale, and spacing grid are all defined
in the same file and can be adjusted to match any branding requirements.

---

## Known Limitations (Academic Prototype)

- Payment is **simulated** — no real payment gateway is integrated
- Map uses **Leaflet.js and OpenStreetMap** via `react-native-webview`, which is 100% free and requires no API keys.
- QR code display is a **placeholder** (Phase 2) — the `ticketCode` value is shown
  as plain text; a library such as `react-native-qrcode-svg` can be substituted
- The app targets **phones only** (portrait orientation); tablet layouts are Phase 2

---

## Running on a Physical Device

1. Ensure your phone and development machine are on the **same Wi-Fi network**
2. Set `API_BASE_URL` and `WS_BASE_URL` to your machine's LAN IP (e.g. `http://192.168.1.10:8080`)
3. Ensure the Spring Boot server allows connections from the local network (CORS configured)
4. Scan the QR code from `npx expo start` with the Expo Go app
