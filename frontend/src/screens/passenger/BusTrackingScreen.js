import React, { useEffect, useRef, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import LeafletMap from '../../components/common/LeafletMap';
import { useTracking } from '../../context/TrackingContext';
import { trackingApi } from '../../api/trackingApi';
import { WsBanner } from '../../components/common/EmptyState';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

// Greater Accra default centre
const ACCRA_REGION = {
  latitude:       5.5800,
  longitude:     -0.2100,
  latitudeDelta:  0.15,
  longitudeDelta: 0.15,
};

export default function BusTrackingScreen({ route: navRoute, navigation }) {
  const insets = useSafeAreaInsets();
  const routeId = navRoute?.params?.routeId ?? null;
  const { busLocations, wsConnected, wsError, connect, disconnect, seedBuses } = useTracking();

  const mapRef      = useRef(null);
  const [routeCoords, setRouteCoords] = useState([]);

  // ── Connect WebSocket and seed map ────────────────────────────────────────
  useEffect(() => {
    let mounted = true;

    async function init() {
      // 1. Initial REST load
      try {
        const res = await trackingApi.getBuses();
        const buses = res.data?.content ?? res.data ?? [];
        if (mounted) seedBuses(buses);
      } catch (err) {
        console.warn("[Tracking] Failed initial bus load", err);
      }

      // 2. Load route geometry if a routeId was passed
      if (routeId) {
        try {
          const res = await trackingApi.getRoutePath(routeId);
          const coords = res.data?.coordinates ?? [];
          if (mounted) {
            setRouteCoords(coords.map((c) => ({
              latitude:  Number(c.latitude),
              longitude: Number(c.longitude),
            })));
          }
        } catch (err) {
          console.warn("[Tracking] Failed route path load", err);
        }
      }

      // 3. Start STOMP connection
      if (mounted) connect();
    }

    init();

    return () => {
      mounted = false;
      disconnect();
    };
  }, [routeId, connect, disconnect, seedBuses]);

  const buses = Object.values(busLocations);

  // Filter buses if looking at a specific route
  const filteredBuses = routeId 
    ? buses.filter(b => b.routeId === routeId || String(b.routeId) === String(routeId))
    : buses;

  // Fit map to visible buses and route
  function fitMap() {
    if (mapRef.current) {
      mapRef.current.fitView();
    }
  }

  return (
    <View style={styles.safe}>
      {/* Header */}
      <SafeAreaView edges={['top']} style={styles.topBar}>
        <TouchableOpacity
          onPress={() => navigation.goBack()}
          style={styles.backBtn}
          accessibilityLabel="Go back"
        >
          <Ionicons name="arrow-back" size={22} color={COLORS.white} />
        </TouchableOpacity>
        <View style={{ flex: 1, marginLeft: SPACING.sm }}>
          <Text style={styles.topTitle}>Live Tracking</Text>
          <Text style={{ fontSize: 10, color: 'rgba(255,255,255,0.7)' }}>
            {routeId ? `Viewing Route #${routeId}` : 'All Active Buses'}
          </Text>
        </View>
        <View style={styles.statusDot}>
          <View style={[styles.dot, { backgroundColor: wsConnected ? COLORS.accent : COLORS.warning }]} />
          <Text style={styles.statusText}>{wsConnected ? 'Live' : 'Offline'}</Text>
        </View>
      </SafeAreaView>

      {/* WS error banner */}
      <WsBanner message={wsError} />

      {/* Map */}
      <LeafletMap
        ref={mapRef}
        style={styles.map}
        initialRegion={ACCRA_REGION}
        markers={filteredBuses}
        routeCoords={routeCoords}
      />

      {/* Bus count overlay - using insets for bottom positioning */}
      <View style={[styles.overlay, { bottom: Math.max(insets.bottom, SPACING.md) + SPACING.md }]}>
        <View style={styles.countCard}>
          <Ionicons name="bus" size={16} color={COLORS.primary} />
          <Text style={styles.countText}>
            {filteredBuses.length} active bus{filteredBuses.length !== 1 ? 'es' : ''}
          </Text>
        </View>
        <TouchableOpacity style={styles.fitBtn} onPress={fitMap} accessibilityLabel="Fit map to buses">
          <Ionicons name="expand-outline" size={20} color={COLORS.primary} />
        </TouchableOpacity>
      </View>
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
  backBtn:    { padding: SPACING.xs },
  topTitle:   { fontSize: FONTS.sizes.lg, fontWeight: '700', color: COLORS.white, flex: 1, marginLeft: SPACING.sm },
  statusDot:  { flexDirection: 'row', alignItems: 'center', gap: 5 },
  dot:        { width: 8, height: 8, borderRadius: 4 },
  statusText: { fontSize: FONTS.sizes.xs, color: COLORS.white },

  map: { flex: 1 },

  overlay: {
    position:        'absolute',
    left:            SPACING.md,
    right:           SPACING.md,
    flexDirection:   'row',
    alignItems:      'center',
    justifyContent:  'space-between',
  },
  countCard: {
    flexDirection:   'row',
    alignItems:      'center',
    backgroundColor: COLORS.white,
    borderRadius:    RADIUS.full,
    paddingHorizontal: SPACING.md,
    paddingVertical:   SPACING.sm,
    gap:             SPACING.sm,
    ...SHADOW.md,
  },
  countText: { fontSize: FONTS.sizes.sm, fontWeight: '700', color: COLORS.textPrimary },
  fitBtn: {
    width:           44,
    height:          44,
    borderRadius:    22,
    backgroundColor: COLORS.white,
    alignItems:      'center',
    justifyContent:  'center',
    ...SHADOW.md,
  },
});
