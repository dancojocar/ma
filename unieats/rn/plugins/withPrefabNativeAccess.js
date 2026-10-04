// AGP 8.12 (React Native 0.86) runs the Prefab CLI on the Gradle JDK and fails the native build on any
// stderr line; on JDK 24+ the CLI's JNA load prints a restricted-method warning. AGP 9.1+ passes this flag
// itself (React Native 0.87), so delete this plugin once the app is on an SDK that ships RN 0.87.
const fs = require("fs");
const path = require("path");
const { withDangerousMod } = require("expo/config-plugins");

const FLAG = "--enable-native-access=ALL-UNNAMED";

const SH = `export JAVA_TOOL_OPTIONS="${FLAG}\${JAVA_TOOL_OPTIONS:+ \$JAVA_TOOL_OPTIONS}"`;
const BAT = `set "JAVA_TOOL_OPTIONS=${FLAG} %JAVA_TOOL_OPTIONS%"`;

function insertAfter(file, anchor, line) {
  if (!fs.existsSync(file)) return;
  const text = fs.readFileSync(file, "utf8");
  if (text.includes(FLAG)) return;
  const eol = text.includes("\r\n") ? "\r\n" : "\n";
  const lines = text.split(eol);
  const at = lines.findIndex((l) => anchor.test(l));
  lines.splice(at + 1, 0, line);
  fs.writeFileSync(file, lines.join(eol));
}

module.exports = function withPrefabNativeAccess(config) {
  return withDangerousMod(config, [
    "android",
    (cfg) => {
      const root = cfg.modRequest.platformProjectRoot;
      insertAfter(path.join(root, "gradlew"), /^#!/, SH);
      insertAfter(path.join(root, "gradlew.bat"), /setlocal/i, BAT);
      return cfg;
    },
  ]);
};
