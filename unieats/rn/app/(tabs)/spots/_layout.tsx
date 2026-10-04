import { Stack } from "expo-router";

export const unstable_settings = {
  // A cold-start deep link to /spots/<id> still gets the list underneath, so Back works.
  initialRouteName: "index",
};

export default function SpotsStackLayout() {
  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: "#e87c2a" },
        headerTintColor: "#fff",
        headerTitleStyle: { fontWeight: "700" },
      }}
    >
      <Stack.Screen name="index" options={{ title: "UniEats" }} />
      <Stack.Screen name="[id]" options={{ title: "Spot" }} />
    </Stack>
  );
}
