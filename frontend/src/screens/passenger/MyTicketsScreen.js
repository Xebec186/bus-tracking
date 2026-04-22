import { useState, useEffect, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  RefreshControl,
  TouchableOpacity,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import TicketCard from "../../components/passenger/TicketCard";
import { SkeletonList } from "../../components/common/LoadingSpinner";
import { EmptyState, ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS } from "../../constants";

const STATUS_FILTERS = ["ALL", "BOOKED", "PAID", "USED"];

export default function MyTicketsScreen({ navigation }) {
  const [tickets, setTickets] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const fetchTickets = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await passengerApi.getTickets();
      setTickets(res.data?.content ?? res.data ?? []);
    } catch {
      setError("Could not load tickets.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  const displayed =
    filter === "ALL" ? tickets : tickets.filter((t) => t.status === filter);

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.titleRow}>
          <Text style={styles.title}>My Tickets</Text>
        </View>
        <Text style={styles.sub}>{tickets.length} total</Text>
      </View>

      {/* Status filter tabs */}
      <View style={styles.filterRow}>
        {STATUS_FILTERS.map((f) => (
          <FilterTab
            key={f}
            label={f.charAt(0) + f.slice(1).toLowerCase()}
            active={filter === f}
            onPress={() => setFilter(f)}
          />
        ))}
      </View>

      {error && (
        <ErrorBanner
          message={error}
          onRetry={() => fetchTickets()}
          onDismiss={() => setError(null)}
        />
      )}

      {loading ? (
        <SkeletonList count={4} />
      ) : (
        <FlatList
          data={displayed}
          keyExtractor={(item) => String(item.id ?? item.ticketId)}
          renderItem={({ item }) => (
            <TicketCard
              ticket={item}
              onPress={() =>
                navigation.navigate("TicketDetail", {
                  ticketId: item.id ?? item.ticketId,
                })
              }
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => fetchTickets(true)}
              colors={[COLORS.primary]}
              tintColor={COLORS.primary}
            />
          }
          ListEmptyComponent={
            <EmptyState
              icon="ticket-outline"
              title={
                filter === "ALL"
                  ? "No tickets yet"
                  : `No ${filter.toLowerCase()} tickets`
              }
              message={
                filter === "ALL"
                  ? "Book your first ticket from the Routes tab."
                  : `You have no ${filter.toLowerCase()} tickets.`
              }
              actionLabel={filter !== "ALL" ? "Show all" : undefined}
              onAction={filter !== "ALL" ? () => setFilter("ALL") : undefined}
            />
          }
          contentContainerStyle={[
            { paddingTop: SPACING.sm, paddingBottom: SPACING.xl },
            displayed.length === 0 && { flex: 1 },
          ]}
          showsVerticalScrollIndicator={false}
        />
      )}
    </SafeAreaView>
  );
}

function FilterTab({ label, active, onPress }) {
  return (
    <View
      style={[styles.filterTab, active && styles.filterTabActive]}
      accessibilityRole="tab"
      accessibilityState={{ selected: active }}
    >
      <Text
        style={[styles.filterText, active && styles.filterTextActive]}
        onPress={onPress}
      >
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },
  header: {
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.md,
    paddingBottom: SPACING.lg,
  },
  title: { fontSize: FONTS.sizes.h2, fontWeight: "800", color: COLORS.white },
  sub: {
    fontSize: FONTS.sizes.sm,
    color: "rgba(255,255,255,0.75)",
    marginTop: 2,
  },

  filterRow: {
    flexDirection: "row",
    backgroundColor: COLORS.white,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.divider,
    gap: SPACING.sm,
  },
  filterTab: {
    paddingHorizontal: SPACING.md,
    paddingVertical: 6,
    borderRadius: RADIUS.full,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  filterTabActive: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  filterText: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
    fontWeight: "600",
  },
  filterTextActive: { color: COLORS.white },
  titleRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  validateBtn: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "rgba(255,255,255,0.2)",
    paddingHorizontal: SPACING.sm,
    paddingVertical: 4,
    borderRadius: RADIUS.full,
    gap: 4,
  },
  validateBtnText: {
    fontSize: FONTS.sizes.xs,
    fontWeight: "700",
    color: COLORS.white,
  },
});
