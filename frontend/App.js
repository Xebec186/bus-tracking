import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { StyleSheet } from "react-native";

import { AuthProvider } from "./src/context/AuthContext";
import { TrackingProvider } from "./src/context/TrackingContext";
import RootNavigator from "./src/navigation/RootNavigator";

/**
 * Provider order:
 *   GestureHandlerRootView   — required by react-navigation gesture support
 *     SafeAreaProvider       — safe area insets for all screens
 *       AuthProvider         — JWT auth state, login/logout
 *         TrackingProvider   — STOMP WebSocket + busLocations state
 *           RootNavigator    — navigation tree (auth gate + role routing)
 */
export default function App() {
  return (
    <GestureHandlerRootView style={styles.root}>
      <SafeAreaProvider>
        <AuthProvider>
          <TrackingProvider>
            <StatusBar style="light" />
            <RootNavigator />
          </TrackingProvider>
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
});
