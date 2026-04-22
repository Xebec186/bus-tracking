import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, FONTS } from '../constants';

import DriverDashboardScreen from '../screens/driver/DriverDashboardScreen';
import ActiveTripScreen      from '../screens/driver/ActiveTripScreen';
import DriverProfileScreen   from '../screens/driver/DriverProfileScreen';

const Stack = createNativeStackNavigator();
const Tab   = createBottomTabNavigator();

// ── Trips stack ───────────────────────────────────────────────────────────────
function TripsStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="DriverDashboard" component={DriverDashboardScreen} />
      <Stack.Screen name="ActiveTrip"      component={ActiveTripScreen} />
    </Stack.Navigator>
  );
}

// ── Profile stack ─────────────────────────────────────────────────────────────
function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="DriverProfile" component={DriverProfileScreen} />
    </Stack.Navigator>
  );
}

// ── Driver tab navigator ──────────────────────────────────────────────────────
export default function DriverStackNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor:   COLORS.accent,
        tabBarInactiveTintColor: COLORS.textMuted,
        tabBarStyle: {
          backgroundColor: COLORS.primaryDark,
          borderTopColor:  'rgba(255,255,255,0.1)',
          borderTopWidth:  1,
          height:          60,
          paddingBottom:   8,
        },
        tabBarLabelStyle: {
          fontSize:   FONTS.sizes.xs,
          fontWeight: '600',
          color:      COLORS.white,
        },
        tabBarIcon: ({ color, size, focused }) => {
          const icons = {
            Trips:   focused ? 'bus'           : 'bus-outline',
            Profile: focused ? 'person-circle' : 'person-circle-outline',
          };
          return <Ionicons name={icons[route.name] ?? 'ellipse'} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen name="Trips"   component={TripsStack}   options={{ title: 'My Trips' }} />
      <Tab.Screen name="Profile" component={ProfileStack} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}
