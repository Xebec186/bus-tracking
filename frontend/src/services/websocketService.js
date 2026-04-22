import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE_URL } from '../constants';

const RECONNECT_DELAY = 5000;

/**
 * Creates and returns a configured STOMP client.
 * Caller is responsible for calling client.activate() and client.deactivate().
 *
 * @param {object} options
 * @param {function} options.onConnect       - Called when STOMP session is established
 * @param {function} options.onDisconnect    - Called on clean disconnect
 * @param {function} options.onError         - Called on broker errors
 */
export function createStompClient({ onConnect, onDisconnect, onError } = {}) {
  const client = new Client({
    // SockJS factory — required for React Native where native WebSocket
    // may not support STOMP framing on all backends
    webSocketFactory: () => new SockJS(WS_BASE_URL),
    reconnectDelay: RECONNECT_DELAY,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: (frame) => {
      if (onConnect) onConnect(frame);
    },
    onDisconnect: (frame) => {
      if (onDisconnect) onDisconnect(frame);
    },
    onStompError: (frame) => {
      console.warn('[WS] STOMP error:', frame.headers?.message);
      if (onError) onError(frame);
    },
    onWebSocketError: (event) => {
      console.warn('[WS] WebSocket error:', event);
      if (onError) onError(event);
    },
  });

  return client;
}

/**
 * Subscribe to the bus location broadcast topic.
 * Returns the STOMP subscription object (call .unsubscribe() to clean up).
 *
 * @param {Client}   client    - Active STOMP client
 * @param {function} callback  - Receives parsed location payload
 */
export function subscribeToLocations(client, callback) {
  if (!client || !client.connected) {
    console.warn('[WS] Cannot subscribe — client not connected');
    return null;
  }

  return client.subscribe('/topic/tracking/locations', (message) => {
    try {
      const payload = JSON.parse(message.body);
      callback(payload);
    } catch (err) {
      console.warn('[WS] Failed to parse location message:', err);
    }
  });
}
