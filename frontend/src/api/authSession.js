import AsyncStorage from "@react-native-async-storage/async-storage";
import { STORAGE_KEYS } from "../constants";

let onLogout = null;
let refreshHandler = null;

export function registerLogoutHandler(handler) {
  onLogout = handler;
}

export function registerRefreshHandler(handler) {
  refreshHandler = handler;
}

export async function getAccessToken() {
  return AsyncStorage.getItem(STORAGE_KEYS.TOKEN);
}

export async function getRefreshToken() {
  return AsyncStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
}

export async function saveTokens({ token, refreshToken }) {
  const kv = [];
  if (token) kv.push([STORAGE_KEYS.TOKEN, token]);
  if (refreshToken) kv.push([STORAGE_KEYS.REFRESH_TOKEN, refreshToken]);
  if (kv.length > 0) {
    await AsyncStorage.multiSet(kv);
  }
}

export async function clearSessionStorage() {
  await AsyncStorage.multiRemove([
    STORAGE_KEYS.TOKEN,
    STORAGE_KEYS.REFRESH_TOKEN,
    STORAGE_KEYS.USER,
  ]);
}

export async function refreshAccessToken() {
  if (!refreshHandler) {
    throw new Error("Refresh handler not registered");
  }
  return refreshHandler();
}

export async function forceLogout() {
  await clearSessionStorage();
  if (onLogout) {
    await onLogout();
  }
}

