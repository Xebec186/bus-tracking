import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

export default function RouteCard({ route, onPress }) {
  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      activeOpacity={0.82}
      accessibilityRole="button"
      accessibilityLabel={`Route ${route.name}`}
    >
      {/* Left accent bar */}
      <View style={styles.accent} />

      <View style={styles.body}>
        {/* Route name */}
        <Text style={styles.routeName} numberOfLines={1}>
          {route.name}
        </Text>

        {/* Origin → Destination */}
        <View style={styles.row}>
          <View style={styles.dot} />
          <Text style={styles.stop} numberOfLines={1}>
            {route.startStopName ?? "—"}
          </Text>
        </View>
        <View style={styles.dashedLine} />
        <View style={styles.row}>
          <View style={[styles.dot, styles.dotEnd]} />
          <Text style={styles.stop} numberOfLines={1}>
            {route.endStopName ?? "—"}
          </Text>
        </View>

        {/* Meta row */}
        <View style={styles.meta}>
          {route.stopCount != null && (
            <View style={styles.badge}>
              <Ionicons
                name="location-outline"
                size={12}
                color={COLORS.primary}
              />
              <Text style={styles.badgeText}>{route.stopCount} stops</Text>
            </View>
          )}
          {route.distance != null && (
            <View style={styles.badge}>
              <Ionicons
                name="navigate-outline"
                size={12}
                color={COLORS.primary}
              />
              <Text style={styles.badgeText}>{route.distance} km</Text>
            </View>
          )}
        </View>
      </View>

      <Ionicons name="chevron-forward" size={20} color={COLORS.textMuted} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.surface,
    borderRadius: RADIUS.md,
    marginHorizontal: SPACING.md,
    marginBottom: SPACING.sm,
    overflow: "hidden",
    ...SHADOW.sm,
  },
  accent: {
    width: 4,
    alignSelf: "stretch",
    backgroundColor: COLORS.primary,
  },
  body: {
    flex: 1,
    padding: SPACING.md,
  },
  routeName: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.textPrimary,
    marginBottom: SPACING.sm,
  },
  row: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 2,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: COLORS.primary,
    marginRight: SPACING.sm,
  },
  dotEnd: {
    backgroundColor: COLORS.accent,
  },
  dashedLine: {
    width: 1,
    height: 10,
    backgroundColor: COLORS.border,
    marginLeft: 3.5,
    marginBottom: 2,
  },
  stop: {
    flex: 1,
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
  },
  meta: {
    flexDirection: "row",
    gap: SPACING.sm,
    marginTop: SPACING.sm,
  },
  badge: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.background,
    paddingHorizontal: SPACING.sm,
    paddingVertical: 3,
    borderRadius: RADIUS.full,
    gap: 3,
  },
  badgeText: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.primary,
    fontWeight: "600",
  },
});
