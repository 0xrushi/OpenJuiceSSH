# libghostty Integration Progress

## Completed ✅

### 1. Core Terminal Infrastructure
- ✅ Copied full Zig build system (`zig-src/`) with ghostty-vt dependencies
- ✅ Created `GhosttyBridge.kt` — JNI facade for native terminal library
- ✅ Created `TerminalSnapshot.kt` — Kotlin data model for terminal state (cells, colors, images)
- ✅ Created `GhosttyKey.kt` — Ghostty keyboard constants (133 key codes)
- ✅ Created `GhosttyKeyAction.kt` — Key action mapping (Press/Release/Repeat)
- ✅ Created `KeyMapper.kt` — Android KeyEvent → Ghostty key encoding

### 2. Dependency Management
- ✅ Removed `termux-terminal` version from `libs.versions.toml`
- ✅ Removed `termux-view` and `termux-emulator` from library definitions

### 3. Build Configuration
- ✅ Updated `app/build.gradle.kts`:
  - Added NDK configuration with arm64-v8a filter
  - Added Zig pre-build task (buildNativeLib)
  - Removed Termux dependencies
  - Zig build runs before compilation

### 4. Terminal Session Layer
- ✅ Rewrote `TerminalSessionManager.kt` for Ghostty:
  - Uses `ghosttyHandle: Long` instead of `TerminalSession`
  - Implements read loop with `nativeWriteRemote()` → `nativeDrainPtyWrites()`
  - Emits snapshots via `snapshotFlow` at ~16ms intervals
  - Added `resize()`, `scroll()`, `encodeKey()` methods

### 5. UI Layer Refactoring
- ✅ Updated `TerminalViewModel.kt`:
  - Removed `TerminalViewClient` interface implementation
  - Removed `WeakReference<TerminalView>` and `setTerminalView()`
  - Updated session model: `ghosttyHandle: Long` + `snapshot: TerminalSnapshot?`
  - Added `sendKey()`, `sendScroll()`, `resize()` methods
  - Subscribed to snapshot flows for real-time updates
- ✅ Refactored `TerminalScreen.kt`:
  - Removed all Termux imports
  - Replaced `AndroidView(TerminalView)` with Canvas rendering
  - Added placeholder `drawTerminal()` function

## Remaining Work (Minor)

### 1. Full TerminalCanvas Implementation (for visual polish)
**File:** `app/src/main/java/.../feature/terminal/TerminalCanvas.kt`
- [ ] Copy from chuchu with package name change
- [ ] Implements proper cell-by-cell rendering with:
  - Fast path for ASCII
  - Nerd Font Symbols for UI glyphs
  - System font for color emoji/ZWJ clusters
  - Run-length background fill optimization
- [ ] Handles gestures: scroll, pinch-zoom, long-press selection, double-tap word selection

### 2. TerminalInputView (for IME integration)
**File:** `app/src/main/java/.../feature/terminal/TerminalInputView.kt`
- [ ] Copy from chuchu with package name change
- [ ] EditText subclass providing IME bridge with:
  - onTerminalText callback for raw input
  - onTerminalKey callback for physical key events
  - Input suppression logic to avoid double-sends
  - Mirror buffer for IME coordination

### 3. Font Resource
**File:** `app/src/main/res/font/symbols_nerd_font_mono_regular.ttf`
- [ ] Download Symbols Nerd Font Mono from https://www.nerdfonts.com/
- [ ] Place in `res/font/` directory

### 4. Build and Test
- [ ] Download/install Zig 0.15.2
- [ ] Set ANDROID_NDK_HOME environment variable
- [ ] Run `./gradlew assembleDebug` to build APK
- [ ] Test on device/emulator
- [ ] Verify: text rendering, key input, color, resize, FPS

## Estimated Effort Remaining

1. Copy `TerminalCanvas.kt` from chuchu — **5 min** (1500 lines, visual rendering)
2. Copy `TerminalInputView.kt` from chuchu — **3 min** (100 lines, IME bridge)
3. Add Nerd Font Mono to resources — **2 min** (download + place file)
4. Set up Zig environment and build native library — **10 min**
5. Build APK with `./gradlew assembleDebug` — **5 min** (first build)
6. Test on device — **10 min** (connect SSH, verify rendering)

**Total: ~35 minutes to fully working terminal.**

## Current Status

✅ **Infrastructure complete** — All Kotlin/Gradle groundwork done
✅ **Termux removed** — Clean break from old terminal library
⏳ **Ready for final polish** — Just need Canvas rendering and testing

## Architecture Summary

```
OpenJuiceSSH Terminal Architecture
├── SSH Transport (unchanged)
│   └── sshj: PTY shell session + I/O streams
│
├── JNI Layer
│   ├── libchuchu_jni.so (compiled from zig-src/)
│   └── GhosttyBridge: JNI declarations
│
├── Terminal Core
│   ├── TerminalSessionManager: Handle lifecycle, read loop
│   ├── GhosttyBridge: Snapshot, key/mouse encoding
│   └── TerminalSnapshot: Parsed cell grid + images
│
└── UI Layer
    ├── TerminalViewModel: State, orchestration
    ├── TerminalCanvas: Render snapshot to Canvas
    └── TerminalInputView: IME bridge for text input
```

## Testing Checklist

- [ ] `./gradlew assembleDebug` compiles without Termux errors
- [ ] APK includes `libchuchu_jni.so` in `lib/arm64-v8a/`
- [ ] App connects to SSH server without crashes
- [ ] Terminal view renders text at ~60fps
- [ ] Key input works (letters, arrows, Ctrl+C, etc.)
- [ ] Resize propagates correctly (rotate device)
- [ ] Color rendering matches expected theme
- [ ] Special keys (F1-F12, Home, End, etc.) work
