import React from "react";
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  Alert,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useAuth } from "../../context/AuthContext";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

export default function DriverProfileScreen() {
  const { user, logout } = useAuth();

  function confirmLogout() {
    Alert.alert(
      "Sign Out",
      "Are you sure you want to sign out?",
      [
        { text: "Cancel", style: "cancel" },
        { text: "Sign Out", style: "destructive", onPress: logout },
      ]
    );
  }

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Driver Profile</Text>
      </View>

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scroll}
      >
        {/* Avatar + name card */}
        <View style={styles.avatarCard}>
          <View style={styles.avatar}>
            <Text style={styles.avatarInitials}>
              {(user?.firstName?.[0] ?? "").toUpperCase()}
              {(user?.lastName?.[0] ?? "").toUpperCase()}
            </Text>
          </View>
          <Text style={styles.displayName}>
            {user?.displayName ?? "Driver"}
          </Text>
          <Text style={styles.emailLabel}>{user?.email ?? ""}</Text>
          <View style={styles.roleBadge}>
            <Ionicons name="bus-outline" size={12} color={COLORS.accentLight} />
            <Text style={styles.roleText}>Driver</Text>
          </View>
        </View>

        {/* Driver assignment */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Assignment</Text>
          <View style={styles.assignCard}>
            <View style={styles.assignIcon}>
              <Ionicons name="bus" size={28} color={COLORS.white} />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={styles.assignLabel}>Assigned Bus</Text>
              <Text style={styles.assignValue}>
                {user?.busId ? `Bus #${user.busId}` : "Not assigned"}
              </Text>
            </View>
            <View
              style={[
                styles.statusDot,
                { backgroundColor: user?.busId ? COLORS.accent : COLORS.textMuted },
              ]}
            />
          </View>
        </View>

        {/* Account info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Account Information</Text>
          <View style={styles.infoCard}>
            <InfoRow
              icon="person-outline"
              label="First name"
              value={user?.firstName ?? "—"}
            />
            <Divider />
            <InfoRow
              icon="person-outline"
              label="Last name"
              value={user?.lastName ?? "—"}
            />
            <Divider />
            <InfoRow
              icon="mail-outline"
              label="Email"
              value={user?.email ?? "—"}
            />
            <Divider />
            <InfoRow
              icon="finger-print-outline"
              label="Driver ID"
              value={user?.userId ? `#${user.userId}` : "—"}
            />
            <Divider />
            <InfoRow
              icon="bus-outline"
              label="Bus ID"
              value={user?.busId ? `#${user.busId}` : "Not assigned"}
            />
          </View>
        </View>

        {/* App info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>App</Text>
          <View style={styles.infoCard}>
            <InfoRow
              icon="bus-outline"
              label="Application"
              value="SmartBus Accra"
            />
            <Divider />
            <InfoRow
              icon="information-circle-outline"
              label="Version"
              value="1.0.0"
            />
            <Divider />
            <InfoRow
              icon="school-outline"
              label="Institution"
              value="University of the West of Scotland"
            />
          </View>
        </View>

        {/* Sign out */}
        <View style={styles.section}>
          <TouchableOpacity
            style={styles.signOutBtn}
            onPress={confirmLogout}
            activeOpacity={0.82}
            accessibilityRole="button"
            accessibilityLabel="Sign out"
          >
            <Ionicons name="log-out-outline" size={20} color={COLORS.error} />
            <Text style={styles.signOutText}>Sign Out</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: SPACING.xl }} />
      </ScrollView>
    </SafeAreaView>
  );
}

function InfoRow({ icon, label, value }) {
  return (
    <View style={styles.infoRow}>
      <View style={styles.infoLeft}>
        <Ionicons name={icon} size={18} color={COLORS.accent} />
        <Text style={styles.infoLabel}>{label}</Text>
      </View>
      <Text style={styles.infoValue} numberOfLines={1}>
        {value}
      </Text>
    </View>
  );
}

function Divider() {
  return <View style={styles.divider} />;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: COLORS.background },

  header: {
    backgroundColor: COLORS.primaryDark,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.md,
    paddingBottom: SPACING.lg,
  },
  headerTitle: {
    fontSize: FONTS.sizes.h2,
    fontWeight: "800",
    color: COLORS.white,
  },

  scroll: { paddingTop: SPACING.md },

  avatarCard: {
    alignItems: "center",
    backgroundColor: COLORS.primaryDark,
    marginHorizontal: SPACING.md,
    borderRadius: RADIUS.xl,
    paddingVertical: SPACING.xl,
    paddingHorizontal: SPACING.lg,
    marginBottom: SPACING.md,
    ...SHADOW.sm,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: "rgba(255,255,255,0.2)",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: SPACING.md,
    borderWidth: 2,
    borderColor: "rgba(255,255,255,0.3)",
  },
  avatarInitials: {
    fontSize: FONTS.sizes.xxl,
    fontWeight: "800",
    color: COLORS.white,
    letterSpacing: 2,
  },
  displayName: {
    fontSize: FONTS.sizes.xl,
    fontWeight: "800",
    color: COLORS.white,
    marginBottom: SPACING.xs,
  },
  emailLabel: {
    fontSize: FONTS.sizes.sm,
    color: "rgba(255,255,255,0.7)",
    marginBottom: SPACING.sm,
  },
  roleBadge: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "rgba(255,255,255,0.15)",
    paddingHorizontal: SPACING.md,
    paddingVertical: 5,
    borderRadius: RADIUS.full,
    gap: 5,
  },
  roleText: {
    fontSize: FONTS.sizes.xs,
    fontWeight: "700",
    color: COLORS.white,
  },

  section: {
    marginHorizontal: SPACING.md,
    marginBottom: SPACING.md,
  },
  sectionTitle: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "700",
    color: COLORS.textSecondary,
    textTransform: "uppercase",
    letterSpacing: 0.8,
    marginBottom: SPACING.sm,
    marginLeft: SPACING.xs,
  },

  assignCard: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    gap: SPACING.md,
    ...SHADOW.sm,
  },
  assignIcon: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: COLORS.accent,
    alignItems: "center",
    justifyContent: "center",
  },
  assignLabel: {
    fontSize: FONTS.sizes.xs,
    color: COLORS.textSecondary,
    marginBottom: 2,
  },
  assignValue: {
    fontSize: FONTS.sizes.lg,
    fontWeight: "800",
    color: COLORS.textPrimary,
  },
  statusDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },

  infoCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    overflow: "hidden",
    ...SHADOW.sm,
  },
  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm + 2,
  },
  infoLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: SPACING.sm,
  },
  infoLabel: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
  },
  infoValue: {
    fontSize: FONTS.sizes.sm,
    fontWeight: "600",
    color: COLORS.textPrimary,
    maxWidth: "55%",
    textAlign: "right",
  },
  divider: {
    height: 1,
    backgroundColor: COLORS.divider,
    marginLeft: SPACING.md + 18 + SPACING.sm,
  },

  signOutBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    paddingVertical: SPACING.md,
    gap: SPACING.sm,
    borderWidth: 1.5,
    borderColor: "#FECACA",
    ...SHADOW.sm,
  },
  signOutText: {
    fontSize: FONTS.sizes.md,
    fontWeight: "700",
    color: COLORS.error,
  },
});
