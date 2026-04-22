import React, { useState, useEffect } from 'react';
import {
  View, Text, ScrollView, StyleSheet, Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { passengerApi } from '../../api/passengerApi';
import AppButton from '../../components/common/AppButton';
import AppInput from '../../components/common/AppInput';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

export default function FareEstimateScreen({ route: navRoute, navigation }) {
  const { routeId, routeName, schedule } = navRoute.params;

  const [params,    setParams]    = useState(null);    // form structure from GET
  const [passengers,setPassengers]= useState('1');
  const [fareType,  setFareType]  = useState('STANDARD');
  const [estimate,  setEstimate]  = useState(null);
  const [loading,   setLoading]   = useState(true);
  const [calcLoading,setCalcLoading] = useState(false);

  useEffect(() => {
    passengerApi.getFareParams()
      .then((res) => setParams(res.data))
      .catch(() => setParams({}))
      .finally(() => setLoading(false));
  }, []);

  async function calculate() {
    const count = parseInt(passengers, 10);
    if (!count || count < 1) {
      Alert.alert('Invalid', 'Enter at least 1 passenger.');
      return;
    }
    setCalcLoading(true);
    setEstimate(null);
    try {
      const res = await passengerApi.calculateFare({
        routeId,
        scheduleId: schedule?.id ?? schedule?.scheduleId,
        passengers: count,
        fareType,
      });
      setEstimate(res.data);
    } catch {
      Alert.alert('Error', 'Could not calculate fare. Please try again.');
    } finally {
      setCalcLoading(false);
    }
  }

  if (loading) return <LoadingSpinner fullScreen message="Loading fare info…" />;

  const fareTypes = params?.fareTypes ?? ['STANDARD', 'STUDENT', 'SENIOR'];

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
        <Text style={styles.topTitle}>Fare Estimate</Text>
        <View style={{ width: 30 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scroll} showsVerticalScrollIndicator={false}>
        {/* Route summary */}
        <View style={styles.routeCard}>
          <Ionicons name="bus" size={20} color={COLORS.primary} />
          <View style={{ flex: 1, marginLeft: SPACING.sm }}>
            <Text style={styles.routeName}>{routeName}</Text>
            <Text style={styles.scheduleText}>
              {schedule?.departureTime ?? schedule?.startTime ?? 'Selected schedule'}
              {' · '}{schedule?.dayOfWeek ?? schedule?.days ?? 'Daily'}
            </Text>
          </View>
        </View>

        {/* Fare type selector */}
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>Passenger Type</Text>
          <View style={styles.fareTypes}>
            {fareTypes.map((ft) => (
              <FareTypeChip
                key={ft}
                label={ft.charAt(0) + ft.slice(1).toLowerCase()}
                selected={fareType === ft}
                onPress={() => setFareType(ft)}
              />
            ))}
          </View>
        </View>

        {/* Passenger count */}
        <View style={styles.section}>
          <AppInput
            label="Number of passengers"
            leftIcon="people-outline"
            keyboardType="number-pad"
            value={passengers}
            onChangeText={setPassengers}
            placeholder="1"
          />
        </View>

        <AppButton
          label="Calculate Fare"
          onPress={calculate}
          loading={calcLoading}
          style={styles.calcBtn}
        />

        {/* Estimate result */}
        {estimate && (
          <View style={styles.resultCard}>
            <Text style={styles.resultLabel}>Estimated Fare</Text>
            <Text style={styles.resultAmount}>
              GH₵ {estimate.totalFare ?? estimate.fare ?? estimate.amount ?? '—'}
            </Text>
            {estimate.perPassenger != null && (
              <Text style={styles.resultSub}>
                GH₵ {estimate.perPassenger} per passenger
              </Text>
            )}
            {estimate.currency && (
              <Text style={styles.resultCurrency}>{estimate.currency}</Text>
            )}
          </View>
        )}

        <View style={{ height: SPACING.xl }} />
      </ScrollView>

      {/* Footer CTA */}
      <View style={styles.footer}>
        <AppButton
          label="Book Ticket"
          disabled={!estimate}
          onPress={() =>
            navigation.navigate('TicketBooking', {
              routeId,
              routeName,
              schedule,
              fareEstimate: estimate,
              passengers:   parseInt(passengers, 10),
              fareType,
            })
          }
        />
      </View>
    </SafeAreaView>
  );
}

function FareTypeChip({ label, selected, onPress }) {
  return (
    <View
      style={[styles.chip, selected && styles.chipSelected]}
      accessibilityRole="radio"
      accessibilityState={{ checked: selected }}
    >
      <Text
        style={[styles.chipText, selected && styles.chipTextSelected]}
        onPress={onPress}
      >
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe:   { flex: 1, backgroundColor: COLORS.background },
  topBar: {
    flexDirection:   'row',
    alignItems:      'center',
    justifyContent:  'space-between',
    backgroundColor: COLORS.primary,
    paddingHorizontal: SPACING.md,
    paddingVertical:  SPACING.md,
  },
  topTitle: { fontSize: FONTS.sizes.lg, fontWeight: '700', color: COLORS.white },

  scroll: { padding: SPACING.md },

  routeCard: {
    flexDirection:   'row',
    alignItems:      'center',
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.md,
    padding:         SPACING.md,
    marginBottom:    SPACING.md,
    ...SHADOW.sm,
  },
  routeName:    { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.textPrimary },
  scheduleText: { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary, marginTop: 2 },

  section:      { marginBottom: SPACING.md },
  sectionLabel: { fontSize: FONTS.sizes.sm, fontWeight: '600', color: COLORS.textPrimary, marginBottom: SPACING.sm },

  fareTypes: { flexDirection: 'row', gap: SPACING.sm },
  chip: {
    paddingHorizontal: SPACING.md,
    paddingVertical:   SPACING.sm,
    borderRadius:      RADIUS.full,
    borderWidth:       1.5,
    borderColor:       COLORS.border,
    backgroundColor:   COLORS.white,
  },
  chipSelected:     { borderColor: COLORS.primary, backgroundColor: '#EBF4FF' },
  chipText:         { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary, fontWeight: '600' },
  chipTextSelected: { color: COLORS.primary },

  calcBtn: { marginBottom: SPACING.md },

  resultCard: {
    backgroundColor: COLORS.primary,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.lg,
    alignItems:      'center',
    ...SHADOW.md,
  },
  resultLabel:    { fontSize: FONTS.sizes.sm, color: 'rgba(255,255,255,0.8)', marginBottom: SPACING.sm },
  resultAmount:   { fontSize: 40, fontWeight: '800', color: COLORS.white },
  resultSub:      { fontSize: FONTS.sizes.sm, color: 'rgba(255,255,255,0.75)', marginTop: SPACING.xs },
  resultCurrency: { fontSize: FONTS.sizes.xs, color: 'rgba(255,255,255,0.6)', marginTop: 4 },

  footer: {
    padding:         SPACING.md,
    backgroundColor: COLORS.white,
    borderTopWidth:  1,
    borderTopColor:  COLORS.divider,
  },
});
