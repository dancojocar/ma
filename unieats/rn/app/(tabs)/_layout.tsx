import { Tabs } from "expo-router";
import { StyleSheet, Text } from "react-native";
import { useSessionStore } from "../../src/store/sessionStore";

function TabIcon({ symbol }: { symbol: string }) {
  return (
    <Text
      style={styles.icon}
      accessible={false}
      accessibilityElementsHidden
      importantForAccessibility="no-hide-descendants"
    >
      {symbol}
    </Text>
  );
}

export default function TabsLayout() {
  const status = useSessionStore((s) => s.status);
  // The splash screen is still up; don't open the list or its WebSocket before the session is known.
  if (status === "restoring") return null;

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: "#e87c2a",
        tabBarInactiveTintColor: "#999",
        headerStyle: { backgroundColor: "#e87c2a" },
        headerTintColor: "#fff",
        headerTitleStyle: { fontWeight: "700" },
      }}
    >
      <Tabs.Screen
        name="spots"
        options={{
          title: "Spots",
          headerShown: false,
          tabBarIcon: ({ focused }) => <TabIcon symbol={focused ? "🍽️" : "🍴"} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "Profile",
          tabBarIcon: ({ focused }) => <TabIcon symbol={focused ? "👤" : "👥"} />,
        }}
      />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  icon: { fontSize: 20 },
});
