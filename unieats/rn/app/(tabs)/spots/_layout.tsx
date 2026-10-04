import { Stack } from "expo-router";

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
    </Stack>
  );
}
