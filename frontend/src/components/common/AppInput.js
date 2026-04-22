import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, FONTS, SPACING, RADIUS } from '../../constants';

export default function AppInput({
  label,
  error,
  secureTextEntry,
  leftIcon,
  style,
  inputStyle,
  ...props
}) {
  const [show, setShow] = useState(false);
  const isPassword = secureTextEntry;

  return (
    <View style={[styles.wrapper, style]}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <View style={[styles.container, error && styles.containerError]}>
        {leftIcon ? (
          <Ionicons
            name={leftIcon}
            size={18}
            color={COLORS.textSecondary}
            style={styles.leftIcon}
          />
        ) : null}
        <TextInput
          style={[styles.input, inputStyle]}
          secureTextEntry={isPassword && !show}
          placeholderTextColor={COLORS.textMuted}
          autoCapitalize="none"
          autoCorrect={false}
          accessibilityLabel={label}
          {...props}
        />
        {isPassword ? (
          <TouchableOpacity onPress={() => setShow(!show)} style={styles.toggle}>
            <Ionicons
              name={show ? 'eye-off-outline' : 'eye-outline'}
              size={20}
              color={COLORS.textSecondary}
            />
          </TouchableOpacity>
        ) : null}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: { marginBottom: SPACING.md },
  label: {
    fontSize:     FONTS.sizes.sm,
    fontWeight:   '600',
    color:        COLORS.textPrimary,
    marginBottom: SPACING.xs,
  },
  container: {
    flexDirection:   'row',
    alignItems:      'center',
    backgroundColor: COLORS.white,
    borderWidth:     1.5,
    borderColor:     COLORS.border,
    borderRadius:    RADIUS.md,
    paddingHorizontal: SPACING.md,
    minHeight:       48,
  },
  containerError: {
    borderColor: COLORS.error,
  },
  leftIcon: { marginRight: SPACING.sm },
  input: {
    flex:      1,
    fontSize:  FONTS.sizes.md,
    color:     COLORS.textPrimary,
    paddingVertical: SPACING.sm,
  },
  toggle: {
    padding: SPACING.xs,
  },
  error: {
    marginTop: 4,
    fontSize:  FONTS.sizes.xs,
    color:     COLORS.error,
  },
});
