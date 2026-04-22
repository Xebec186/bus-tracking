import { useState, useEffect } from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  FlatList,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import AppButton from "../../components/common/AppButton";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

export default function RouteDetailScreen({ route: navRoute, navigation }) {
  const { routeId, routeName } = navRoute.params;
  const [routeData, setRouteData] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [routeStops, setRouteStops] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const [rRes, sRes, rsRes] = await Promise.all([
          passengerApi.getRoute(routeId),
          passengerApi.getSchedulesByRoute(routeId),
          passengerApi.getRouteStops(routeId),
        ]);
        setRouteData(rRes.data);
        setSchedules(sRes.data);
        setRouteStops(rsRes.data);
      } catch {
        setError("Failed to load route details.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [routeId]);

  if (loading) return <LoadingSpinner fullScreen message="Loading route…" />;

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Back header */}
      <View style={styles.topBar}>
        <TouchableOpacity
          style={styles.backBtn}
          onPress={() => navigation.goBack()}
          accessibilityLabel="Go back"
        >
          <Ionicons name="arrow-back" size={22} color={COLORS.white} />
        </TouchableOpacity>
        <Text style={styles.topTitle} numberOfLines={1}>
          {routeName}
        </Text>
        <TouchableOpacity
          style={styles.mapBtn}
          onPress={() =>
            navigation.navigate("Tracking", {
              screen: "BusTrackingMain",
              params: { routeId },
            })
          }
          accessibilityLabel="Track buses on this route"
        >
          <Ionicons name="map-outline" size={22} color={COLORS.white} />
        </TouchableOpacity>
      </View>

      {error && (
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
      )}

      <ScrollView showsVerticalScrollIndicator={false}>
        {/* Route summary card */}
        <View style={styles.summaryCard}>
          <View style={styles.journeyRow}>
            <View style={styles.journeyPoint}>
              <View style={[styles.dot, { backgroundColor: COLORS.primary }]} />
              <Text style={styles.journeyLabel}>Origin</Text>
              <Text style={styles.journeyStop}>
                {routeData?.startStopName ?? "—"}
              </Text>
            </View>
            <View style={styles.journeyDivider}>
              <Ionicons
                name="arrow-forward"
                size={20}
                color={COLORS.textMuted}
              />
            </View>
            <View style={[styles.journeyPoint, { alignItems: "flex-end" }]}>
              <View style={[styles.dot, { backgroundColor: COLORS.accent }]} />
              <Text style={styles.journeyLabel}>Destination</Text>
              <Text style={styles.journeyStop}>
                {routeData?.endStopName ?? "—"}
              </Text>
            </View>
          </View>

          <View style={styles.statsRow}>
            <StatChip
              icon="location-outline"
              value={`${routeData?.stopCount ?? "—"} stops`}
            />
            <StatChip
              icon="navigate-outline"
              value={`${routeData?.distanceKm ?? "—"} km`}
            />
            <StatChip
              icon="time-outline"
              value={`${routeData?.estimatedDurationMinutes ?? "—"} min`}
            />
          </View>
        </View>

        {/* Stops list */}
        {routeStops.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Stops</Text>
            {routeStops.map((stop, idx) => (
              <View key={stop.id ?? idx} style={styles.stopRow}>
                <View style={styles.stopIndicator}>
                  <View
                    style={[
                      styles.stopDot,
                      idx === 0 || idx === routeStops.length - 1
                        ? styles.stopDotTerminal
                        : {},
                    ]}
                  />
                  {idx < routeStops.length - 1 && (
                    <View style={styles.stopLine} />
                  )}
                </View>
                <Text style={styles.name}>
                  {stop.name ?? `Stop ${idx + 1}`}
                </Text>
              </View>
            ))}
          </View>
        )}

        {/* Schedules */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Select a Schedule</Text>
          {schedules.length === 0 ? (
            <Text style={styles.noData}>
              No schedules available for this route.
            </Text>
          ) : (
            schedules.map((s) => (
              <TouchableOpacity
                keyExtractor={(item, index) => `${item.id}-${index}`}
                style={[
                  styles.scheduleCard,
                  selected?.id === s.id && styles.scheduleSelected,
                ]}
                onPress={() => setSelected(s)}
                accessibilityRole="radio"
                accessibilityState={{ checked: selected?.id === s.id }}
              >
                <View style={styles.scheduleLeft}>
                  <Text style={styles.scheduleTime}>
                    {s.departureTime ?? s.startTime ?? "—"}
                  </Text>
                  <Text style={styles.scheduleDay}>
                    {s.dayOfWeek ?? s.days ?? "Daily"}
                  </Text>
                </View>
                <View style={styles.scheduleRight}>
                  {s.availableSeats != null && (
                    <Text style={styles.seats}>{s.availableSeats} seats</Text>
                  )}
                  {selected?.id === s.id && (
                    <Ionicons
                      name="checkmark-circle"
                      size={22}
                      color={COLORS.primary}
                    />
                  )}
                </View>
              </TouchableOpacity>
            ))
          )}
        </View>

        <View style={{ height: SPACING.xl }} />
      </ScrollView>

      {/* Bottom CTA */}
      <View style={styles.footer}>
        <AppButton
          label={
            selected
              ? "Continue to Fare Estimate"
              : "Select a Schedule to Continue"
          }
          disabled={!selected}
          onPress={() =>
            navigation.navigate("FareEstimate", {
              routeId,
              routeName,
              schedule: selected,
            })
          }
          style={styles.ctaBtn}
        />
      </View>
    </SafeAreaView>
  );
}

function StatChip({ icon, value }) {
  return (
    <View style={styles.statChip}>
      <Ionicons name={icon} size={14} color={COLORS.primary} />
      <Text style={styles.statValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },

  topBar: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.md,
  },
  backBtn: { padding: SPACING.xs, marginRight: SPACING.sm },
  topTitle: {
    flex: 1,
    fontSize: FONTS.sizes.lg,
    fontWeight: "700",
    color: COLORS.white,
  },
  mapBtn: { padding: SPACING.xs },

  summaryCard: {
    backgroundColor: COLORS.white,
    margin: SPACING.md,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    ...SHADOW.sm,
  },
  journeyRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: SPACING.md,
  },
  journeyPoint: { flex: 1 },
  journeyDivider: { paddingHorizontal: SPACING.sm },
  dot: { width: 10, height: 10, borderRadius: 5, marginBottom: 4 },
  journeyLabel: { fontSize: FONTS.sizes.xs, color: COLORS.textMuted },
  journeyStop: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },

  statsRow: { flexDirection: "row", gap: SPACING.sm, flexWrap: "wrap" },
  statChip: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.background,
    paddingHorizontal: SPACING.sm,
    paddingVertical: 5,
    borderRadius: RADIUS.full,
    gap: 4,
  },
  statValue: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.primary,
    fontWeight: "600",
  },

  section: { paddingHorizontal: SPACING.md, marginBottom: SPACING.md },
  sectionTitle: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
    marginBottom: SPACING.sm,
  },
  noData: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },

  stopRow: { flexDirection: "row", alignItems: "flex-start", marginBottom: 0 },
  stopIndicator: { alignItems: "center", width: 20, marginRight: SPACING.sm },
  stopDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: COLORS.border,
    borderWidth: 2,
    borderColor: COLORS.primary,
  },
  stopDotTerminal: { backgroundColor: COLORS.primary },
  stopLine: {
    width: 2,
    flex: 1,
    minHeight: 20,
    backgroundColor: COLORS.border,
  },
  stopName: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
    paddingVertical: 4,
  },

  scheduleCard: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.md,
    padding: SPACING.md,
    marginBottom: SPACING.sm,
    borderWidth: 2,
    borderColor: COLORS.border,
    ...SHADOW.sm,
  },
  scheduleSelected: { borderColor: COLORS.primary, backgroundColor: "#EBF4FF" },
  scheduleLeft: {},
  scheduleRight: {
    flexDirection: "row",
    alignItems: "center",
    gap: SPACING.sm,
  },
  scheduleTime: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },
  scheduleDay: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textSecondary,
    marginTop: 2,
  },
  seats: { fontSize: FONTS.sizes.sm, color: COLORS.accent, fontWeight: "600" },

  footer: {
    padding: SPACING.md,
    backgroundColor: COLORS.white,
    borderTopWidth: 1,
    borderTopColor: COLORS.divider,
  },
  ctaBtn: { width: "100%" },
});
