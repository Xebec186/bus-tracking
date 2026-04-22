import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { jwtDecode } from "jwt-decode";
import { STORAGE_KEYS } from "../constants";
import { setLogoutCallback } from "../api/axiosClient";

const AuthContext = createContext(null);

/**
 * Decodes the JWT and extracts user info.
 *
 * The Spring Boot backend sets these claims:
 *   sub         → email
 *   firstName   → user's first name
 *   lastName    → user's last name
 *   role        → "PASSENGER" | "DRIVER"
 *   userId      → numeric user ID
 *   exp         → expiry unix timestamp
 *
 * displayName is composed here so every screen can just read user.displayName.
 */
function decodeToken(token) {
  try {
    const decoded = jwtDecode(token);

    const firstName = decoded.firstName ?? "";
    const lastName = decoded.lastName ?? "";
    // Compose a display name; fall back gracefully if claims are absent
    const displayName =
      [firstName, lastName].filter(Boolean).join(" ").trim() || decoded.sub;

    return {
      token,
      userId: decoded.userId ?? decoded.sub,
      email: decoded.sub,
      firstName,
      lastName,
      displayName, // "Ama Owusu" — use this for UI
      role: decoded.role ?? decoded.roles?.[0] ?? "PASSENGER",
      exp: decoded.exp,
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session on app launch
  useEffect(() => {
    (async () => {
      try {
        const token = await AsyncStorage.getItem(STORAGE_KEYS.TOKEN);
        if (token) {
          const decoded = decodeToken(token);
          if (decoded && decoded.exp * 1000 > Date.now()) {
            setUser(decoded);
          } else {
            await AsyncStorage.multiRemove([
              STORAGE_KEYS.TOKEN,
              STORAGE_KEYS.USER,
              STORAGE_KEYS.REFRESH_TOKEN,
            ]);
          }
        }
      } catch (err) {
        console.warn("[Auth] Failed to restore session:", err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const login = useCallback(async (tokenResponse) => {
    const token = tokenResponse.token;
    const decoded = decodeToken(token);
    if (!decoded) throw new Error("Invalid token received");
    await AsyncStorage.multiSet([
      [STORAGE_KEYS.TOKEN, token],
      [STORAGE_KEYS.REFRESH_TOKEN, tokenResponse.refreshToken],
    ]);
    setUser(decoded);
    return decoded;
  }, []);

  const logout = useCallback(async () => {
    await AsyncStorage.multiRemove([
      STORAGE_KEYS.TOKEN,
      STORAGE_KEYS.REFRESH_TOKEN,
      STORAGE_KEYS.USER,
    ]);
    setUser(null);
  }, []);

  useEffect(() => {
    setLogoutCallback(logout);
  }, [logout]);

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    isPassenger: user?.role === "PASSENGER",
    isDriver: user?.role === "DRIVER",
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
