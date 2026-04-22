import { View, ActivityIndicator, StyleSheet } from "react-native";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import { useAuth } from "../context/AuthContext";
import PassengerTabNavigator from "./PassengerTabNavigator";
import DriverStackNavigator from "./DriverStackNavigator";
import LoginScreen from "../screens/auth/LoginScreen";
import SignupScreen from "../screens/auth/SignupScreen";
import { COLORS } from "../constants";

const Stack = createNativeStackNavigator();

// ── Auth stack (unauthenticated) ──────────────────────────────────────────────
function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false, animation: "fade" }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Signup" component={SignupScreen} />
    </Stack.Navigator>
  );
}

// ── Splash guard while token is being restored from AsyncStorage ──────────────
function SplashScreen() {
  return (
    <View style={styles.splash}>
      <ActivityIndicator size="large" color={COLORS.primary} />
    </View>
  );
}

// ── Root navigator ────────────────────────────────────────────────────────────
export default function RootNavigator() {
  const { isAuthenticated, isPassenger, isDriver, loading } = useAuth();

  if (loading) return <SplashScreen />;

  return (
    <NavigationContainer>
      {!isAuthenticated ? (
        // Not logged in → show auth stack
        <AuthStack />
      ) : isDriver ? (
        // Logged in as DRIVER → driver stack
        <DriverStackNavigator />
      ) : (
        // Logged in as PASSENGER (or any other role) → passenger tabs
        <PassengerTabNavigator />
      )}
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    backgroundColor: COLORS.background,
    alignItems: "center",
    justifyContent: "center",
  },
});
