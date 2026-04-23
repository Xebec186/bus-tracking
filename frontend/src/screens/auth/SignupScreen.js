import { useState, useEffect } from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  Keyboard,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { authApi } from "../../api/authApi";
import { useAuth } from "../../context/AuthContext";
import AppInput from "../../components/common/AppInput";
import AppButton from "../../components/common/AppButton";
import { COLORS, FONTS, SPACING, RADIUS } from "../../constants";

export default function SignupScreen({ navigation }) {
  const { login } = useAuth();

  // Form fields
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState(null);
  const [isKeyboardVisible, setKeyboardVisible] = useState(false);

  // ── Keyboard Listeners ─────────────────────────────────────────────────────
  useEffect(() => {
    const showSubscription = Keyboard.addListener("keyboardDidShow", () => {
      setKeyboardVisible(true);
    });
    const hideSubscription = Keyboard.addListener("keyboardDidHide", () => {
      setKeyboardVisible(false);
    });

    return () => {
      showSubscription.remove();
      hideSubscription.remove();
    };
  }, []);

  // ── Client-side validation ─────────────────────────────────────────────────
  function validate() {
    const e = {};

    if (!firstName.trim()) e.firstName = "First name is required";
    else if (firstName.trim().length > 100)
      e.firstName = "First name cannot exceed 100 characters";

    if (!lastName.trim()) e.lastName = "Last name is required";
    else if (lastName.trim().length > 100)
      e.lastName = "Last name cannot exceed 100 characters";

    if (!email.trim()) e.email = "Email is required";
    else if (!/\S+@\S+\.\S+/.test(email))
      e.email = "Please enter a valid email address";
    else if (email.length > 150) e.email = "Email cannot exceed 150 characters";

    if (phoneNumber.trim() && !/^[0-9]{10}$/.test(phoneNumber.trim()))
      e.phoneNumber = "Phone number must be exactly 10 digits";

    if (!password) e.password = "Password is required";
    else if (password.length < 8)
      e.password = "Password must be at least 8 characters";
    else if (password.length > 72)
      e.password = "Password must be between 8 and 72 characters";
    else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password))
      e.password =
        "Password must contain at least one uppercase letter, one lowercase letter, and one digit";

    if (!confirm) e.confirm = "Please confirm the password";
    else if (confirm !== password) e.confirm = "Passwords do not match";

    setErrors(e);
    return Object.keys(e).length === 0;
  }

  function clearFieldError(field) {
    setErrors((prev) => {
      if (!prev[field]) return prev;
      const next = { ...prev };
      delete next[field];
      return next;
    });
    setServerError(null);
  }

  function handleApiError(err) {
    const data = err.response?.data;
    if (data?.errors && Object.keys(data.errors).length > 0) {
      const mapped = { ...data.errors };
      if (mapped.confirmPassword) {
        mapped.confirm = mapped.confirmPassword;
        delete mapped.confirmPassword;
      }
      setErrors((prev) => ({ ...prev, ...mapped }));
    } else {
      setServerError(data?.message ?? "Registration failed. Please try again.");
    }
  }

  async function handleSignup() {
    setServerError(null);
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await authApi.signup({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim().toLowerCase(),
        phoneNumber: phoneNumber.trim() || undefined,
        password,
        confirmPassword: confirm,
      });
      await login(res.data);
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
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        keyboardVerticalOffset={Platform.OS === "ios" ? 0 : 20}
      >
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
          bounces={isKeyboardVisible}
          scrollEnabled={true} // Re-enabled to allow full visibility of long form
        >
          {/* Compact Header */}
          <View style={styles.header}>
            <TouchableOpacity
              style={styles.backBtn}
              onPress={() => navigation.goBack()}
              accessibilityLabel="Go back"
            >
              <Ionicons name="arrow-back" size={24} color={COLORS.white} />
            </TouchableOpacity>
            <View style={styles.headerTextWrap}>
              <Text style={styles.appName}>Create Account</Text>
              <Text style={styles.tagline}>SmartBus Accra</Text>
            </View>
          </View>

          {/* Card */}
          <View style={styles.card}>
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

            {/* Name row */}
            <View style={styles.nameRow}>
              <View style={styles.nameField}>
                <AppInput
                  label="First name"
                  leftIcon="person-outline"
                  placeholder="Ama"
                  autoCapitalize="words"
                  value={firstName}
                  onChangeText={(v) => {
                    setFirstName(v);
                    clearFieldError("firstName");
                  }}
                  error={errors.firstName}
                />
              </View>
              <View style={styles.nameGap} />
              <View style={styles.nameField}>
                <AppInput
                  label="Last name"
                  placeholder="Owusu"
                  autoCapitalize="words"
                  value={lastName}
                  onChangeText={(v) => {
                    setLastName(v);
                    clearFieldError("lastName");
                  }}
                  error={errors.lastName}
                />
              </View>
            </View>

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
              label="Phone number (optional)"
              leftIcon="call-outline"
              placeholder="0244123456"
              keyboardType="number-pad"
              maxLength={10}
              value={phoneNumber}
              onChangeText={(v) => {
                setPhoneNumber(v.replace(/\D/g, ""));
                clearFieldError("phoneNumber");
              }}
              error={errors.phoneNumber}
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

            {!errors.password ? (
              <Text style={styles.passwordHint}>
                Min 8 chars · uppercase · lowercase · number
              </Text>
            ) : null}

            <AppInput
              label="Confirm password"
              leftIcon="lock-closed-outline"
              placeholder="••••••••"
              secureTextEntry
              value={confirm}
              onChangeText={(v) => {
                setConfirm(v);
                clearFieldError("confirm");
              }}
              error={errors.confirm}
            />

            <AppButton
              label="Create Account"
              onPress={handleSignup}
              loading={loading}
              style={styles.btn}
            />

            <TouchableOpacity
              style={styles.linkRow}
              onPress={() => navigation.navigate("Login")}
              accessibilityRole="link"
            >
              <Text style={styles.linkText}>
                Already have an account?{" "}
                <Text style={styles.link}>Sign in</Text>
              </Text>
            </TouchableOpacity>
          </View>
          
          <View style={{ height: SPACING.xl }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.primary },
  scroll: { 
    flexGrow: 1,
    paddingBottom: SPACING.xl 
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.md,
    paddingBottom: SPACING.md,
  },
  backBtn: {
    padding: SPACING.xs,
    marginRight: SPACING.sm,
  },
  headerTextWrap: {
    flex: 1,
  },
  appName: {
    fontSize: FONTS.sizes.h2,
    fontWeight: "800",
    color: COLORS.white,
  },
  tagline: {
    fontSize: FONTS.sizes.xs,
    color: "rgba(255,255,255,0.75)",
  },
  card: {
    backgroundColor: COLORS.white,
    marginHorizontal: SPACING.md,
    borderRadius: RADIUS.xl,
    padding: SPACING.lg,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.2,
        shadowRadius: 10,
      },
      android: {
        elevation: 8,
      },
    }),
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
  nameRow: {
    flexDirection: "row",
    alignItems: "flex-start",
  },
  nameField: { flex: 1 },
  nameGap: { width: SPACING.sm },
  passwordHint: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textMuted,
    marginTop: -SPACING.sm,
    marginBottom: SPACING.sm,
    marginLeft: 2,
  },
  btn: { marginTop: SPACING.sm },
  linkRow: { alignItems: "center", marginTop: SPACING.md },
  linkText: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
  link: { color: COLORS.primary, fontWeight: "700" },
});
