import { useState } from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useAuth } from "../../context/AuthContext";
import { authApi } from "../../api/authApi";
import AppInput from "../../components/common/AppInput";
import AppButton from "../../components/common/AppButton";
import { COLORS, FONTS, SPACING, RADIUS } from "../../constants";

export default function LoginScreen({ navigation }) {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState(null);

  // ── Client-side validation ─────────────────────────────────────────────────
  // Password minimum matches backend @Size(min = 8)
  function validate() {
    const e = {};
    if (!email.trim()) e.email = "Email is required";
    else if (!/\S+@\S+\.\S+/.test(email)) e.email = "Enter a valid email";
    if (!password) e.password = "Password is required";
    else if (password.length < 8)
      e.password = "Password must be at least 8 characters";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  // Clear a single field error (client or server) as the user edits
  function clearFieldError(field) {
    setErrors((prev) => {
      if (!prev[field]) return prev;
      const next = { ...prev };
      delete next[field];
      return next;
    });
    setServerError(null);
  }

  // Route backend error response to the right UI slot
  function handleApiError(err) {
    const data = err.response?.data;
    if (data?.errors && Object.keys(data.errors).length > 0) {
      // Field-level errors from MethodArgumentNotValidException handler
      setErrors((prev) => ({ ...prev, ...data.errors }));
    } else {
      // Non-field error: wrong credentials, account locked, server fault, etc.
      setServerError(
        data?.message ?? "Login failed. Please check your credentials.",
      );
    }
  }

  async function handleLogin() {
    setServerError(null);
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await authApi.login(email.trim().toLowerCase(), password);
      // const token = res.data?.token ?? res.data?.accessToken ?? res.data;
      await login(res.data);
      // Navigation handled automatically by RootNavigator auth state
    } catch (err) {
      handleApiError(err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.safe}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Header */}
          <View style={styles.header}>
            <View style={styles.logoWrap}>
              <Ionicons name="bus" size={36} color={COLORS.white} />
            </View>
            <Text style={styles.appName}>SmartBus</Text>
            <Text style={styles.tagline}>Greater Accra Public Transport</Text>
          </View>

          {/* Card */}
          <View style={styles.card}>
            <Text style={styles.title}>Welcome back</Text>
            <Text style={styles.subtitle}>Sign in to your account</Text>

            {/* General server error (wrong credentials, 500, etc.) */}
            {serverError ? (
              <View style={styles.serverErrorBox}>
                <Ionicons
                  name="alert-circle-outline"
                  size={16}
                  color={COLORS.error}
                />
                <Text style={styles.serverErrorText}>{serverError}</Text>
              </View>
            ) : null}

            <AppInput
              label="Email address"
              leftIcon="mail-outline"
              placeholder="you@example.com"
              keyboardType="email-address"
              value={email}
              onChangeText={(v) => {
                setEmail(v);
                clearFieldError("email");
              }}
              error={errors.email}
            />

            <AppInput
              label="Password"
              leftIcon="lock-closed-outline"
              placeholder="••••••••"
              secureTextEntry
              value={password}
              onChangeText={(v) => {
                setPassword(v);
                clearFieldError("password");
              }}
              error={errors.password}
            />

            <AppButton
              label="Sign In"
              onPress={handleLogin}
              loading={loading}
              style={styles.btn}
            />

            <TouchableOpacity
              style={styles.linkRow}
              onPress={() => navigation.navigate("Signup")}
              accessibilityRole="link"
            >
              <Text style={styles.linkText}>
                Don't have an account?{" "}
                <Text style={styles.link}>Create one</Text>
              </Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.primary },
  scroll: { flexGrow: 1, justifyContent: "center", paddingBottom: SPACING.xl },
  header: {
    alignItems: "center",
    paddingTop: SPACING.xl,
    paddingBottom: SPACING.xl,
  },
  logoWrap: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: "rgba(255,255,255,0.2)",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: SPACING.md,
  },
  appName: {
    fontSize: FONTS.sizes.h1,
    fontWeight: "800",
    color: COLORS.white,
  },
  tagline: {
    fontSize: FONTS.sizes.sm,
    color: "rgba(255,255,255,0.75)",
    marginTop: SPACING.xs,
  },
  card: {
    backgroundColor: COLORS.white,
    marginHorizontal: SPACING.md,
    borderRadius: RADIUS.xl,
    padding: SPACING.lg,
  },
  title: {
    fontSize: FONTS.sizes.h2,
    fontWeight: "800",
    color: COLORS.textPrimary,
    marginBottom: SPACING.xs,
  },
  subtitle: {
    fontSize: FONTS.sizes.md,
    color: COLORS.textSecondary,
    marginBottom: SPACING.lg,
  },
  serverErrorBox: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#FEF2F2",
    borderWidth: 1,
    borderColor: "#FECACA",
    borderRadius: RADIUS.md,
    padding: SPACING.sm,
    marginBottom: SPACING.md,
    gap: SPACING.xs,
  },
  serverErrorText: {
    flex: 1,
    fontSize: FONTS.sizes.sm,
    color: COLORS.error,
    lineHeight: 20,
  },
  btn: { marginTop: SPACING.sm },
  linkRow: { alignItems: "center", marginTop: SPACING.md },
  linkText: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
  link: { color: COLORS.primary, fontWeight: "700" },
});
