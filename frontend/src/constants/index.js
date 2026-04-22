// ─── Colours ─────────────────────────────────────────────────────────────────
export const COLORS = {
  // Brand
  primary: "#1A5F7A", // deep teal — main brand
  primaryLight: "#2980B9", // lighter teal for buttons / accents
  primaryDark: "#0F3D52", // dark teal — headers
  accent: "#27AE60", // green — Greater Accra transport ref
  accentLight: "#2ECC71",

  // Status
  success: "#27AE60",
  warning: "#F39C12",
  error: "#E74C3C",
  info: "#2980B9",

  // Neutrals
  white: "#FFFFFF",
  background: "#F4F7FA",
  surface: "#FFFFFF",
  border: "#DDE3ED",
  divider: "#EAECF0",

  // Text
  textPrimary: "#1A2332",
  textSecondary: "#5A6B7C",
  textMuted: "#9BA8B5",
  textOnDark: "#FFFFFF",

  // Status badges
  statusBooked: "#EBF4FF",
  statusBookedText: "#1A5F7A",
  statusPaid: "#E8F8EF",
  statusPaidText: "#1A7A44",
  statusUsed: "#F0F0F0",
  statusUsedText: "#7A7A7A",
  statusCancelled: "#FDEDED",
  statusCancelledText: "#C0392B",

  // Map
  routeLine: "#1A5F7A",
  busMarker: "#27AE60",
  stopMarker: "#E74C3C",
};

// ─── Typography ───────────────────────────────────────────────────────────────
export const FONTS = {
  regular: "System",
  bold: "System",
  sizes: {
    xs: 11,
    sm: 13,
    md: 15,
    lg: 17,
    xl: 20,
    xxl: 24,
    h1: 28,
    h2: 22,
    h3: 18,
  },
};

// ─── Spacing (4pt grid) ───────────────────────────────────────────────────────
export const SPACING = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

// ─── Border radius ────────────────────────────────────────────────────────────
export const RADIUS = {
  sm: 6,
  md: 12,
  lg: 16,
  xl: 24,
  full: 999,
};

// ─── Shadows ──────────────────────────────────────────────────────────────────
export const SHADOW = {
  sm: {
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 3,
    elevation: 2,
  },
  md: {
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.12,
    shadowRadius: 6,
    elevation: 4,
  },
  lg: {
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.16,
    shadowRadius: 12,
    elevation: 8,
  },
};

// ─── Backend ──────────────────────────────────────────────────────────────────
// Replace with your actual Spring Boot host when running on device
export const API_BASE_URL = "http://192.168.100.3:8080"; // Android emulator → localhost
export const WS_BASE_URL = "http://192.168.100.3:8080/ws"; // SockJS endpoint

// ─── AsyncStorage keys ────────────────────────────────────────────────────────
export const STORAGE_KEYS = {
  TOKEN: "@smartbus_token",
  USER: "@smartbus_user",
  REFRESH_TOKEN: "@smartbus_refresh_token",
};
