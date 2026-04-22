import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, FONTS, SPACING, RADIUS } from '../../constants';

// ─── EmptyState ───────────────────────────────────────────────────────────────
export function EmptyState({ icon = 'bus-outline', title, message, actionLabel, onAction }) {
  return (
    <View style={styles.emptyContainer}>
      <Ionicons name={icon} size={56} color={COLORS.border} />
      <Text style={styles.emptyTitle}>{title}</Text>
      {message ? <Text style={styles.emptyMessage}>{message}</Text> : null}
      {actionLabel && onAction ? (
        <TouchableOpacity style={styles.emptyAction} onPress={onAction}>
          <Text style={styles.emptyActionText}>{actionLabel}</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );
}

// ─── ErrorBanner ──────────────────────────────────────────────────────────────
export function ErrorBanner({ message, onRetry, onDismiss }) {
  return (
    <View style={styles.errorBanner}>
      <Ionicons name="alert-circle-outline" size={20} color={COLORS.error} />
      <Text style={styles.errorText} numberOfLines={2}>{message}</Text>
      <View style={styles.errorActions}>
        {onRetry && (
          <TouchableOpacity onPress={onRetry} style={styles.errorBtn}>
            <Text style={styles.errorBtnText}>Retry</Text>
          </TouchableOpacity>
        )}
        {onDismiss && (
          <TouchableOpacity onPress={onDismiss}>
            <Ionicons name="close" size={18} color={COLORS.textSecondary} />
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

// ─── WsBanner — WebSocket status banner ──────────────────────────────────────
export function WsBanner({ message }) {
  if (!message) return null;
  return (
    <View style={styles.wsBanner}>
      <Ionicons name="wifi-outline" size={16} color={COLORS.warning} />
      <Text style={styles.wsText}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  emptyContainer: {
    flex:            1,
    alignItems:      'center',
    justifyContent:  'center',
    padding:         SPACING.xxl,
  },
  emptyTitle: {
    marginTop:  SPACING.md,
    fontSize:   FONTS.sizes.lg,
    fontWeight: '700',
    color:      COLORS.textPrimary,
    textAlign:  'center',
  },
  emptyMessage: {
    marginTop:  SPACING.sm,
    fontSize:   FONTS.sizes.md,
    color:      COLORS.textSecondary,
    textAlign:  'center',
    lineHeight: 22,
  },
  emptyAction: {
    marginTop:       SPACING.lg,
    paddingVertical: SPACING.sm,
    paddingHorizontal: SPACING.lg,
    backgroundColor: COLORS.primary,
    borderRadius:    RADIUS.full,
  },
  emptyActionText: {
    color:      COLORS.white,
    fontWeight: '700',
    fontSize:   FONTS.sizes.md,
  },

  errorBanner: {
    flexDirection:  'row',
    alignItems:     'center',
    backgroundColor:'#FEF2F2',
    borderLeftWidth: 4,
    borderLeftColor: COLORS.error,
    marginHorizontal: SPACING.md,
    marginTop:       SPACING.sm,
    padding:         SPACING.sm,
    borderRadius:    RADIUS.sm,
    gap:             SPACING.sm,
  },
  errorText: {
    flex:     1,
    fontSize: FONTS.sizes.sm,
    color:    COLORS.error,
  },
  errorActions: {
    flexDirection: 'row',
    alignItems:    'center',
    gap:           SPACING.sm,
  },
  errorBtn: {
    paddingHorizontal: SPACING.sm,
    paddingVertical:   4,
    backgroundColor:   COLORS.error,
    borderRadius:      RADIUS.sm,
  },
  errorBtnText: {
    color:    COLORS.white,
    fontSize: FONTS.sizes.xs,
    fontWeight:'700',
  },

  wsBanner: {
    flexDirection:  'row',
    alignItems:     'center',
    backgroundColor:'#FFFBEB',
    padding:         SPACING.sm,
    gap:             SPACING.xs,
  },
  wsText: {
    fontSize: FONTS.sizes.sm,
    color:    COLORS.warning,
  },
});
