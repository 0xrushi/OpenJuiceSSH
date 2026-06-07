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

## Remaining Work (Must Be Done)

### Phase 1: Build Configuration (High Priority)
**File:** `app/build.gradle.kts`
- [ ] Remove `implementation(libs.termux.view)` and `implementation(libs.termux.emulator)`
- [ ] Remove JitPack repository from `settings.gradle.kts` (if not needed elsewhere)
- [ ] Add NDK configuration:
  ```kotlin
  ndkVersion = "27.2.12479018"
  defaultConfig { ndk { abiFilters.add("arm64-v8a") } }
  ```
- [ ] Add Zig pre-build task before `android {}`:
  ```kotlin
  tasks.register<Exec>("buildNativeLib") {
      workingDir = rootProject.file("zig-src")
      commandLine("zig", "build", "-Doptimize=ReleaseSmall", "jni")
  }
  tasks.matching { it.name.startsWith("compile") || it.name == "preBuild" }.configureEach {
      dependsOn("buildNativeLib")
  }
  ```

### Phase 2: TerminalSessionManager Rewrite (Critical)
**File:** `app/src/main/java/.../core/data/ssh/TerminalSessionManager.kt`

Replace Termux-based session management with libghostty:
- [ ] Change `TerminalSessionData` to use `ghosttyHandle: Long` instead of `terminalSession: TerminalSession`
- [ ] Add `snapshotFlow: MutableStateFlow<TerminalSnapshot?>` to each session
- [ ] Rewrite `openSession()`:
  - Create Ghostty handle: `bridge.nativeCreate(80, 24, 1000)`
  - Launch read loop: SSH output → `nativeWriteRemote()` → `nativeDrainPtyWrites()` → session output
  - Throttle snapshots to ~16ms
- [ ] Add `resize(serverId, sessionId, cols, rows, cellW, cellH)` calling `nativeResize()` and PTY resize
- [ ] Remove all `TerminalSessionClient` interface methods

### Phase 3: UI Layer Refactoring (High Priority)
**File:** `app/src/main/java/.../feature/terminal/TerminalViewModel.kt`
- [ ] Remove `TerminalViewClient` interface implementation
- [ ] Remove `WeakReference<TerminalView>` and `setTerminalView()` method
- [ ] Change `TerminalSessionData` model to use `ghosttyHandle: Long` and `snapshot: TerminalSnapshot?`
- [ ] Add methods:
  - `sendKey(sessionId, key, cp, mods, action)` → `nativeEncodeKey()` → `sendInput()`
  - `sendScroll(sessionId, delta, x, y)` → `nativeScroll()`
  - `resize(sessionId, cols, rows, cellW, cellH)` → `terminalSessionManager.resize(...)`
- [ ] Subscribe to snapshot flows and update state
- [ ] Remove `onKeyDown()` and `onCodePoint()` overrides

**File:** `app/src/main/java/.../feature/terminal/TerminalScreen.kt`
- [ ] Copy `TerminalCanvas.kt` and `TerminalInputView.kt` from chuchu (with package changes)
- [ ] Replace `AndroidView(TerminalView)` with:
  - `TerminalCanvas` for rendering snapshots
  - `TerminalInputView` overlay for IME input
- [ ] Remove `applyTerminalTheme()` function
- [ ] Remove `import com.termux.*` statements

### Phase 4: Font Resource
**File:** `app/src/main/res/font/symbols_nerd_font_mono_regular.ttf`
- [ ] Download Symbols Nerd Font Mono from https://www.nerdfonts.com/
- [ ] Place in `res/font/` directory

## Next Steps to Execute

1. **Complete build.gradle.kts modifications** (10 min)
2. **Rewrite TerminalSessionManager.kt** (30 min)
   - Replace Termux session lifecycle with Ghostty handle management
   - Implement read loop with native calls and snapshot emission
3. **Update TerminalViewModel.kt** (20 min)
   - Remove Termux interfaces
   - Add Ghostty key/scroll encoding
4. **Copy TerminalCanvas.kt and TerminalInputView.kt from chuchu** (5 min)
5. **Update TerminalScreen.kt** (15 min)
   - Replace terminal view with Canvas + InputView
6. **Download and add Nerd Font** (2 min)
7. **Initial build test** — verify no compilation errors
8. **Runtime testing** — connect to SSH server and verify terminal renders

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
