import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import { unwrapApiData } from "../../api/responseUtils";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

const STATUS_COLOR = {
  BOOKED: { bg: COLORS.statusBooked, text: COLORS.statusBookedText },
  PAID: { bg: COLORS.statusPaid, text: COLORS.statusPaidText },
  USED: { bg: COLORS.statusUsed, text: COLORS.statusUsedText },
  CANCELLED: { bg: COLORS.statusCancelled, text: COLORS.statusCancelledText },
};

export default function TicketDetailScreen({ route: navRoute, navigation }) {
  const { ticketId, fromBooking } = navRoute.params;
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    passengerApi
      .getTicket(ticketId)
      .then((res) => setTicket(unwrapApiData(res)))
      .catch(() => setError("Could not load ticket details."))
      .finally(() => setLoading(false));
  }, [ticketId]);

  if (loading) return <LoadingSpinner fullScreen message="Loading ticket…" />;

  const status = STATUS_COLOR[ticket?.status] ?? STATUS_COLOR.BOOKED;

  return (
    <SafeAreaView style={styles.safe} edges={["bottom"]}>
      {/* Header */}
      <View style={styles.topBar}>
        <TouchableOpacity
          onPress={() =>
            fromBooking ? navigation.navigate("MyTickets") : navigation.goBack()
          }
          style={styles.backBtn}
          accessibilityLabel="Go back"
        >
          <Ionicons name="arrow-back" size={22} color={COLORS.white} />
        </TouchableOpacity>
        <Text style={styles.topTitle}>Ticket Details</Text>
        <View style={{ width: 30 }} />
      </View>

      {error && (
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
      )}

      {ticket && (
        <ScrollView
          contentContainerStyle={styles.scroll}
          showsVerticalScrollIndicator={false}
        >
          {/* Main ticket card */}
          <View style={styles.ticketCard}>
            {/* Top band */}
            <View style={styles.band}>
              <Ionicons name="bus" size={20} color={COLORS.white} />
              <Text style={styles.bandRoute} numberOfLines={1}>
                {ticket.routeName ?? "SmartBus"}
              </Text>
              <View
                style={[styles.statusBadge, { backgroundColor: status.bg }]}
              >
                <Text style={[styles.statusText, { color: status.text }]}>
                  {ticket.status}
                </Text>
              </View>
            </View>

            {/* Journey */}
            <View style={styles.journey}>
              <View style={styles.journeyEnd}>
                <Text style={styles.journeyLabel}>FROM</Text>
                <Text style={styles.journeyStop}>{ticket.origin ?? "—"}</Text>
              </View>
              <View style={styles.journeyMiddle}>
                <View style={styles.journeyLine} />
                <Ionicons name="bus-outline" size={22} color={COLORS.primary} />
                <View style={styles.journeyLine} />
              </View>
              <View style={[styles.journeyEnd, { alignItems: "flex-end" }]}>
                <Text style={styles.journeyLabel}>TO</Text>
                <Text style={styles.journeyStop}>
                  {ticket.destination ?? "—"}
                </Text>
              </View>
            </View>

            {/* Dashed separator */}
            <View style={styles.tear}>
              <View style={styles.tearCircleLeft} />
              <View style={styles.tearLine} />
              <View style={styles.tearCircleRight} />
            </View>

            {/* Meta grid */}
            <View style={styles.metaGrid}>
              <MetaCell
                label="Date"
                value={ticket.travelDate ?? ticket.date ?? "—"}
              />
              <MetaCell label="Boarding" value={ticket.boardingTime ?? "—"} />
              <MetaCell label="Amount" value={`GH₵ ${ticket.price ?? "—"}`} />
              <MetaCell
                label="Payment Method"
                value={ticket.paymentMethod ?? "—"}
              />
              <MetaCell
                label="Payment Reference"
                value={ticket.paymentReference ?? "—"}
              />
              <MetaCell label="Ticket #" value={ticket.code ?? "-"} />
            </View>

            <View style={styles.qrSection}>
              <View style={styles.qrBox}>
                <Ionicons
                  name="qr-code-outline"
                  size={72}
                  color={COLORS.textMuted}
                />
              </View>
              <Text style={styles.qrLabel}>Ticket Code</Text>
              <Text style={styles.qrCode}>{ticket.code ?? "-"}</Text>
              <Text style={styles.qrHint}>
                Show this to the driver to board
              </Text>
            </View>
          </View>

          {/* Back to tickets button */}
          {fromBooking && (
            <TouchableOpacity
              style={styles.backLink}
              onPress={() => navigation.navigate("MyTickets")}
            >
              <Ionicons
                name="ticket-outline"
                size={16}
                color={COLORS.primary}
              />
              <Text style={styles.backLinkText}>View all my tickets</Text>
            </TouchableOpacity>
          )}

          <View style={{ height: SPACING.xl }} />
        </ScrollView>
      )}
    </SafeAreaView>
  );
}

function MetaCell({ label, value }) {
  return (
    <View style={styles.metaCell}>
      <Text style={styles.metaLabel}>{label}</Text>
      <Text style={styles.metaValue} numberOfLines={2}>
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

  ticketCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.xl,
    overflow: "hidden",
    ...SHADOW.md,
  },
  band: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    gap: SPACING.sm,
  },
  bandRoute: {
    flex: 1,
    fontSize: FONTS.sizes.sm,
    fontWeight: "700",
    color: COLORS.white,
  },
  statusBadge: {
    paddingHorizontal: SPACING.sm,
    paddingVertical: 3,
    borderRadius: RADIUS.full,
  },
  statusText: { fontSize: FONTS.sizes.xs, fontWeight: "700" },

  journey: {
    flexDirection: "row",
    alignItems: "center",
    padding: SPACING.lg,
  },
  journeyEnd: { flex: 1 },
  journeyLabel: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textMuted,
    letterSpacing: 1,
  },
  journeyStop: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "800",
    color: COLORS.textPrimary,
    marginTop: 2,
  },
  journeyMiddle: { alignItems: "center", paddingHorizontal: SPACING.sm },
  journeyLine: { width: 30, height: 1.5, backgroundColor: COLORS.border },

  tear: {
    flexDirection: "row",
    alignItems: "center",
    marginHorizontal: -1,
  },
  tearCircleLeft: {
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: COLORS.background,
  },
  tearCircleRight: {
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: COLORS.background,
  },
  tearLine: {
    flex: 1,
    height: 1.5,
    borderStyle: "dashed",
    borderWidth: 1,
    borderColor: COLORS.border,
  },

  metaGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    padding: SPACING.md,
    paddingTop: SPACING.sm,
    gap: SPACING.sm,
  },
  metaCell: {
    width: "30%",
    flex: 1,
    minWidth: "30%",
    backgroundColor: COLORS.background,
    borderRadius: RADIUS.sm,
    padding: SPACING.sm,
  },
  metaLabel: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textMuted,
    marginBottom: 2,
  },
  metaValue: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },

  qrSection: {
    alignItems: "center",
    paddingBottom: SPACING.lg,
    paddingTop: SPACING.sm,
  },
  qrBox: {
    width: 120,
    height: 120,
    backgroundColor: COLORS.background,
    borderRadius: RADIUS.md,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: COLORS.border,
    marginBottom: SPACING.sm,
  },
  qrLabel: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textMuted,
    letterSpacing: 1,
  },
  qrCode: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "800",
    color: COLORS.textPrimary,
    marginTop: 2,
  },
  qrHint: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textSecondary,
    marginTop: SPACING.xs,
  },

  backLink: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: SPACING.xs,
    marginTop: SPACING.md,
    padding: SPACING.sm,
  },
  backLinkText: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.primary,
    fontWeight: "600",
  },
});
