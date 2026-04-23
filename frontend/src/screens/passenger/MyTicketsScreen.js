import { useState, useEffect, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  RefreshControl,
  TouchableOpacity,
  ScrollView,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import { unwrapApiData } from "../../api/responseUtils";
import TicketCard from "../../components/passenger/TicketCard";
import { SkeletonList } from "../../components/common/LoadingSpinner";
import { EmptyState, ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING, RADIUS } from "../../constants";

export default function MyTicketsScreen({ navigation }) {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState("ALL"); // ALL, PENDING, PAID, USED, EXPIRED, CANCELLED

  const fetchTickets = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await passengerApi.getMyTickets();
      const raw = unwrapApiData(res);
      const data = raw?.content ?? raw ?? [];
      // Sort: most recent first
      data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setTickets(data);
    } catch {
      setError("Could not load tickets. Check your connection.");
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

  const FilterBtn = ({ label, value }) => (
    <TouchableOpacity
      style={[styles.filterBtn, filter === value && styles.filterBtnActive]}
      onPress={() => setFilter(value)}
    >
      <Text
        style={[styles.filterText, filter === value && styles.filterTextActive]}
      >
        {label}
      </Text>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>My Tickets</Text>
        <TouchableOpacity
          onPress={() => fetchTickets(true)}
          style={styles.refreshBtn}
          accessibilityLabel="Refresh tickets"
        >
          <Ionicons name="refresh" size={20} color={COLORS.white} />
        </TouchableOpacity>
      </View>

      {/* Filters */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={{ flexGrow: 0 }}
        contentContainerStyle={{ alignItems: "center" }}
      >
        <View style={styles.filterRow}>
          <FilterBtn label="All" value="ALL" />
          <FilterBtn label="Pending" value="PENDING" />
          <FilterBtn label="Paid" value="PAID" />
          <FilterBtn label="Used" value="USED" />
          <FilterBtn label="Expired" value="EXPIRED" />
          <FilterBtn label="Cancelled" value="CANCELLED" />
        </View>
      </ScrollView>

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
          keyExtractor={(item, index) => {
            const id = item.id ?? item.ticketId;
            return id ? String(id) : `ticket-${index}`;
          }}
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
              title="No tickets found"
              message={
                filter === "ALL"
                  ? "You haven't booked any tickets yet."
                  : `No ${filter.toLowerCase()} tickets found.`
              }
              actionLabel={filter === "ALL" ? "Book a Trip" : "Clear Filter"}
              onAction={
                filter === "ALL"
                  ? () => navigation.navigate("Home")
                  : () => setFilter("ALL")
              }
            />
          }
          contentContainerStyle={[
            displayed.length === 0 && { flex: 1 },
            { paddingBottom: SPACING.xl },
          ]}
          showsVerticalScrollIndicator={false}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.md,
  },
  title: {
    fontSize: FONTS.sizes.h2,
    fontWeight: "800",
    color: COLORS.white,
  },
  refreshBtn: { padding: 4 },

  filterRow: {
    flexDirection: "row",
    padding: SPACING.md,
    gap: SPACING.sm,
    backgroundColor: COLORS.white,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.divider,
  },
  filterBtn: {
    paddingHorizontal: SPACING.md,
    paddingVertical: 6,
    borderRadius: RADIUS.full,
    backgroundColor: COLORS.background,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  filterBtnActive: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  filterText: {
    fontSize: FONTS.sizes.xs,
    fontWeight: "600",
    color: COLORS.textSecondary,
  },
  filterTextActive: {
    color: COLORS.white,
  },
});
