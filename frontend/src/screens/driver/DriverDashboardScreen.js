import React, { useState, useEffect, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  RefreshControl,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useAuth } from "../../context/AuthContext";
import { driverApi } from "../../api/driverApi";
import { unwrapApiData } from "../../api/responseUtils";
import TripStatusBadge from "../../components/driver/TripStatusBadge";
import { SkeletonList } from "../../components/common/LoadingSpinner";
import { EmptyState, ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

export default function DriverDashboardScreen({ navigation }) {
  const { user } = useAuth();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const driverId = user?.userId;

  const fetchTrips = useCallback(
    async (isRefresh = false) => {
      if (isRefresh) setRefreshing(true);
      else setLoading(true);
      setError(null);
      try {
        const res = await driverApi.getTripsByDriver(driverId);
        const raw = unwrapApiData(res);
        const data = raw?.content ?? raw ?? [];
        // Sort: in-progress first, then scheduled, then rest
        const order = { IN_PROGRESS: 0, DEPARTED: 1, SCHEDULED: 2 };
        data.sort((a, b) => (order[a.status] ?? 99) - (order[b.status] ?? 99));
        setTrips(data);
      } catch {
        setError("Could not load trips. Check your connection.");
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [driverId],
  );

  useEffect(() => {
    fetchTrips();
  }, [fetchTrips]);

  const activeTrip = trips.find((t) =>
    ["IN_PROGRESS", "DEPARTED"].includes(t.status),
  );

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Driver Dashboard</Text>
          <Text style={styles.driverName}>{user?.displayName ?? "Driver"}</Text>
        </View>
        <View style={{ flexDirection: "row", alignItems: "center", gap: SPACING.sm }}>
          <TouchableOpacity
            style={styles.scanBtn}
            onPress={() => navigation.navigate("DriverProfile")}
            accessibilityLabel="Profile"
          >
            <Ionicons name="person-circle-outline" size={22} color={COLORS.white} />
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.scanBtn}
            onPress={() => navigation.navigate("TicketValidation")}
            accessibilityLabel="Scan ticket"
          >
            <Ionicons name="scan" size={22} color={COLORS.white} />
          </TouchableOpacity>
        </View>
      </View>

      {/* Active trip banner */}
      {activeTrip && (
        <TouchableOpacity
          style={styles.activeBanner}
          onPress={() =>
            navigation.navigate("ActiveTrip", {
              tripId: activeTrip.id ?? activeTrip.tripId,
            })
          }
          activeOpacity={0.88}
          accessibilityLabel="Open active trip"
        >
          <View style={styles.activePulse}>
            <Ionicons name="radio-button-on" size={16} color={COLORS.accent} />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.activeLabel}>Active Trip in Progress</Text>
            <Text style={styles.activeRoute} numberOfLines={1}>
              {activeTrip.routeName ?? `Trip #${activeTrip.id}`}
            </Text>
          </View>
          <Ionicons name="chevron-forward" size={20} color={COLORS.white} />
        </TouchableOpacity>
      )}

      {/* Bus info card */}
      <View style={styles.busCard}>
        <Ionicons name="bus" size={28} color={COLORS.primary} />
        <View style={{ flex: 1, marginLeft: SPACING.md }}>
          <Text style={styles.busLabel}>Driver Account</Text>
          <Text style={styles.busId}>Driver #{driverId ?? "—"}</Text>
        </View>
        <View style={styles.tripCount}>
          <Text style={styles.tripCountNum}>{trips.length}</Text>
          <Text style={styles.tripCountLabel}>trips today</Text>
        </View>
      </View>

      {/* Section */}
      <View style={styles.sectionRow}>
        <Text style={styles.sectionTitle}>All Trips</Text>
      </View>

      {error && (
        <ErrorBanner
          message={error}
          onRetry={() => fetchTrips()}
          onDismiss={() => setError(null)}
        />
      )}

      {loading ? (
        <SkeletonList count={4} />
      ) : (
        <FlatList
          data={trips}
          keyExtractor={(item) => String(item.id ?? item.tripId)}
          renderItem={({ item }) => (
            <TripCard
              trip={item}
              onPress={() =>
                navigation.navigate("ActiveTrip", {
                  tripId: item.id ?? item.tripId,
                })
              }
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => fetchTrips(true)}
              colors={[COLORS.primary]}
              tintColor={COLORS.primary}
            />
          }
          ListEmptyComponent={
            <EmptyState
              icon="bus-outline"
              title="No trips assigned"
              message="Your trips will appear here once scheduled by the admin."
            />
          }
          contentContainerStyle={
            trips.length === 0 ? { flex: 1 } : { paddingBottom: SPACING.xl }
          }
          showsVerticalScrollIndicator={false}
        />
      )}
    </SafeAreaView>
  );
}

function TripCard({ trip, onPress }) {
  return (
    <TouchableOpacity
      style={styles.tripCard}
      onPress={onPress}
      activeOpacity={0.82}
    >
      <View style={styles.tripLeft}>
        <Text style={styles.tripRoute} numberOfLines={1}>
          {trip.routeName ?? `Trip #${trip.id ?? trip.tripId}`}
        </Text>
        <Text style={styles.tripTime}>
          {trip.scheduledDeparture ?? trip.departureTime ?? "—"}
          {trip.scheduledArrival ? ` → ${trip.scheduledArrival}` : ""}
        </Text>
        <TripStatusBadge status={trip.status} size="sm" />
      </View>
      <Ionicons name="chevron-forward" size={18} color={COLORS.textMuted} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: COLORS.primaryDark,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.md,
  },
  greeting: { fontSize: FONTS.sizes.sm, color: "rgba(255,255,255,0.75)" },
  driverName: {
    fontSize: FONTS.sizes.xl,
    fontWeight: "800",
    color: COLORS.white,
  },
  scanBtn: {
    padding: SPACING.sm,
    backgroundColor: "rgba(255,255,255,0.2)",
    borderRadius: RADIUS.md,
  },

  activeBanner: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.accent,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    gap: SPACING.sm,
  },
  activePulse: {},
  activeLabel: {
    fontSize: FONTS.sizes.xs,
    color: "rgba(255,255,255,0.85)",
    fontWeight: "600",
  },
  activeRoute: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.white,
    fontWeight: "700",
  },

  busCard: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.white,
    margin: SPACING.md,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    ...SHADOW.sm,
  },
  busLabel: { fontSize: FONTS.sizes.xs, color: COLORS.textSecondary },
  busId: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "800",
    color: COLORS.textPrimary,
  },
  tripCount: { alignItems: "flex-end" },
  tripCountNum: {
    fontSize: FONTS.sizes.xxl,
    fontWeight: "800",
    color: COLORS.primary,
  },
  tripCountLabel: { fontSize: FONTS.sizes.xs, color: COLORS.textSecondary },

  sectionRow: { paddingHorizontal: SPACING.md, paddingBottom: SPACING.sm },
  sectionTitle: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },

  tripCard: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.white,
    marginHorizontal: SPACING.md,
    marginBottom: SPACING.sm,
    borderRadius: RADIUS.md,
    padding: SPACING.md,
    ...SHADOW.sm,
  },
  tripLeft: { flex: 1, gap: 4 },
  tripRoute: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },
  tripTime: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
});
