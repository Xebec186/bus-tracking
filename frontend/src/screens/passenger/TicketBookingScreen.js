import React, { useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { passengerApi } from '../../api/passengerApi';
import AppButton from '../../components/common/AppButton';
import AppInput from '../../components/common/AppInput';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

export default function TicketBookingScreen({ route: navRoute, navigation }) {
  const { routeId, routeName, schedule, fareEstimate, passengers, fareType } = navRoute.params;

  const [cardNumber, setCardNumber] = useState('');
  const [expiry,     setExpiry]     = useState('');
  const [cvv,        setCvv]        = useState('');
  const [cardName,   setCardName]   = useState('');
  const [loading,    setLoading]    = useState(false);
  const [errors,     setErrors]     = useState({});

  function formatCard(text) {
    const digits = text.replace(/\D/g, '').slice(0, 16);
    return digits.replace(/(.{4})/g, '$1 ').trim();
  }

  function formatExpiry(text) {
    const digits = text.replace(/\D/g, '').slice(0, 4);
    if (digits.length >= 3) return `${digits.slice(0,2)}/${digits.slice(2)}`;
    return digits;
  }

  function validate() {
    const e = {};
    const rawCard = cardNumber.replace(/\s/g, '');
    if (rawCard.length !== 16)  e.cardNumber = 'Enter a valid 16-digit card number';
    if (!/^\d{2}\/\d{2}$/.test(expiry)) e.expiry = 'Use MM/YY format';
    if (cvv.length < 3)         e.cvv        = 'Enter a valid CVV';
    if (!cardName.trim())       e.cardName   = 'Cardholder name is required';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handlePay() {
    if (!validate()) return;
    setLoading(true);
    try {
      // Step 1: book
      const bookRes = await passengerApi.bookTicket({
        routeId,
        scheduleId:  schedule?.id ?? schedule?.scheduleId,
        passengers,
        fareType,
      });
      const ticketId = bookRes.data?.id ?? bookRes.data?.ticketId;

      // Step 2: pay
      await passengerApi.payTicket(ticketId, {
        paymentMethod: 'CARD',
        cardLast4:     cardNumber.replace(/\s/g, '').slice(-4),
        amount:        fareEstimate?.totalFare ?? fareEstimate?.fare,
      });

      // Navigate to confirmation
      navigation.replace('TicketDetail', {
        ticketId,
        fromBooking: true,
      });
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Payment failed. Please try again.';
      Alert.alert('Payment Error', msg);
    } finally {
      setLoading(false);
    }
  }

  const total = fareEstimate?.totalFare ?? fareEstimate?.fare ?? '—';

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
        <Text style={styles.topTitle}>Book Ticket</Text>
        <View style={{ width: 22 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scroll} showsVerticalScrollIndicator={false}>

        {/* Order summary */}
        <View style={styles.summaryCard}>
          <Text style={styles.summaryTitle}>Order Summary</Text>
          <Row label="Route"      value={routeName} />
          <Row label="Schedule"   value={`${schedule?.departureTime ?? '—'} · ${schedule?.dayOfWeek ?? 'Daily'}`} />
          <Row label="Passengers" value={`${passengers}`} />
          <Row label="Fare type"  value={fareType?.charAt(0) + fareType?.slice(1).toLowerCase()} />
          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>Total</Text>
            <Text style={styles.totalAmount}>GH₵ {total}</Text>
          </View>
        </View>

        {/* Payment form */}
        <View style={styles.payCard}>
          <View style={styles.payHeader}>
            <Ionicons name="card-outline" size={20} color={COLORS.primary} />
            <Text style={styles.payTitle}>Payment Details</Text>
            <View style={styles.simBadge}>
              <Text style={styles.simText}>Simulated</Text>
            </View>
          </View>

          <AppInput
            label="Card number"
            leftIcon="card-outline"
            placeholder="0000 0000 0000 0000"
            keyboardType="number-pad"
            value={cardNumber}
            onChangeText={(t) => setCardNumber(formatCard(t))}
            error={errors.cardNumber}
            maxLength={19}
          />

          <View style={styles.row}>
            <View style={{ flex: 1 }}>
              <AppInput
                label="Expiry"
                placeholder="MM/YY"
                keyboardType="number-pad"
                value={expiry}
                onChangeText={(t) => setExpiry(formatExpiry(t))}
                error={errors.expiry}
                maxLength={5}
              />
            </View>
            <View style={{ width: SPACING.md }} />
            <View style={{ flex: 1 }}>
              <AppInput
                label="CVV"
                placeholder="123"
                keyboardType="number-pad"
                secureTextEntry
                value={cvv}
                onChangeText={(t) => setCvv(t.replace(/\D/g, '').slice(0,4))}
                error={errors.cvv}
                maxLength={4}
              />
            </View>
          </View>

          <AppInput
            label="Cardholder name"
            leftIcon="person-outline"
            placeholder="Ama Owusu"
            autoCapitalize="words"
            value={cardName}
            onChangeText={setCardName}
            error={errors.cardName}
          />
        </View>

        <View style={{ height: SPACING.xl }} />
      </ScrollView>

      {/* Pay button */}
      <View style={styles.footer}>
        <AppButton
          label={`Pay GH₵ ${total}`}
          onPress={handlePay}
          loading={loading}
        />
      </View>
    </SafeAreaView>
  );
}

function Row({ label, value }) {
  return (
    <View style={styles.summaryRow}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={styles.rowValue}>{value}</Text>
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
  scroll:   { padding: SPACING.md },

  summaryCard: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.md,
    marginBottom:    SPACING.md,
    ...SHADOW.sm,
  },
  summaryTitle: { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.textPrimary, marginBottom: SPACING.sm },
  summaryRow:   { flexDirection: 'row', justifyContent: 'space-between', marginBottom: SPACING.xs },
  rowLabel:     { fontSize: FONTS.sizes.sm, color: COLORS.textSecondary },
  rowValue:     { fontSize: FONTS.sizes.sm, color: COLORS.textPrimary, fontWeight: '600', maxWidth: '60%', textAlign: 'right' },
  totalRow:     { flexDirection: 'row', justifyContent: 'space-between', marginTop: SPACING.sm, paddingTop: SPACING.sm, borderTopWidth: 1, borderTopColor: COLORS.divider },
  totalLabel:   { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.textPrimary },
  totalAmount:  { fontSize: FONTS.sizes.lg, fontWeight: '800', color: COLORS.primary },

  payCard: {
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.lg,
    padding:         SPACING.md,
    ...SHADOW.sm,
  },
  payHeader: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm, marginBottom: SPACING.md },
  payTitle:  { fontSize: FONTS.sizes.md, fontWeight: '700', color: COLORS.textPrimary, flex: 1 },
  simBadge:  { backgroundColor: '#FFF7E6', paddingHorizontal: SPACING.sm, paddingVertical: 2, borderRadius: RADIUS.full },
  simText:   { fontSize: FONTS.sizes.xs, color: '#A65C00', fontWeight: '700' },

  row: { flexDirection: 'row' },

  footer: { padding: SPACING.md, backgroundColor: COLORS.white, borderTopWidth: 1, borderTopColor: COLORS.divider },
});
