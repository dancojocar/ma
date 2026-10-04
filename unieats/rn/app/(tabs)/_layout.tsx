import { Redirect, Tabs, useRouter } from "expo-router";
import { useEffect } from "react";
import { StyleSheet, Text } from "react-native";
import { LoadingView } from "../../src/components/StatusViews";
import { onNotificationTapped, requestNotificationPermission } from "../../src/notifications/spotNotifications";
import { useSessionStore } from "../../src/store/sessionStore";

function TabIcon({ symbol }: { symbol: string }) {
  return <Text style={styles.icon}>{symbol}</Text>;
}

export default function TabsLayout() {
  const router = useRouter();
  const status = useSessionStore((s) => s.status);

  useEffect(() => {
    if (status !== "signedIn") return;
    void requestNotificationPermission();
    return onNotificationTapped((id) => router.push({ pathname: "/spots/[id]", params: { id } }));
  }, [status, router]);

  if (status === "restoring") return <LoadingView />;
  if (status === "signedOut") return <Redirect href="/login" />;

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
        name="nearby"
        options={{
          title: "Nearby",
          tabBarIcon: ({ focused }) => <TabIcon symbol={focused ? "📍" : "🧭"} />,
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
