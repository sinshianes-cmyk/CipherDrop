#!/bin/sh
# Reproduces the "part of another page shows below the current one" race on a plain JVM.
# Usage: KOTLIN_HOME=/path/to/kotlinc ./run.sh <path-to-PageViewModel-dir> <path-to-PageState.kt-dir>
# Runs the scenario against whatever PageViewModel.kt you point it at (e.g. the original vs the fixed one).
set -e
HERE=$(cd "$(dirname "$0")" && pwd)
VM_DIR=${1:?dir containing PageViewModel.kt and PageState.kt}
K=${KOTLIN_HOME:?set KOTLIN_HOME to a Kotlin compiler distribution}
OUT=$(mktemp -d)
mkdir -p "$OUT/src"
cp "$VM_DIR/PageState.kt" "$OUT/src/"
# plain JVM has no Android main looper: swap in a single-thread dispatcher
sed 's/Dispatchers.Main.immediate/harness.MainThread.dispatcher/g' "$VM_DIR/PageViewModel.kt" > "$OUT/src/PageViewModel.kt"
CP="$K/lib/kotlinx-coroutines-core-jvm.jar:$K/lib/kotlin-stdlib.jar"
"$K/bin/kotlinc" -nowarn -cp "$CP" "$HERE"/stubs/*.kt "$OUT"/src/*.kt "$HERE/Test.kt" -d "$OUT/out"
java -cp "$OUT/out:$CP" TestKt
