import React from 'react';
import { View, ActivityIndicator, Text, StyleSheet } from 'react-native';
import { COLORS, SPACING, RADIUS, FONTS } from '../../constants';

export function LoadingSpinner({ message = 'Loading…', fullScreen = false }) {
  if (fullScreen) {
    return (
      <View style={styles.fullScreen}>
        <ActivityIndicator size="large" color={COLORS.primary} />
        {message ? <Text style={styles.message}>{message}</Text> : null}
      </View>
    );
  }
  return (
    <View style={styles.inline}>
      <ActivityIndicator size="small" color={COLORS.primary} />
      {message ? <Text style={styles.inlineMsg}>{message}</Text> : null}
    </View>
  );
}

export function SkeletonCard({ height = 80, style }) {
  return <View style={[styles.skeleton, { height }, style]} />;
}

export function SkeletonList({ count = 4 }) {
  return (
    <View style={{ padding: SPACING.md }}>
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonCard key={i} style={{ marginBottom: SPACING.sm }} />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  fullScreen: {
    flex: 1,
    alignItems:     'center',
    justifyContent: 'center',
    backgroundColor: COLORS.background,
  },
  message: {
    marginTop:  SPACING.md,
    fontSize:   FONTS.sizes.md,
    color:      COLORS.textSecondary,
  },
  inline: {
    flexDirection: 'row',
    alignItems:    'center',
    justifyContent:'center',
    padding:        SPACING.md,
  },
  inlineMsg: {
    marginLeft: SPACING.sm,
    fontSize:   FONTS.sizes.sm,
    color:      COLORS.textSecondary,
  },
  skeleton: {
    backgroundColor: '#E8EDF2',
    borderRadius:    RADIUS.md,
    overflow:        'hidden',
  },
});
