import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, FONTS, SPACING, RADIUS } from '../../constants';

const CONFIG = {
  SCHEDULED:  { label: 'Scheduled',  icon: 'time-outline',          bg: '#EBF4FF', text: '#1A5F7A' },
  IN_PROGRESS:{ label: 'In Progress', icon: 'bus',                   bg: '#E8F8EF', text: '#1A7A44' },
  DEPARTED:   { label: 'Departed',   icon: 'arrow-forward-circle',  bg: '#FFF7E6', text: '#A65C00' },
  ARRIVED:    { label: 'Arrived',    icon: 'checkmark-circle',      bg: '#E8F8EF', text: '#1A7A44' },
  COMPLETED:  { label: 'Completed',  icon: 'checkmark-done-circle', bg: '#F0F0F0', text: '#555' },
  CANCELLED:  { label: 'Cancelled',  icon: 'close-circle',          bg: '#FDEDED', text: '#C0392B' },
};

export default function TripStatusBadge({ status, size = 'md' }) {
  const cfg = CONFIG[status] ?? CONFIG.SCHEDULED;
  const small = size === 'sm';

  return (
    <View style={[styles.badge, { backgroundColor: cfg.bg }, small && styles.badgeSm]}>
      <Ionicons
        name={cfg.icon}
        size={small ? 12 : 14}
        color={cfg.text}
      />
      <Text style={[styles.label, { color: cfg.text }, small && styles.labelSm]}>
        {cfg.label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection:   'row',
    alignItems:      'center',
    paddingHorizontal: SPACING.sm,
    paddingVertical:   5,
    borderRadius:    RADIUS.full,
    gap:             4,
    alignSelf:       'flex-start',
  },
  badgeSm: {
    paddingHorizontal: SPACING.xs,
    paddingVertical:   3,
  },
  label: {
    fontSize:   FONTS.sizes.sm,
    fontWeight: '700',
  },
  labelSm: {
    fontSize: FONTS.sizes.xs,
  },
});
