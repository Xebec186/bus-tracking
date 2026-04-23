import React, { useState } from "react";
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
import { passengerApi } from "../../api/passengerApi";
import AppInput from "../../components/common/AppInput";
import AppButton from "../../components/common/AppButton";
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from "../../constants";

export default function PassengerProfileScreen() {
  const { user, logout } = useAuth();
  
  // Change password state
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPasswordSection, setShowPasswordSection] = useState(false);

  async function handleChangePassword() {
    if (!oldPassword || !newPassword || !confirmPassword) {
      Alert.alert("Error", "Please fill in all password fields.");
      return;
    }
    if (newPassword !== confirmPassword) {
      Alert.alert("Error", "New passwords do not match.");
      return;
    }
    if (newPassword.length < 8) {
      Alert.alert("Error", "New password must be at least 8 characters.");
      return;
    }

    setLoading(true);
    try {
      await passengerApi.changePassword({
        oldPassword,
        newPassword,
        confirmPassword,
      });
      Alert.alert("Success", "Password changed successfully.");
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setShowPasswordSection(false);
    } catch (err) {
      const msg = err.response?.data?.message ?? "Failed to change password.";
      Alert.alert("Error", msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>My Profile</Text>
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
            {user?.displayName ?? "Passenger"}
          </Text>
          <Text style={styles.emailLabel}>{user?.email ?? ""}</Text>
          <View style={styles.roleBadge}>
            <Ionicons name="person-outline" size={12} color={COLORS.primary} />
            <Text style={styles.roleText}>Passenger</Text>
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
              icon="call-outline"
              label="Phone"
              value={user?.phoneNumber ?? "—"}
            />
          </View>
        </View>

        {/* Change Password Section */}
        <View style={styles.section}>
          <TouchableOpacity 
            style={styles.sectionHeaderRow}
            onPress={() => setShowPasswordSection(!showPasswordSection)}
          >
            <Text style={styles.sectionTitle}>Security</Text>
            <Ionicons 
              name={showPasswordSection ? "chevron-up" : "chevron-down"} 
              size={18} 
              color={COLORS.textSecondary} 
            />
          </TouchableOpacity>
          
          {showPasswordSection ? (
            <View style={styles.passwordCard}>
              <AppInput
                label="Current Password"
                placeholder="••••••••"
                secureTextEntry
                value={oldPassword}
                onChangeText={setOldPassword}
              />
              <AppInput
                label="New Password"
                placeholder="••••••••"
                secureTextEntry
                value={newPassword}
                onChangeText={setNewPassword}
              />
              <AppInput
                label="Confirm New Password"
                placeholder="••••••••"
                secureTextEntry
                value={confirmPassword}
                onChangeText={setConfirmPassword}
              />
              <AppButton
                label="Update Password"
                onPress={handleChangePassword}
                loading={loading}
                style={styles.updateBtn}
              />
            </View>
          ) : (
            <TouchableOpacity 
              style={styles.infoCard} 
              onPress={() => setShowPasswordSection(true)}
            >
              <View style={styles.infoRow}>
                <View style={styles.infoLeft}>
                  <Ionicons name="lock-closed-outline" size={18} color={COLORS.primary} />
                  <Text style={styles.infoLabel}>Change Password</Text>
                </View>
                <Ionicons name="chevron-forward" size={16} color={COLORS.textMuted} />
              </View>
            </TouchableOpacity>
          )}
        </View>

        {/* Sign out */}
        <View style={styles.section}>
          <TouchableOpacity
            style={styles.signOutBtn}
            onPress={logout}
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
        <Ionicons name={icon} size={18} color={COLORS.primary} />
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
    backgroundColor: COLORS.primary,
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
    backgroundColor: COLORS.white,
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
    backgroundColor: COLORS.primary,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: SPACING.md,
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
    color: COLORS.textPrimary,
    marginBottom: SPACING.xs,
  },
  emailLabel: {
    fontSize: FONTS.sizes.sm,
    color: COLORS.textSecondary,
    marginBottom: SPACING.sm,
  },
  roleBadge: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: COLORS.background,
    paddingHorizontal: SPACING.md,
    paddingVertical: 5,
    borderRadius: RADIUS.full,
    gap: 5,
  },
  roleText: {
    fontSize: FONTS.sizes.xs,
    fontWeight: "700",
    color: COLORS.primary,
  },

  section: {
    marginHorizontal: SPACING.md,
    marginBottom: SPACING.md,
  },
  sectionHeaderRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: SPACING.sm,
    paddingRight: SPACING.xs,
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

  passwordCard: {
    backgroundColor: COLORS.white,
    borderRadius: RADIUS.lg,
    padding: SPACING.md,
    ...SHADOW.sm,
  },
  updateBtn: {
    marginTop: SPACING.sm,
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
