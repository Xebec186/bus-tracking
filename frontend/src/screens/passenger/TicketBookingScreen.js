import React, { useState } from "react";
import { View, Text, ScrollView, StyleSheet } from "react-native";
import {
  SafeAreaView,
  useSafeAreaInsets,
} from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import AppButton from "../../components/common/AppButton";
import AppInput from "../../components/common/AppInput";
import { ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";
import { unwrapApiData } from "../../api/responseUtils";

export default function TicketBookingScreen({ route: navRoute, navigation }) {
  const insets = useSafeAreaInsets();
  const {
    routeId,
    routeName,
    schedule,
    fareEstimate,
    passengers,
    fareType,
    originStopId,
    destinationStopId,
  } = navRoute.params;

  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [cardName, setCardName] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState(null);

  function formatCard(text) {
    const digits = text.replace(/\D/g, "").slice(0, 16);
    return digits.replace(/(.{4})/g, "$1 ").trim();
  }

  function formatExpiry(text) {
    const digits = text.replace(/\D/g, "").slice(0, 4);
    if (digits.length >= 3) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return digits;
  }

  function validate() {
    const e = {};
    const rawCard = cardNumber.replace(/\s/g, "");
    if (rawCard.length !== 16)
      e.cardNumber = "Enter a valid 16-digit card number";
    if (!/^\d{2}\/\d{2}$/.test(expiry)) e.expiry = "Use MM/YY format";
    if (cvv.length < 3) e.cvv = "Enter a valid CVV";
    if (!cardName.trim()) e.cardName = "Cardholder name is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handlePay() {
    if (!validate()) return;
    setServerError(null);
    setLoading(true);
    try {
      // Step 1: book
      const bookRes = await passengerApi.bookTicket({
        scheduleId: schedule?.id ?? schedule?.scheduleId,
        originStopId,
        destinationStopId,
        date: new Date().toISOString().slice(0, 10),
      });
      const booked = unwrapApiData(bookRes);
      const ticketId = booked?.id ?? booked?.ticketId;
      if (!ticketId) {
        throw new Error("Booking response missing ticket id");
      }

      // Step 2: pay
      await passengerApi.payTicket(ticketId, {
        paymentMethod: "CARD",
        paymentReference: `CARD-${Date.now()}-${cardNumber.replace(/\s/g, "").slice(-4)}`,
      });

      // Navigate to confirmation
      navigation.replace("TicketDetail", {
        ticketId,
        fromBooking: true,
      });
    } catch (err) {
      const msg =
        err.response?.data?.message ?? "Payment failed. Please try again.";
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  }

  const total = fareEstimate?.totalFare ?? fareEstimate?.fare ?? "—";

  return (
    <View style={styles.safe}>
      {/* Header */}
      <SafeAreaView edges={["top"]} style={styles.topBar}>
        <Ionicons
          name="arrow-back"
          size={22}
          color={COLORS.white}
          onPress={() => navigation.goBack()}
          accessibilityLabel="Go back"
        />
        <Text style={styles.topTitle}>Book Ticket</Text>
        <View style={{ width: 22 }} />
      </SafeAreaView>

      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}
      >
        {serverError && (
          <ErrorBanner
            message={serverError}
            onDismiss={() => setServerError(null)}
          />
        )}

        {/* Order summary */}
        <View style={styles.summaryCard}>
          <Text style={styles.summaryTitle}>Order Summary</Text>
          <Row label="Route" value={routeName} />
          <Row
            label="Schedule"
            value={`${schedule?.departureTime ?? "—"} · ${schedule?.scheduleDay ?? "Daily"}`}
          />
          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>Total</Text>
            <Text style={styles.totalAmount}>GH₵ {total}</Text>
          </View>
        </View>

        {/* Payment form */}
        <View style={styles.payCard}>
          <View style={styles.payHeader}>
            <Ionicons name="card-outline" size={20} color={COLORS.primary} />
            <Text style={styles.payTitle}>Payment Details</Text>
          </View>

          <AppInput
            label="Card number"
            leftIcon="card-outline"
            placeholder="0000 0000 0000 0000"
            keyboardType="number-pad"
            value={cardNumber}
            onChangeText={(t) => setCardNumber(formatCard(t))}
            error={errors.cardNumber}
            maxLength={19}
          />

          <View style={styles.row}>
            <View style={{ flex: 1 }}>
              <AppInput
                label="Expiry"
                placeholder="MM/YY"
                keyboardType="number-pad"
                value={expiry}
                onChangeText={(t) => setExpiry(formatExpiry(t))}
                error={errors.expiry}
                maxLength={5}
              />
            </View>
            <View style={{ width: SPACING.md }} />
            <View style={{ flex: 1 }}>
              <AppInput
                label="CVV"
                placeholder="123"
                keyboardType="number-pad"
                secureTextEntry
                value={cvv}
                onChangeText={(t) => setCvv(t.replace(/\D/g, "").slice(0, 4))}
                error={errors.cvv}
                maxLength={4}
              />
            </View>
          </View>

          <AppInput
            label="Cardholder name"
            leftIcon="person-outline"
            placeholder="Ama Owusu"
            autoCapitalize="words"
            value={cardName}
            onChangeText={setCardName}
            error={errors.cardName}
          />
        </View>

        <View style={{ height: SPACING.xl }} />
      </ScrollView>

      {/* Pay button footer with safe area padding */}
      <View
        style={[
          styles.footer,
          { paddingBottom: Math.max(insets.bottom, SPACING.md) },
        ]}
      >
        <AppButton
          label={`Pay GH₵ ${total}`}
          onPress={handlePay}
          loading={loading}
        />
      </View>
    </View>
  );
}

function Row({ label, value }) {
  return (
    <View style={styles.summaryRow}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={styles.rowValue}>{value}</Text>
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
  topTitle: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "700",
    color: COLORS.white,
  },
  scroll: { padding: SPACING.md },

  summaryCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    marginBottom: SPACING.md,
    ...SHADOW.sm,
  },
  summaryTitle: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
    marginBottom: SPACING.sm,
  },
  summaryRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: SPACING.xs,
  },
  rowLabel: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
  rowValue: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textPrimary,
    fontWeight: "600",
    maxWidth: "60%",
    textAlign: "right",
  },
  totalRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: SPACING.sm,
    paddingTop: SPACING.sm,
    borderTopWidth: 1,
    borderTopColor: COLORS.divider,
  },
  totalLabel: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },
  totalAmount: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "800",
    color: COLORS.primary,
  },

  payCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    ...SHADOW.sm,
  },
  payHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: SPACING.sm,
    marginBottom: SPACING.md,
  },
  payTitle: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
    flex: 1,
  },

  row: { flexDirection: "row" },

  footer: {
    padding: SPACING.md,
    backgroundColor: COLORS.white,
    borderTopWidth: 1,
    borderTopColor: COLORS.divider,
    ...SHADOW.lg,
  },
});
