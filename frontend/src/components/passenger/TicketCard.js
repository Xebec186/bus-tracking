import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

const STATUS_MAP = {
  BOOKED:    { label: 'Booked',    bg: COLORS.statusBooked,    text: COLORS.statusBookedText },
  PAID:      { label: 'Paid',      bg: COLORS.statusPaid,      text: COLORS.statusPaidText },
  USED:      { label: 'Used',      bg: COLORS.statusUsed,      text: COLORS.statusUsedText },
  CANCELLED: { label: 'Cancelled', bg: COLORS.statusCancelled, text: COLORS.statusCancelledText },
};

export default function TicketCard({ ticket, onPress }) {
  const status = STATUS_MAP[ticket.status] ?? STATUS_MAP.BOOKED;

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      activeOpacity={0.82}
      accessibilityRole="button"
      accessibilityLabel={`Ticket for ${ticket.routeName}`}
    >
      {/* Top strip */}
      <View style={styles.strip}>
        <View style={styles.stripLeft}>
          <Ionicons name="bus" size={16} color={COLORS.white} />
          <Text style={styles.routeName} numberOfLines={1}>{ticket.routeName ?? 'Bus Ticket'}</Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: status.bg }]}>
          <Text style={[styles.statusText, { color: status.text }]}>{status.label}</Text>
        </View>
      </View>

      {/* Body */}
      <View style={styles.body}>
        <View style={styles.journey}>
          <View style={styles.journeyPoint}>
            <Text style={styles.journeyLabel}>From</Text>
            <Text style={styles.journeyStop} numberOfLines={1}>{ticket.origin ?? '—'}</Text>
          </View>
          <Ionicons name="arrow-forward" size={18} color={COLORS.textMuted} />
          <View style={[styles.journeyPoint, { alignItems: 'flex-end' }]}>
            <Text style={styles.journeyLabel}>To</Text>
            <Text style={styles.journeyStop} numberOfLines={1}>{ticket.destination ?? '—'}</Text>
          </View>
        </View>

        <View style={styles.divider} />

        <View style={styles.meta}>
          <MetaItem icon="calendar-outline" value={ticket.date ?? ticket.travelDate ?? '—'} />
          <MetaItem icon="time-outline"     value={ticket.departureTime ?? '—'} />
          <MetaItem icon="ticket-outline"   value={`#${ticket.ticketCode ?? ticket.id ?? '—'}`} />
        </View>
      </View>
    </TouchableOpacity>
  );
}

function MetaItem({ icon, value }) {
  return (
    <View style={styles.metaItem}>
      <Ionicons name={icon} size={13} color={COLORS.textSecondary} />
      <Text style={styles.metaText}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: COLORS.surface,
    borderRadius:    RADIUS.lg,
    marginHorizontal: SPACING.md,
    marginBottom:    SPACING.sm,
    overflow:        'hidden',
    ...SHADOW.sm,
  },
  strip: {
    flexDirection:   'row',
    alignItems:      'center',
    justifyContent:  'space-between',
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
  },
  stripLeft: {
    flexDirection: 'row',
    alignItems:    'center',
    gap:           SPACING.sm,
    flex:          1,
  },
  routeName: {
    fontSize:   FONTS.sizes.sm,
    fontWeight: '700',
    color:      COLORS.white,
    flex:       1,
  },
  statusBadge: {
    paddingHorizontal: SPACING.sm,
    paddingVertical:   3,
    borderRadius:      RADIUS.full,
  },
  statusText: {
    fontSize:   FONTS.sizes.xs,
    fontWeight: '700',
  },
  body: {
    padding: SPACING.md,
  },
  journey: {
    flexDirection:  'row',
    alignItems:     'center',
    justifyContent: 'space-between',
    marginBottom:   SPACING.sm,
  },
  journeyPoint: { flex: 1 },
  journeyLabel: {
    fontSize: FONTS.sizes.xs,
    color:    COLORS.textMuted,
  },
  journeyStop: {
    fontSize:   FONTS.sizes.md,
    fontWeight: '700',
    color:      COLORS.textPrimary,
  },
  divider: {
    height:          1,
    backgroundColor: COLORS.divider,
    marginBottom:    SPACING.sm,
    borderStyle:     'dashed',
  },
  meta: {
    flexDirection: 'row',
    gap:           SPACING.md,
  },
  metaItem: {
    flexDirection: 'row',
    alignItems:    'center',
    gap:           3,
  },
  metaText: {
    fontSize: FONTS.sizes.xs,
    color:    COLORS.textSecondary,
  },
});
