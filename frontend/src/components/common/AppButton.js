import React from 'react';
import {
  TouchableOpacity, Text, ActivityIndicator, StyleSheet, View,
} from 'react-native';
import { COLORS, FONTS, SPACING, RADIUS, SHADOW } from '../../constants';

/**
 * AppButton
 * @param {string}   variant   'primary' | 'secondary' | 'destructive' | 'ghost'
 * @param {string}   size      'sm' | 'md' | 'lg'
 * @param {boolean}  loading
 * @param {boolean}  disabled
 * @param {function} onPress
 * @param {string}   label
 * @param {node}     icon      Optional icon node placed before label
 */
export default function AppButton({
  variant  = 'primary',
  size     = 'md',
  loading  = false,
  disabled = false,
  onPress,
  label,
  icon,
  style,
  labelStyle,
}) {
  const isDisabled = disabled || loading;

  const containerStyle = [
    styles.base,
    styles[`size_${size}`],
    styles[`variant_${variant}`],
    isDisabled && styles.disabled,
    style,
  ];

  const textStyle = [
    styles.label,
    styles[`label_${size}`],
    styles[`labelColor_${variant}`],
    isDisabled && styles.labelDisabled,
    labelStyle,
  ];

  return (
    <TouchableOpacity
      style={containerStyle}
      onPress={onPress}
      disabled={isDisabled}
      activeOpacity={0.78}
      accessibilityRole="button"
      accessibilityLabel={label}
    >
      {loading ? (
        <ActivityIndicator
          size="small"
          color={variant === 'primary' || variant === 'destructive' ? COLORS.white : COLORS.primary}
        />
      ) : (
        <View style={styles.inner}>
          {icon && <View style={styles.iconWrap}>{icon}</View>}
          <Text style={textStyle}>{label}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  base: {
    borderRadius: RADIUS.md,
    alignItems:   'center',
    justifyContent: 'center',
    ...SHADOW.sm,
  },
  inner: {
    flexDirection: 'row',
    alignItems:    'center',
  },
  iconWrap: {
    marginRight: SPACING.sm,
  },

  // Sizes
  size_sm: { paddingVertical: SPACING.xs,  paddingHorizontal: SPACING.md,  minHeight: 36 },
  size_md: { paddingVertical: 13,          paddingHorizontal: SPACING.lg,  minHeight: 48 },
  size_lg: { paddingVertical: SPACING.md,  paddingHorizontal: SPACING.xl,  minHeight: 56 },

  // Variant backgrounds
  variant_primary:     { backgroundColor: COLORS.primary },
  variant_secondary:   { backgroundColor: COLORS.white, borderWidth: 1.5, borderColor: COLORS.primary },
  variant_destructive: { backgroundColor: COLORS.error },
  variant_ghost:       { backgroundColor: 'transparent', shadowOpacity: 0, elevation: 0 },

  // Labels
  label: { fontWeight: '700', letterSpacing: 0.3 },
  label_sm: { fontSize: FONTS.sizes.sm },
  label_md: { fontSize: FONTS.sizes.md },
  label_lg: { fontSize: FONTS.sizes.lg },

  labelColor_primary:     { color: COLORS.white },
  labelColor_secondary:   { color: COLORS.primary },
  labelColor_destructive: { color: COLORS.white },
  labelColor_ghost:       { color: COLORS.primary },

  disabled:      { opacity: 0.5 },
  labelDisabled: {},
});
