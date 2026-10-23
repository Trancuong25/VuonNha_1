# AGENTS.md

Android (Kotlin/Compose) app + local Node.js backend for the "Vườn Nhà" plant-care app. This file covers workspace-level gotchas. Deeper rules and architecture live in `VuonNha_1/AGENTS.md`, `VuonNha_1/README.md`, `VuonNha_1/docs/ARCHITECTURE.md`, and `VuonNha_1/backend/README.md` — read those before changing behavior.

## Layout / git

- The opencode workspace root (`D:\Hoc_Tap\VuonNha_1`) is **not** the Android project. Run all Gradle/backend commands from `VuonNha_1/` (use the `workdir` param; there is no Gradle wrapper at the root).
- Two nested git repos exist. `VuonNha_1/.git` holds the real history; the outer repo tracks the whole folder as plain files. **Commit project changes from `VuonNha_1/`** and inspect `git -C VuonNha_1 status` / `git -C VuonNha_1 diff` before committing.
- Root-level `*.log` and `anr-*.txt` are debug artifacts, already gitignored. Never commit `.env`, `VuonNha_1/backend/data/`, `local.properties`, `app/build/`, `.idea/`, `.kotlin/`, or `node_modules/`.
- `VuonNha_1/AGENTS.md` currently says Room is v4; the code is actually **v5** (`data/AppDb.kt`, `MIGRATION_4_5` adds `Item.profileJson`). Any schema change needs a new migration + test.

## Commands (workdir `VuonNha_1`)

```powershell
# JVM unit tests, debug APK, androidTest APK, lint
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug

# Backend tests (built-in node test runner, no npm install / no node_modules needed)
cd backend; node --test --test-isolation=none
```

- Single JVM test: `.\gradlew.bat testDebugUnitTest --tests com.example.vuonnha.domain.ScheduleTest`
- Single instrumented test (device/emulator + both APKs installed): `adb -s SERIAL shell am instrument -w -e class com.example.vuonnha.GardenNavigationTest com.example.vuonnha.test/androidx.test.runner.AndroidJUnitRunner`
- There is no CI config in this repo; run the commands locally.

## Setup gotchas

- Android: minSdk 26, compile/target SDK 37, AGP 9.3.3, Kotlin 2.2.10, Room 2.8.4, KSP. Gradle daemon toolchain is JDK 25 (declared via foojay resolver); configuration cache is on.
- Backend: Node.js 24+, ESM, `127.0.0.1:8787` only. No npm dependencies — never add one without cause. Start with `backend/run-backend.cmd` (loads `.env` and runs `connect-usb.ps1` for `adb reverse`), or `node --env-file=.env server.mjs` from `backend/`.
- `GEMINI_API_KEY` and `SCAN_TOKEN` live only in `backend/.env` (gitignored; copy `.env.example`). The Android debug build's `SCAN_TOKEN` is a local dev token hard-coded in `app/build.gradle.kts` (see that file for the value), **not** the Gemini key. Never put a Gemini key in the APK or docs.
- Release builds require `SCAN_ENDPOINT` to be empty or `https://` (enforced in `app/build.gradle.kts`).
- Do not run a second backend while port 8787 is in use; `/health` only proves env vars exist, not that the Gemini key/quota works.
- When testing on emulator/device, re-run `backend/connect-usb.ps1` (or `adb reverse tcp:8787 tcp:8787`) after replugging/restarting.

## Testing quirks

- `AccountFlowTest` / `AccountBackupTest` hit an isolated backend on port **8788** and are gated: `AccountFlowTest` runs only with `-e accountAudit true` (and as guest in an isolated emulator). Never point them at the user's real backend/data.
- `LiveRecognitionAuditTest`, `NotificationDeliveryAuditTest`, `GardenFlowAuditTest` are opt-in integration/audit tests that call real AI or need time; keep them out of normal/CI runs. `LiveRecognitionAuditTest` can consume Gemini quota.
- Do not delete or rewrite existing passing tests; add new ones.

## Style / documentation

- Match existing conventions; do not add comments unless asked.
- Update docs in place rather than creating new Markdown files: `README.md` (overview/status), `backend/README.md` (API/ops), `VuonNha_1/docs/ARCHITECTURE.md` (technical), `VuonNha_1/docs/HANDOFF.md` (handoff state), `VuonNha_1/AGENTS.md` (team rules).
- Do not run `git clean`, `git reset --hard`, or delete `backend/data/`, diary photos, AVDs, or `.audit/`.

## Workflow

- Read `VuonNha_1/docs/HANDOFF.md` before starting any task.
- Do only the task asked, in small steps.
- Never read, print or copy `backend/.env`, `local.properties`, or any key/token.
- After each task, update `VuonNha_1/docs/HANDOFF.md` with what was done, files changed, commands run and results, uncertainties, and a suggested next step.
- Do not commit.
