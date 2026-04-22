import { useState, useRef } from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
  Animated,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

// Validation result states
const STATE = {
  IDLE: "IDLE",
  LOADING: "LOADING",
  VALID: "VALID",
  INVALID: "INVALID",
  ERROR: "ERROR",
};

export default function TicketValidationScreen({ navigation }) {
  const [code, setCode] = useState("");
  const [status, setStatus] = useState(STATE.IDLE);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  // Subtle shake animation for invalid result
  const shakeAnim = useRef(new Animated.Value(0)).current;

  function shake() {
    Animated.sequence([
      Animated.timing(shakeAnim, {
        toValue: 8,
        duration: 60,
        useNativeDriver: true,
      }),
      Animated.timing(shakeAnim, {
        toValue: -8,
        duration: 60,
        useNativeDriver: true,
      }),
      Animated.timing(shakeAnim, {
        toValue: 6,
        duration: 60,
        useNativeDriver: true,
      }),
      Animated.timing(shakeAnim, {
        toValue: -6,
        duration: 60,
        useNativeDriver: true,
      }),
      Animated.timing(shakeAnim, {
        toValue: 0,
        duration: 60,
        useNativeDriver: true,
      }),
    ]).start();
  }

  function reset() {
    setCode("");
    setStatus(STATE.IDLE);
    setResult(null);
    setError(null);
  }

  async function handleValidate() {
    const trimmed = code.trim().toUpperCase();
    if (!trimmed) return;

    setStatus(STATE.LOADING);
    setResult(null);
    setError(null);

    try {
      const res = await passengerApi.validateTicket(trimmed);
      const data = res.data;

      // Backend may return { valid: true/false, ticket: {...}, message: "..." }
      // or just the ticket object on success
      const isValid =
        data?.valid !== undefined
          ? data.valid
          : data?.status === "PAID" || data?.status === "BOOKED";

      if (isValid) {
        setResult(data?.ticket ?? data);
        setStatus(STATE.VALID);
      } else {
        setError(data?.message ?? "Ticket is not valid for boarding.");
        setStatus(STATE.INVALID);
        shake();
      }
    } catch (err) {
      const statusCode = err.response?.status;
      if (statusCode === 404) {
        setError("No ticket found with this code. Please check and try again.");
        setStatus(STATE.INVALID);
        shake();
      } else {
        setError(
          err.response?.data?.message ??
            "Could not validate ticket. Check your connection.",
        );
        setStatus(STATE.ERROR);
      }
    }
  }

  // Format the code input — uppercase, strip non-alphanumeric, max 20 chars
  function handleCodeChange(text) {
    const cleaned = text
      .replace(/[^A-Za-z0-9\-]/g, "")
      .toUpperCase()
      .slice(0, 20);
    setCode(cleaned);
    if (status !== STATE.IDLE) reset();
    setCode(cleaned); // re-set after reset clears it
  }

  const canValidate = code.trim().length >= 3 && status !== STATE.LOADING;

  return (
    <SafeAreaView style={styles.safe} edges={["bottom"]}>
      {/* Header */}
      <View style={styles.topBar}>
        <TouchableOpacity
          onPress={() => navigation.goBack()}
          style={styles.backBtn}
          accessibilityLabel="Go back"
        >
          <Ionicons name="arrow-back" size={22} color={COLORS.white} />
        </TouchableOpacity>
        <Text style={styles.topTitle}>Ticket Validation</Text>
        <View style={{ width: 30 }} />
      </View>

      <ScrollView
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {/* Instruction card */}
        <View style={styles.instructionCard}>
          <Ionicons
            name="information-circle-outline"
            size={20}
            color={COLORS.primary}
          />
          <Text style={styles.instructionText}>
            Enter the ticket code shown on your ticket detail screen to validate
            it for boarding.
          </Text>
        </View>

        {/* Code input area */}
        <Animated.View
          style={[styles.inputCard, { transform: [{ translateX: shakeAnim }] }]}
        >
          <Text style={styles.inputLabel}>Ticket Code</Text>
          <View
            style={[
              styles.codeInputWrap,
              status === STATE.VALID && styles.codeInputValid,
              status === STATE.INVALID && styles.codeInputInvalid,
            ]}
          >
            <Ionicons
              name="ticket-outline"
              size={20}
              color={
                status === STATE.VALID
                  ? COLORS.success
                  : status === STATE.INVALID
                    ? COLORS.error
                    : COLORS.textSecondary
              }
            />
            <TextInput
              style={styles.codeInput}
              value={code}
              onChangeText={handleCodeChange}
              placeholder="e.g. SB-000123"
              placeholderTextColor={COLORS.textMuted}
              autoCapitalize="characters"
              autoCorrect={false}
              returnKeyType="done"
              onSubmitEditing={handleValidate}
              editable={status !== STATE.LOADING}
              accessibilityLabel="Ticket code input"
            />
            {code.length > 0 && status !== STATE.LOADING && (
              <TouchableOpacity onPress={reset} accessibilityLabel="Clear code">
                <Ionicons
                  name="close-circle"
                  size={18}
                  color={COLORS.textMuted}
                />
              </TouchableOpacity>
            )}
          </View>

          {/* Validate button */}
          <TouchableOpacity
            style={[
              styles.validateBtn,
              !canValidate && styles.validateBtnDisabled,
            ]}
            onPress={handleValidate}
            disabled={!canValidate}
            activeOpacity={0.82}
            accessibilityRole="button"
            accessibilityLabel="Validate ticket"
          >
            {status === STATE.LOADING ? (
              <ActivityIndicator size="small" color={COLORS.white} />
            ) : (
              <>
                <Ionicons
                  name="checkmark-circle-outline"
                  size={20}
                  color={COLORS.white}
                />
                <Text style={styles.validateBtnText}>Validate Ticket</Text>
              </>
            )}
          </TouchableOpacity>
        </Animated.View>

        {/* ── VALID result ───────────────────────────────────────────────── */}
        {status === STATE.VALID && result && (
          <View style={styles.resultCard}>
            {/* Success banner */}
            <View style={styles.validBanner}>
              <Ionicons
                name="checkmark-circle"
                size={32}
                color={COLORS.white}
              />
              <View>
                <Text style={styles.validTitle}>Valid Ticket</Text>
                <Text style={styles.validSub}>Passenger may board</Text>
              </View>
            </View>

            {/* Ticket details */}
            <View style={styles.ticketDetails}>
              <DetailRow label="Route" value={result.routeName ?? "—"} />
              <DetailRow
                label="From"
                value={result.origin ?? result.startStop ?? "—"}
              />
              <DetailRow
                label="To"
                value={result.destination ?? result.endStop ?? "—"}
              />
              <DetailRow
                label="Date"
                value={result.travelDate ?? result.date ?? "—"}
              />
              <DetailRow
                label="Departure"
                value={result.departureTime ?? "—"}
              />
              <DetailRow
                label="Passengers"
                value={String(result.passengers ?? 1)}
              />
              <DetailRow
                label="Status"
                value={result.status ?? "PAID"}
                highlight
              />
              <DetailRow
                label="Ticket #"
                value={result.ticketCode ?? `#${result.id ?? "—"}`}
              />
            </View>

            <TouchableOpacity style={styles.validateAnotherBtn} onPress={reset}>
              <Text style={styles.validateAnotherText}>
                Validate Another Ticket
              </Text>
            </TouchableOpacity>
          </View>
        )}

        {/* ── INVALID result ─────────────────────────────────────────────── */}
        {(status === STATE.INVALID || status === STATE.ERROR) && error && (
          <View style={styles.invalidCard}>
            <Ionicons name="close-circle" size={40} color={COLORS.error} />
            <Text style={styles.invalidTitle}>
              {status === STATE.ERROR ? "Validation Error" : "Invalid Ticket"}
            </Text>
            <Text style={styles.invalidMessage}>{error}</Text>
            <TouchableOpacity style={styles.tryAgainBtn} onPress={reset}>
              <Text style={styles.tryAgainText}>Try Again</Text>
            </TouchableOpacity>
          </View>
        )}

        <View style={{ height: SPACING.xl }} />
      </ScrollView>
    </SafeAreaView>
  );
}

function DetailRow({ label, value, highlight }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text
        style={[
          styles.detailValue,
          highlight && { color: COLORS.success, fontWeight: "800" },
        ]}
        numberOfLines={1}
      >
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },

  topBar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.md,
  },
  backBtn: { padding: SPACING.xs },
  topTitle: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "700",
    color: COLORS.white,
  },

  scroll: { padding: SPACING.md },

  instructionCard: {
    flexDirection: "row",
    alignItems: "flex-start",
    backgroundColor: "#EBF4FF",
    borderRadius: RADIUS.md,
    padding: SPACING.md,
    gap: SPACING.sm,
    marginBottom: SPACING.md,
  },
  instructionText: {
    flex: 1,
    fontSize: FONTS.sizes.sm,
    color: COLORS.primary,
    lineHeight: 20,
  },

  inputCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    marginBottom: SPACING.md,
    ...SHADOW.sm,
  },
  inputLabel: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "600",
    color: COLORS.textPrimary,
    marginBottom: SPACING.sm,
  },
  codeInputWrap: {
    flexDirection: "row",
    alignItems: "center",
    borderWidth: 1.5,
    borderColor: COLORS.border,
    borderRadius: RADIUS.md,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    gap: SPACING.sm,
    backgroundColor: COLORS.background,
    marginBottom: SPACING.md,
  },
  codeInputValid: { borderColor: COLORS.success, backgroundColor: "#F0FDF4" },
  codeInputInvalid: { borderColor: COLORS.error, backgroundColor: "#FEF2F2" },
  codeInput: {
    flex: 1,
    fontSize: FONTS.sizes.lg,
    fontWeight: "700",
    color: COLORS.textPrimary,
    letterSpacing: 2,
    padding: 0,
  },

  validateBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: COLORS.primary,
    borderRadius: RADIUS.md,
    paddingVertical: 14,
    gap: SPACING.sm,
    minHeight: 48,
  },
  validateBtnDisabled: { opacity: 0.45 },
  validateBtnText: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.white,
  },

  // Valid result
  resultCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.xl,
    overflow: "hidden",
    ...SHADOW.md,
    marginBottom: SPACING.md,
  },
  validBanner: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.success,
    padding: SPACING.md,
    gap: SPACING.md,
  },
  validTitle: {
    fontSize: FONTS.sizes.xl,
    fontWeight: "800",
    color: COLORS.white,
  },
  validSub: {
    fontSize: FONTS.sizes.sm,
    color: "rgba(255,255,255,0.85)",
    marginTop: 2,
  },
  ticketDetails: { padding: SPACING.md },
  detailRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: SPACING.xs + 2,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.divider,
  },
  detailLabel: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
  },
  detailValue: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "600",
    color: COLORS.textPrimary,
    maxWidth: "60%",
    textAlign: "right",
  },
  validateAnotherBtn: {
    margin: SPACING.md,
    marginTop: 0,
    paddingVertical: SPACING.sm,
    alignItems: "center",
    borderTopWidth: 1,
    borderTopColor: COLORS.divider,
    paddingTop: SPACING.md,
  },
  validateAnotherText: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "700",
    color: COLORS.primary,
  },

  // Invalid result
  invalidCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.xl,
    padding: SPACING.xl,
    alignItems: "center",
    ...SHADOW.sm,
    marginBottom: SPACING.md,
  },
  invalidTitle: {
    fontSize: FONTS.sizes.xl,
    fontWeight: "800",
    color: COLORS.error,
    marginTop: SPACING.md,
    marginBottom: SPACING.sm,
  },
  invalidMessage: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
    textAlign: "center",
    lineHeight: 22,
    marginBottom: SPACING.lg,
  },
  tryAgainBtn: {
    paddingHorizontal: SPACING.xl,
    paddingVertical: SPACING.sm,
    backgroundColor: COLORS.error,
    borderRadius: RADIUS.full,
  },
  tryAgainText: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "700",
    color: COLORS.white,
  },
});
