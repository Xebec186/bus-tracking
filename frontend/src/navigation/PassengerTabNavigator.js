import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { COLORS, FONTS } from "../constants";

import HomeScreen from "../screens/passenger/HomeScreen";
import RouteDetailScreen from "../screens/passenger/RouteDetailScreen";
import FareEstimateScreen from "../screens/passenger/FareEstimateScreen";
import TicketBookingScreen from "../screens/passenger/TicketBookingScreen";
import BusTrackingScreen from "../screens/passenger/BusTrackingScreen";
import MyTicketsScreen from "../screens/passenger/MyTicketsScreen";
import TicketDetailScreen from "../screens/passenger/TicketDetailScreen";
import PassengerProfileScreen from "../screens/passenger/PassengerProfileScreen";

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

// ── Home stack ────────────────────────────────────────────────────────────────
function HomeStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="HomeMain" component={HomeScreen} />
      <Stack.Screen name="RouteDetail" component={RouteDetailScreen} />
      <Stack.Screen name="FareEstimate" component={FareEstimateScreen} />
      <Stack.Screen name="TicketBooking" component={TicketBookingScreen} />
      <Stack.Screen name="TicketDetail" component={TicketDetailScreen} />
    </Stack.Navigator>
  );
}

// ── Tracking stack ────────────────────────────────────────────────────────────
function TrackingStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="BusTrackingMain" component={BusTrackingScreen} />
    </Stack.Navigator>
  );
}

// ── Tickets stack ─────────────────────────────────────────────────────────────
function TicketsStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="MyTickets" component={MyTicketsScreen} />
      <Stack.Screen name="TicketDetail" component={TicketDetailScreen} />
    </Stack.Navigator>
  );
}

function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="PassengerProfile" component={PassengerProfileScreen} />
    </Stack.Navigator>
  );
}

// ── Tab navigator ─────────────────────────────────────────────────────────────
export default function PassengerTabNavigator() {
  const insets = useSafeAreaInsets();
  
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: COLORS.primary,
        tabBarInactiveTintColor: COLORS.textMuted,
        tabBarStyle: {
          backgroundColor: COLORS.white,
          borderTopColor: COLORS.divider,
          borderTopWidth: 1,
          // Fixed height + inset is more reliable for visibility
          height: 60 + insets.bottom, 
          paddingBottom: insets.bottom > 0 ? insets.bottom : 8,
          paddingTop: 8,
        },
        tabBarLabelStyle: {
          fontSize: FONTS.sizes.xs,
          fontWeight: "600",
        },
        tabBarIcon: ({ color, size, focused }) => {
          const icons = {
            Home: focused ? "home" : "home-outline",
            Tracking: focused ? "map" : "map-outline",
            Tickets: focused ? "ticket" : "ticket-outline",
            Profile: focused ? "person" : "person-outline",
          };
          return (
            <Ionicons
              name={icons[route.name] ?? "ellipse"}
              size={size}
              color={color}
            />
          );
        },
      })}
    >
      <Tab.Screen
        name="Home"
        component={HomeStack}
        options={{ title: "Routes" }}
      />
      <Tab.Screen
        name="Tracking"
        component={TrackingStack}
        options={{ title: "Live Map" }}
      />
      <Tab.Screen
        name="Tickets"
        component={TicketsStack}
        options={{ title: "My Tickets" }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileStack}
        options={{ title: "Profile" }}
      />
    </Tab.Navigator>
  );
}
