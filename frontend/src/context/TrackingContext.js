import React, { createContext, useContext, useState, useRef, useCallback, useEffect } from 'react';
import { createStompClient, subscribeToLocations } from '../services/websocketService';

const TrackingContext = createContext(null);
const USE_MOCK_WS = false;

export function TrackingProvider({ children }) {
  // busLocations: Map keyed by busId → { busId, latitude, longitude, routeId, timestamp, ... }
  const [busLocations, setBusLocations] = useState({});
  const [wsConnected, setWsConnected]   = useState(false);
  const [wsError, setWsError]           = useState(null);

  const clientRef       = useRef(null);
  const subscriptionRef = useRef(null);
  const mockTimerRef    = useRef(null);

  // ── Mock Simulation (kept for explicit local demo toggles) ───────────────
  const startMockSimulation = useCallback(() => {
    setWsConnected(true);
    // Initial seed
    const initialMap = {};
    MOCK_BUS_LOCATIONS.forEach(b => initialMap[b.busId] = b);
    setBusLocations(initialMap);

    // Simulate movement
    mockTimerRef.current = setInterval(() => {
      setBusLocations(prev => {
        const next = { ...prev };
        Object.keys(next).forEach(id => {
          const bus = next[id];
          // Add a tiny random jitter
          next[id] = {
            ...bus,
            latitude: bus.latitude + (Math.random() - 0.5) * 0.001,
            longitude: bus.longitude + (Math.random() - 0.5) * 0.001,
            timestamp: new Date().toISOString()
          };
        });
        return next;
      });
    }, 3000);
  }, []);

  // ── Connect and subscribe ─────────────────────────────────────────────────
  const connect = useCallback(() => {
    if (USE_MOCK_WS) {
      startMockSimulation();
      return;
    }

    if (clientRef.current?.connected) return; // already connected

    const client = createStompClient({
      onConnect: () => {
        setWsConnected(true);
        setWsError(null);

        // Subscribe to location broadcasts
        subscriptionRef.current = subscribeToLocations(client, (payload) => {
          // Payload can be:
          // 1) a single location object
          // 2) an array of location objects
          // 3) { activeBuses: [...] } from backend realtime payload
          const source = Array.isArray(payload)
            ? payload
            : Array.isArray(payload?.activeBuses)
              ? payload.activeBuses
              : [payload];
          const locations = source
            .map((loc) => ({
              ...loc,
              busId: loc?.busId ?? loc?.id ?? loc?.registrationNumber,
              latitude: Number(loc?.latitude ?? loc?.lat),
              longitude: Number(loc?.longitude ?? loc?.lng),
            }))
            .filter(
              (loc) =>
                loc?.busId != null &&
                Number.isFinite(loc.latitude) &&
                Number.isFinite(loc.longitude),
            );
          setBusLocations((prev) => {
            const next = { ...prev };
            locations.forEach((loc) => {
              if (loc?.busId != null) {
                next[loc.busId] = loc;
              }
            });
            return next;
          });
        });
      },
      onDisconnect: () => {
        setWsConnected(false);
      },
      onError: (err) => {
        setWsError('Real-time connection lost. Retrying…');
        setWsConnected(false);
      },
    });

    clientRef.current = client;
    client.activate();
  }, [startMockSimulation]);

  // ── Disconnect cleanly ────────────────────────────────────────────────────
  const disconnect = useCallback(() => {
    if (USE_MOCK_WS) {
      clearInterval(mockTimerRef.current);
      setWsConnected(false);
      return;
    }
    subscriptionRef.current?.unsubscribe();
    subscriptionRef.current = null;
    clientRef.current?.deactivate();
    clientRef.current = null;
    setWsConnected(false);
  }, []);

  // ── Seed initial buses from REST response ─────────────────────────────────
  const seedBuses = useCallback((buses) => {
    if (!Array.isArray(buses)) return;
    const map = {};
    buses.forEach((b) => {
      const busId = b?.busId ?? b?.id ?? b?.registrationNumber;
      const latitude = Number(b?.latitude ?? b?.lat);
      const longitude = Number(b?.longitude ?? b?.lng);
      if (busId != null && Number.isFinite(latitude) && Number.isFinite(longitude)) {
        map[busId] = { ...b, busId, latitude, longitude };
      }
    });
    setBusLocations(map);
  }, []);

  const value = {
    busLocations,      // { [busId]: locationObject }
    wsConnected,
    wsError,
    connect,
    disconnect,
    seedBuses,
  };

  return (
    <TrackingContext.Provider value={value}>
      {children}
    </TrackingContext.Provider>
  );
}

export function useTracking() {
  const ctx = useContext(TrackingContext);
  if (!ctx) throw new Error('useTracking must be used inside TrackingProvider');
  return ctx;
}
