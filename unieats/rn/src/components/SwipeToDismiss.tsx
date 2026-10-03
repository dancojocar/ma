import type { ReactNode } from "react";
import { StyleSheet, Text, View, useWindowDimensions } from "react-native";
import { Gesture, GestureDetector } from "react-native-gesture-handler";
import Animated, { runOnJS, useAnimatedStyle, useSharedValue, withTiming } from "react-native-reanimated";

const DISMISS_FRACTION = 0.35;

export function SwipeToDismiss({ children, onDismiss }: { children: ReactNode; onDismiss: () => void }) {
  const { width } = useWindowDimensions();
  const translateX = useSharedValue(0);

  const pan = Gesture.Pan()
    // Horizontal intent only, so vertical drags still scroll the FlatList.
    .activeOffsetX([-15, 15])
    .failOffsetY([-10, 10])
    .onUpdate((e) => {
      translateX.value = e.translationX;
    })
    .onEnd((e) => {
      if (Math.abs(e.translationX) > width * DISMISS_FRACTION) {
        const target = Math.sign(e.translationX) * width;
        translateX.value = withTiming(target, { duration: 200 }, (finished) => {
          if (finished) runOnJS(onDismiss)();
        });
      } else {
        translateX.value = withTiming(0, { duration: 200 });
      }
    });

  const rowStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }],
    opacity: 1 - Math.min(Math.abs(translateX.value) / width, 0.6),
  }));

  return (
    <View>
      <View style={styles.behind}>
        <Text style={styles.behindText}>Hide</Text>
        <Text style={styles.behindText}>Hide</Text>
      </View>
      <GestureDetector gesture={pan}>
        <Animated.View style={rowStyle}>{children}</Animated.View>
      </GestureDetector>
    </View>
  );
}

const styles = StyleSheet.create({
  behind: {
    ...StyleSheet.absoluteFillObject,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 20,
    backgroundColor: "#fdecea",
    borderRadius: 12,
  },
  behindText: { color: "#c0392b", fontWeight: "600" },
});
