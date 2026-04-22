import { useState, useEffect, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  RefreshControl,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons, FontAwesome5 } from "@expo/vector-icons";
import { passengerApi } from "../../api/passengerApi";
import { useAuth } from "../../context/AuthContext";
import RouteCard from "../../components/passenger/RouteCard";
import { SkeletonList } from "../../components/common/LoadingSpinner";
import { EmptyState, ErrorBanner } from "../../components/common/EmptyState";
import { COLORS, FONTS, SPACING } from "../../constants";

export default function HomeScreen({ navigation }) {
  const { user, logout } = useAuth();
  const [routes, setRoutes] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const fetchRoutes = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await passengerApi.getRoutes();
      const data = res.data?.content ?? res.data ?? [];
      console.log(data);
      setRoutes(data);
      setFiltered(data);
    } catch {
      setError("Could not load routes. Check your connection.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchRoutes();
  }, [fetchRoutes]);

  // Local search filter — falls back to backend search on submit
  function handleSearch(text) {
    setQuery(text);
    if (!text.trim()) {
      setFiltered(routes);
      return;
    }
    const q = text.toLowerCase();
    setFiltered(
      routes.filter(
        (r) =>
          r.name?.toLowerCase().includes(q) ||
          r.origin?.toLowerCase().includes(q) ||
          r.destination?.toLowerCase().includes(q),
      ),
    );
  }

  async function handleSearchSubmit() {
    if (!query.trim()) {
      setFiltered(routes);
      return;
    }
    setLoading(true);
    try {
      const res = await passengerApi.searchRoute({ query: query.trim() });
      const data = res.data?.content ?? res.data ?? [];
      setFiltered(data);
    } catch {
      // Silently fall back to local filter already applied
    } finally {
      setLoading(false);
    }
  }

  function getGreeting() {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 17) return "Good afternoon";
    return "Good evening";
  }

  return (
    <SafeAreaView style={styles.safe} edges={["top", "bottom"]}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>{getGreeting()},</Text>
          <Text style={styles.userName} numberOfLines={1}>
            {/* {user?.firstName ?? "Passenger"}{" "}
             */}
            John <FontAwesome5 name="smile-beam" size={24} color="white" />
          </Text>
        </View>
        <TouchableOpacity
          style={styles.logoutBtn}
          onPress={logout}
          accessibilityLabel="Log out"
        >
          <Ionicons name="log-out-outline" size={22} color={COLORS.white} />
        </TouchableOpacity>
      </View>

      {/* Search bar */}
      <View style={styles.searchWrap}>
        <View style={styles.searchBar}>
          <Ionicons name="search" size={18} color={COLORS.textSecondary} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search routes, stops…"
            placeholderTextColor={COLORS.textMuted}
            value={query}
            onChangeText={handleSearch}
            onSubmitEditing={handleSearchSubmit}
            returnKeyType="search"
            accessibilityLabel="Search routes"
          />
          {query.length > 0 && (
            <TouchableOpacity onPress={() => handleSearch("")}>
              <Ionicons
                name="close-circle"
                size={18}
                color={COLORS.textMuted}
              />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Section title */}
      <View style={styles.sectionRow}>
        <Text style={styles.sectionTitle}>
          {query ? `Results for "${query}"` : "All Routes"}
        </Text>
        <Text style={styles.count}>{filtered.length} routes</Text>
      </View>

      {/* Error */}
      {error && (
        <ErrorBanner
          message={error}
          onRetry={() => fetchRoutes()}
          onDismiss={() => setError(null)}
        />
      )}

      {/* List */}
      {loading ? (
        <SkeletonList count={5} />
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={(item) => String(item.id ?? item.routeId)}
          renderItem={({ item }) => (
            <RouteCard
              route={item}
              onPress={() =>
                navigation.navigate("RouteDetail", {
                  routeId: item.id ?? item.routeId,
                  routeName: item.name,
                })
              }
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => fetchRoutes(true)}
              colors={[COLORS.primary]}
              tintColor={COLORS.primary}
            />
          }
          ListEmptyComponent={
            <EmptyState
              icon="bus-outline"
              title="No routes found"
              message={
                query
                  ? "Try a different search term."
                  : "Routes will appear here once available."
              }
              actionLabel={query ? "Clear search" : undefined}
              onAction={query ? () => handleSearch("") : undefined}
            />
          }
          contentContainerStyle={[
            filtered.length === 0 && { flex: 1 },
            { paddingBottom: SPACING.xl + SPACING.md },
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
    paddingTop: SPACING.md,
    paddingBottom: SPACING.xl + SPACING.md,
  },
  greeting: { fontSize: FONTS.sizes.sm, color: "rgba(255,255,255,0.8)" },
  userName: {
    fontSize: FONTS.sizes.xl,
    fontWeight: "800",
    color: COLORS.white,
  },
  logoutBtn: { padding: SPACING.sm },

  searchWrap: {
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingBottom: SPACING.lg,
    marginTop: -SPACING.md,
  },
  searchBar: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.white,
    borderRadius: 12,
    paddingHorizontal: SPACING.md,
    paddingVertical: 10,
    gap: SPACING.sm,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  searchInput: {
    flex: 1,
    fontSize: FONTS.sizes.md,
    color: COLORS.textPrimary,
    padding: 0,
  },
  sectionRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.md,
  },
  sectionTitle: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
  },
  count: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
  },
});
