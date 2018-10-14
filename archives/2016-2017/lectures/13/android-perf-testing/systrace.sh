#!/bin/sh

${ANDROID_HOME}/platform-tools/systrace/systrace.py --time=10 -o trace.html gfx view res
