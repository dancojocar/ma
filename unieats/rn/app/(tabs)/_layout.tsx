import { Tabs } from "expo-router";
import { StyleSheet, Text } from "react-native";

function TabIcon({ symbol }: { symbol: string }) {
  return <Text style={styles.icon}>{symbol}</Text>;
}

export default function TabsLayout() {
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
    </Tabs>
  );
}

const styles = StyleSheet.create({
  icon: { fontSize: 20 },
});
