import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import DriverDashboardScreen from '../screens/driver/DriverDashboardScreen';
import ActiveTripScreen      from '../screens/driver/ActiveTripScreen';
import TicketValidationScreen from '../screens/driver/TicketValidationScreen';

const Stack = createNativeStackNavigator();

export default function DriverStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="DriverDashboard" component={DriverDashboardScreen} />
      <Stack.Screen name="ActiveTrip"      component={ActiveTripScreen} />
      <Stack.Screen name="TicketValidation" component={TicketValidationScreen} />
    </Stack.Navigator>
  );
}
