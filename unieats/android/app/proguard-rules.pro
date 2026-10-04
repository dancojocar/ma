# Empty on purpose: every library in the app ships its own consumer keep rules, and R8 reports no
# missing classes. Add a rule here only when `:app:assembleRelease` asks for one
# (build/outputs/mapping/release/missing_rules.txt) or the release build misbehaves at runtime.
