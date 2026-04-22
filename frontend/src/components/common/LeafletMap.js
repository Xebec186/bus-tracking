import React, { useRef, useEffect, forwardRef, useImperativeHandle } from 'react';
import { StyleSheet, View, ActivityIndicator } from 'react-native';
import { WebView } from 'react-native-webview';
import { COLORS } from '../../constants';

/**
 * A 100% free mapping alternative using Leaflet.js and OpenStreetMap.
 * Bypasses Google/Apple Maps SDK requirements and API keys entirely.
 */
const LeafletMap = forwardRef(({ 
  initialRegion, 
  markers = [], 
  routeCoords = [], 
  style 
}, ref) => {
  const webViewRef = useRef(null);

  useImperativeHandle(ref, () => ({
    fitView: () => {
      if (webViewRef.current) {
        webViewRef.current.postMessage(JSON.stringify({ type: 'FIT_VIEW' }));
      }
    }
  }));

  // Generate the HTML for the Leaflet map
  const mapHtml = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
          body { margin: 0; padding: 0; }
          #map { height: 100vh; width: 100vw; background-color: #f0f0f0; }
          .bus-marker {
            background-color: ${COLORS.accent};
            border: 2px solid white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 2px 5px rgba(0,0,0,0.3);
            color: white;
            font-size: 14px;
          }
        </style>
      </head>
      <body>
        <div id="map"></div>
        <script>
          var map = L.map('map', { zoomControl: false }).setView([${initialRegion.latitude}, ${initialRegion.longitude}], 13);
          
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
          }).addTo(map);

          var markers = {};
          var polyline = null;

          function updateMarkers(busData) {
            // Remove old markers that aren't in the new data
            Object.keys(markers).forEach(id => {
              if (!busData.find(b => String(b.busId) === id)) {
                map.removeLayer(markers[id]);
                delete markers[id];
              }
            });

            // Add or update markers
            busData.forEach(bus => {
              var id = String(bus.busId);
              var pos = [bus.latitude, bus.longitude];
              
              if (markers[id]) {
                markers[id].setLatLng(pos);
              } else {
                var icon = L.divIcon({
                  className: 'bus-marker',
                  html: '<div style="transform: rotate(0deg);">🚌</div>',
                  iconSize: [32, 32],
                  iconAnchor: [16, 16]
                });
                markers[id] = L.marker(pos, { icon: icon })
                  .addTo(map)
                  .bindPopup('Bus ' + id);
              }
            });
          }

          function updateRoute(coords) {
            if (polyline) map.removeLayer(polyline);
            if (coords.length < 2) return;

            var latLngs = coords.map(c => [c.latitude, c.longitude]);
            polyline = L.polyline(latLngs, { color: '${COLORS.routeLine}', weight: 4 }).addTo(map);
          }

          function fitToAll() {
            var group = new L.featureGroup(Object.values(markers));
            if (polyline) group.addLayer(polyline);
            if (group.getLayers().length > 0) {
              map.fitBounds(group.getBounds(), { padding: [50, 50] });
            }
          }

          // Handle messages from React Native
          window.addEventListener('message', function(event) {
            var data = JSON.parse(event.data);
            if (data.type === 'UPDATE_MARKERS') updateMarkers(data.payload);
            if (data.type === 'UPDATE_ROUTE') updateRoute(data.payload);
            if (data.type === 'FIT_VIEW') fitToAll();
          });

          // Initial load
          updateMarkers(${JSON.stringify(markers)});
          updateRoute(${JSON.stringify(routeCoords)});
        </script>
      </body>
    </html>
  `;

  // Sync state changes to the WebView
  useEffect(() => {
    if (webViewRef.current) {
      webViewRef.current.postMessage(JSON.stringify({ type: 'UPDATE_MARKERS', payload: markers }));
    }
  }, [markers]);

  useEffect(() => {
    if (webViewRef.current) {
      webViewRef.current.postMessage(JSON.stringify({ type: 'UPDATE_ROUTE', payload: routeCoords }));
    }
  }, [routeCoords]);

  return (
    <View style={[styles.container, style]}>
      <WebView
        ref={webViewRef}
        originWhitelist={['*']}
        source={{ html: mapHtml }}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        style={styles.map}
        startInLoadingState={true}
        renderLoading={() => (
          <View style={styles.loader}>
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        )}
        onMessage={(event) => {
          // Add handling for interactions if needed
        }}
      />
    </View>
  );
});

const styles = StyleSheet.create({
  container: { flex: 1, overflow: 'hidden' },
  map: { flex: 1 },
  loader: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'white',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
});

export default LeafletMap;
