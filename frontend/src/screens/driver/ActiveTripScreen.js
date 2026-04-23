import React, { useState, useEffect } from 'react';
import {
  View, Text, ScrollView, StyleSheet,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { driverApi } from '../../api/driverApi';
import TripStatusBadge from '../../components/driver/TripStatusBadge';
import AppButton from '../../components/common/AppButton';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ErrorBanner } from '../../components/common/EmptyState';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

export default function ActiveTripScreen({ route: navRoute, navigation }) {
  const { tripId } = navRoute.params;
  const [trip,     setTrip]     = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [departing,setDeparting]= useState(false);
  const [arriving, setArriving] = useState(false);

  useEffect(() => {
    driverApi.getTrip(tripId)
      .then((res) => setTrip(res.data))
      .catch(() => setError('Failed to load trip details.'))
      .finally(() => setLoading(false));
  }, [tripId]);

  async function handleDepart() {
    setError(null);
    setDeparting(true);
    try {
      await driverApi.depart(tripId);
      setTrip((prev) => ({ ...prev, status: 'DEPARTED' }));
    } catch {
      setError("Could not mark departure. Try again.");
    } finally {
      setDeparting(false);
    }
  }

  async function handleArrive() {
    setError(null);
    setArriving(true);
    try {
      await driverApi.arrive(tripId);
      await driverApi.updateTripStatus(tripId, 'COMPLETED');
      setTrip((prev) => ({ ...prev, status: 'COMPLETED' }));
    } catch {
      setError("Could not mark arrival. Try again.");
    } finally {
      setArriving(false);
    }
  }

  if (loading) return <LoadingSpinner fullScreen message="Loading trip…" />;

  const status     = trip?.status ?? 'SCHEDULED';
  const isScheduled   = status === 'SCHEDULED';
  const isDeparted    = status === 'DEPARTED' || status === 'IN_PROGRESS';
  const isCompleted   = status === 'COMPLETED' || status === 'ARRIVED';

  return (
    <SafeAreaView style={styles.safe} edges={['bottom']}>
      {/* Header */}
      <View style={styles.topBar}>
        <Ionicons
          name="arrow-back"
          size={22}
          color={COLORS.white}
          onPress={() => navigation.goBack()}
          accessibilityLabel="Go back"
        />
        <Text style={styles.topTitle}>Active Trip</Text>
        <View style={{ width: 22 }} />
      </View>

      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      <ScrollView contentContainerStyle={styles.scroll} showsVerticalScrollIndicator={false}>

        {/* Status card */}
        <View style={styles.statusCard}>
          <TripStatusBadge status={status} size="md" />
          <Text style={styles.tripRoute} numberOfLines={2}>
            {trip?.routeName ?? `Trip #${tripId}`}
          </Text>
          <Text style={styles.tripSub}>
            {trip?.scheduledDeparture ?? trip?.departureTime ?? '—'}
            {trip?.scheduledArrival ? ` → ${trip.scheduledArrival}` : ''}
          </Text>
        </View>

        {/* Journey detail */}
        <View style={styles.journeyCard}>
          <JourneyRow
            icon="ellipse"
            iconColor={COLORS.primary}
            label="Origin"
            value={trip?.origin ?? trip?.startStop ?? '—'}
          />
          <View style={styles.verticalLine} />
          <JourneyRow
            icon="location"
            iconColor={COLORS.accent}
            label="Destination"
            value={trip?.destination ?? trip?.endStop ?? '—'}
          />
        </View>

        {/* Trip meta */}
        <View style={styles.metaCard}>
          <MetaRow label="Trip ID"       value={`#${trip?.id ?? tripId}`} />
          <MetaRow label="Schedule"      value={trip?.scheduleId ? `#${trip.scheduleId}` : '—'} />
          <MetaRow label="Passengers"    value={String(trip?.passengerCount ?? '—')} />
          <MetaRow label="Bus"           value={`Bus #${trip?.busId ?? '—'}`} />
        </View>

        {/* Location note */}
        <View style={styles.noteCard}>
          <Ionicons name="information-circle-outline" size={18} color={COLORS.info} />
          <Text style={styles.noteText}>
            Bus location updates are managed automatically by the system. No action required from you.
          </Text>
        </View>

        {/* Progress timeline */}
        <View style={styles.timeline}>
          <TimelineStep
            done={true}
            active={isScheduled}
            label="Scheduled"
            icon="time-outline"
          />
          <TimelineStep
            done={isDeparted || isCompleted}
            active={isDeparted}
            label="Departed"
            icon="arrow-forward-circle-outline"
          />
          <TimelineStep
            done={isCompleted}
            active={isCompleted}
            label="Arrived"
            icon="checkmark-circle-outline"
            last
          />
        </View>

        <View style={{ height: SPACING.xl }} />
      </ScrollView>

      {/* Action buttons */}
      {!isCompleted && (
        <View style={styles.footer}>
          {isScheduled && (
            <AppButton
              label="Mark as Departed"
              onPress={handleDepart}
              loading={departing}
              style={styles.actionBtn}
            />
          )}
          {isDeparted && (
            <AppButton
              label="Mark as Arrived"
              onPress={handleArrive}
              loading={arriving}
              variant="secondary"
              style={styles.actionBtn}
            />
          )}
        </View>
      )}

      {isCompleted && (
        <View style={styles.footer}>
          <View style={styles.completedMsg}>
            <Ionicons name="checkmark-circle" size={22} color={COLORS.success} />
            <Text style={styles.completedText}>Trip completed successfully</Text>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
}

function JourneyRow({ icon, iconColor, label, value }) {
  return (
    <View style={styles.journeyRow}>
      <Ionicons name={icon} size={18} color={iconColor} />
      <View style={{ flex: 1, marginLeft: SPACING.sm }}>
        <Text style={styles.journeyLabel}>{label}</Text>
        <Text style={styles.journeyValue}>{value}</Text>
      </View>
    </View>
  );
}

function MetaRow({ label, value }) {
  return (
    <View style={styles.metaRow}>
      <Text style={styles.metaLabel}>{label}</Text>
      <Text style={styles.metaValue}>{value}</Text>
    </View>
  );
}

function TimelineStep({ done, active, label, icon, last }) {
  return (
    <View style={styles.timelineStep}>
      <View style={styles.timelineLeft}>
        <View style={[
          styles.timelineCircle,
          done  && styles.timelineDone,
          active && !done && styles.timelineActive,
        ]}>
          <Ionicons
            name={icon}
            size={14}
            color={done ? COLORS.white : active ? COLORS.primary : COLORS.textMuted}
          />
        </View>
        {!last && <View style={[styles.timelineLine, done && styles.timelineLineDone]} />}
      </View>
      <Text style={[styles.timelineLabel, done && styles.timelineLabelDone]}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe:   { flex: 1, backgroundColor: COLORS.background },
  topBar: {
    flexDirection:   'row',
    alignItems:      'center',
    justifyContent:  'space-between',
    backgroundColor: COLORS.primaryDark,
    paddingHorizontal: SPACING.md,
    paddingVertical:  SPACING.md,
  },
  topTitle: { fontSize: FONTS.sizes.lg, fontWeight: '700', color: COLORS.white },
  scroll:   { padding: SPACING.md },

  statusCard: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.lg,
    marginBottom:    SPACING.md,
    gap:             SPACING.sm,
    ...SHADOW.sm,
  },
  tripRoute: { fontSize: FONTS.sizes.xxl, fontWeight: '800', color: COLORS.textPrimary },
  tripSub:   { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },

  journeyCard: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.md,
    marginBottom:    SPACING.md,
    ...SHADOW.sm,
  },
  journeyRow:   { flexDirection: 'row', alignItems: 'center', paddingVertical: SPACING.xs },
  journeyLabel: { fontSize: FONTS.sizes.xs, color: COLORS.textMuted },
  journeyValue: { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.textPrimary },
  verticalLine: { width: 2, height: 16, backgroundColor: COLORS.border, marginLeft: 8, marginVertical: 2 },

  metaCard: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.md,
    marginBottom:    SPACING.md,
    ...SHADOW.sm,
  },
  metaRow:   { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: SPACING.xs, borderBottomWidth: 1, borderBottomColor: COLORS.divider },
  metaLabel: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
  metaValue: { fontSize: FONTS.sizes.sm, fontWeight: '700', color: COLORS.textPrimary },

  noteCard: {
    flexDirection:   'row',
    alignItems:      'flex-start',
    backgroundColor: '#EBF4FF',
    borderRadius:    RADIUS.md,
    padding:         SPACING.md,
    gap:             SPACING.sm,
    marginBottom:    SPACING.md,
  },
  noteText: { flex: 1, fontSize: FONTS.sizes.sm, color: COLORS.info, lineHeight: 20 },

  timeline: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.md,
    ...SHADOW.sm,
  },
  timelineStep:   { flexDirection: 'row', alignItems: 'flex-start', gap: SPACING.md },
  timelineLeft:   { alignItems: 'center' },
  timelineCircle: {
    width:  28, height: 28, borderRadius: 14,
    backgroundColor: COLORS.background,
    borderWidth: 2, borderColor: COLORS.border,
    alignItems: 'center', justifyContent: 'center',
  },
  timelineDone:   { backgroundColor: COLORS.accent, borderColor: COLORS.accent },
  timelineActive: { borderColor: COLORS.primary },
  timelineLine:   { width: 2, height: 28, backgroundColor: COLORS.border, marginTop: 2 },
  timelineLineDone: { backgroundColor: COLORS.accent },
  timelineLabel:     { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary, paddingTop: 4, fontWeight: '600' },
  timelineLabelDone: { color: COLORS.textPrimary },

  footer: {
    padding:         SPACING.md,
    backgroundColor: COLORS.white,
    borderTopWidth:  1,
    borderTopColor:  COLORS.divider,
  },
  actionBtn:     { width: '100%' },
  completedMsg:  { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: SPACING.sm, padding: SPACING.sm },
  completedText: { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.success },
});
