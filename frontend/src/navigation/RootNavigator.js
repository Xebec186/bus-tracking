import { View, ActivityIndicator, StyleSheet, Text, TouchableOpacity } from "react-native";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import { useAuth } from "../context/AuthContext";
import PassengerTabNavigator from "./PassengerTabNavigator";
import DriverStackNavigator from "./DriverStackNavigator";
import LoginScreen from "../screens/auth/LoginScreen";
import SignupScreen from "../screens/auth/SignupScreen";
import { COLORS } from "../constants";

const Stack = createNativeStackNavigator();

function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false, animation: "fade" }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Signup" component={SignupScreen} />
    </Stack.Navigator>
  );
}

function SplashScreen() {
  return (
    <View style={styles.splash}>
      <ActivityIndicator size="large" color={COLORS.primary} />
    </View>
  );
}

export default function RootNavigator() {
  const { isAuthenticated, isPassenger, isDriver, user, logout, loading } = useAuth();

  if (loading) return <SplashScreen />;

  return (
    <NavigationContainer>
      {!isAuthenticated ? (
        <AuthStack />
      ) : isDriver ? (
        <DriverStackNavigator />
      ) : isPassenger ? (
        <PassengerTabNavigator />
      ) : (
        <View style={styles.splash}>
          <Text style={styles.unsupportedTitle}>Unsupported Role</Text>
          <Text style={styles.unsupportedText}>
            Role `{user?.role ?? "UNKNOWN"}` is not available in this mobile app.
          </Text>
          <TouchableOpacity onPress={logout} style={styles.unsupportedButton}>
            <Text style={styles.unsupportedButtonText}>Sign out</Text>
          </TouchableOpacity>
        </View>
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
    paddingHorizontal: 20,
  },
  unsupportedTitle: { fontSize: 20, fontWeight: "700", color: COLORS.textPrimary },
  unsupportedText: { marginTop: 8, color: COLORS.textSecondary, textAlign: "center" },
  unsupportedButton: {
    marginTop: 16,
    backgroundColor: COLORS.primary,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 8,
  },
  unsupportedButtonText: { color: COLORS.white, fontWeight: "700" },
});

