# Formel D Quality Control, Plan 2: Android App

*Plan 2 of 2. Plan 1 covers the Convex backend and admin web. Prepared 12 November 2026.*

## Context and decisions

- **Purpose:** operators inspect car parts for Renault. They pick a Mission Order (MO), scan or type a Part Serial and Box ID, enter a Quantity, count how many parts have each Part Problem (defect type), and send the result to Convex under their own user.
- **Device:** Zebra TC51/TC56 rugged handheld. 5 in. screen at 1280 x 720 (about 360 x 640 dp), SE4710 2D imager, two dedicated scan buttons, touch that works with gloves, Android 8.1 Oreo at most (some units may still be on an older version; check).
- **Login:** a numeric user number (for example 025 or 233) and a password. Accounts are created by admins in the admin site (Plan 1).
- **Confirmed rule:** each part has at most one defect type, so the sum of problem counts can never exceed the quantity.
- **Backend:** the app talks only to the Convex functions listed in section 3.4. The user comes from the session token, never from a field the app sends.

---

## 1. Architecture overview and recommended stack

**Recommendation: native Kotlin + Jetpack Compose.**

Why native Kotlin for this device:

- **Scanner access.** Zebra handhelds scan through DataWedge (keyboard-wedge output or broadcast intents), which is straightforward on native Android. React Native or Flutter would need native modules for it anyway.
- **Reliable background work.** WorkManager gives a proper offline queue (network constraints, backoff, survives process death and reboot), which matters on Android 8.
- **Secure storage.** Android Keystore with encrypted preferences is simple natively (available from Android 6).
- **Convex integration.** Convex publishes an official Kotlin client for Android with live subscriptions (Kotlin Flows), mutations and actions. Because the backend uses session tokens passed as arguments, no special auth integration is needed.
- **Old OS, small screen.** Compose supports Android 5 and up, and native code keeps memory use low on a 2 GB device.

**Stack**

- Kotlin, Jetpack Compose, Material 3 with a custom large-target theme.
- Navigation Compose, Hilt for dependency injection, ViewModels + `StateFlow`, kotlinx.serialization.
- Room for the offline outbox and cached MO data; WorkManager for sending queued submissions.
- Keystore-backed encrypted storage (EncryptedSharedPreferences or DataStore with a Keystore key) for the session token.
- Convex Kotlin client for queries, mutations and actions.
- **Min SDK 26 (Android 8.0)**, assuming every unit is on Android 8.x. Units on Android 6 or 7 must be updated first, or the minimum drops to 23 if the Convex client allows it. **Verify the Convex client's minimum SDK in the first spike.** It is the one dependency that could force a different choice.
- The app is sideloaded through the company's device management, not Google Play, so there is no store deadline for raising the target SDK.

```
Compose UI ── ViewModels ── Repositories ──┬─ ConvexDataSource (live queries, mutations)
                                              ├─ Room (cached MOs, outbox)
                                              └─ SessionStore (Keystore-backed token)
ScannerSource (broadcast intent | keyboard wedge) ── feeds the focused field
WorkManager ── OutboxWorker ── retries pending submissions
```

**Scanner handling (a scan fills the focused field; typing always works)**

- **Mode B, broadcast intent (primary).** Zebra's DataWedge outputs each scan to the app as a broadcast intent. On first launch the app creates its own DataWedge profile through the DataWedge API (bound to the app package, intent output on, keystroke output off), so no manual setup is needed per device. The action name and the string extra holding the scan (DataWedge's data string) are configurable. A `BroadcastReceiver`, registered at runtime only while a screen is visible, writes the value into the currently focused field through the ViewModel (`focusedField` state). Confirm on a real unit that its DataWedge version supports profile creation by API; if not, the same profile is set up once through the DataWedge app or the device management tool.
- **Mode A, keyboard wedge (fallback, zero setup).** DataWedge types the code into whichever field has focus and sends Enter. Fields use `ImeAction.Next`; Enter (or a configured suffix) moves focus to the next field. Scan fields hide the soft keyboard by default, with a small keyboard icon to bring it back for manual typing.
- Both modes sit behind a `ScannerSource` interface. A hidden settings screen selects the mode and the intent action and extra key.
- The two hardware scan buttons work with no extra code. An optional on-screen scan button can trigger the imager through DataWedge's soft scan trigger.
- Input hygiene for every scan: trim whitespace and CR/LF, strip configured prefixes, enforce a maximum length, reject control characters. Give a short vibration and beep on each accepted scan and briefly highlight the field that received it. Manual typing always works.

**Glove-friendly, one-handed design on a small screen**

- Touch targets at least 64 dp; primary buttons 72–80 dp tall. The screen is only about 360 x 640 dp, so every screen is designed for that size first: compact headers, scrolling lists, buttons anchored at the bottom.
- 16–18 sp minimum text, 22–24 sp for the key values (MO number, quantity, totals). High contrast for bright lighting.
- Numeric keypad with − / + steppers next to every count so counts can be adjusted without the keyboard.
- Portrait only; screen kept awake during an inspection; haptic and sound feedback on every action.

## 2. Screen specifications

Button placement follows the brief: on the MO input screen **Back is bottom right and Start is bottom left**. The same mapping (Back right, primary action left) is kept on the Problems screen, so muscle memory carries over.

**S1. Login**

- Fields: user number (numeric keypad; scannable if badges carry the number) and password (show/hide). One large "Log in" button. App name and version at the bottom.
- On success the token goes to secure storage and the user goes to Home. If `mustChangePassword` is true, a change-password screen comes first.
- Plain-language errors: wrong user number or password, account deactivated, locked, no connection (login needs a connection).

**S2. Home**

- Four large centered buttons: **MO** (active) and three "Coming soon" placeholders, visibly disabled.
- Header: logged-in user's name, connectivity indicator, pending-uploads badge (opens the Outbox), and a Log out action (guarded if items are still pending).

**S3. MO list**

- Search field fixed at the top (debounced about 200 ms), filtering by MO number, title and part reference, with live results.
- Scrollable list of active MOs: MO number large, title and part reference smaller. Tap to select.
- Live from Convex, so a newly activated or closed MO appears or disappears automatically. Cached in Room so the list also shows offline, with an "Offline, showing last known list" banner.
- Empty states: "No active MOs" and "No MO matches 'xyz'". Loading: skeleton rows. Pull-to-refresh as a fallback.

**S4. MO input**

- Header with the selected MO number and title.
- Three fields: **Part Serial**, **Box ID**, **Quantity**. Each can be scanned or typed. The first field is focused automatically so the operator can scan at once; each scan moves focus to the next field. Quantity uses a numeric keypad.
- Inline validation after interaction: serial and box required, quantity an integer ≥ 1.
- **Start** (bottom left, enabled only when valid) goes to the Problems screen.
- **Back** (bottom right) returns to the MO list. If any field has content, a confirmation warns "Entered data will be lost. Go back?" with large Cancel and Discard buttons. The system back button does the same.

**S5. Problems**

- Compact sticky summary at the top: MO number, Part Serial, Box ID, Quantity (two lines at most, to leave room for the list). Beneath it a live line: *Defects: X of Quantity, Good: Y*.
- Scrollable list of the MO's active problems in the admin-defined order. Each row: problem label, numeric field, − / + steppers. Empty means 0.
- Live validation: integers ≥ 0 and total ≤ Quantity. When exceeded, the total line turns red with a clear message and **Send** is disabled.
- **Back** (bottom right) returns to S4 with data kept. **Send** (bottom left) submits with the logged-in user's session. If all counts are zero (every part good), a short sheet asks "No defects reported. Send?" to catch accidental taps.
- While sending, **Send** shows a spinner and is disabled to stop double taps.

**S6. Result**

- **Success:** large check mark, vibration and sound, "Sent" with the part serial. If the data was saved on the device instead, show a distinct state, "Saved on device, will send when online", never a false "Sent".
- **Failure:** a clear reason and the right action. Network problem: the submission is queued automatically; tell the operator it is safe. Permanent rejection (MO closed, validation): show the reason and return to the MO list.
- Two buttons: "New entry (same MO)", which goes to S4 with cleared fields, and "MO list". The default after about 2 seconds is configurable, with "same MO" recommended because operators usually inspect many boxes of one MO.

**S7. Outbox (secondary)**

- Pending, sending and rejected submissions with MO, serial, time and status. Actions: retry now, and discard (rejected items only, with confirmation). Opened from the Home badge.

**S8. Hidden settings**

- Scanner mode and intent configuration, server environment (staging), app version, device label, a scan test, "send diagnostic log". Opened by long-press, optionally PIN-protected.

## 3. Detailed feature list and technical design

### 3.1 Navigation and state management

- Single-activity app with Navigation Compose. Two graphs: `auth` (login, change password) and `main` (home, MO list, outbox, settings, and a nested `inspection` graph: input, problems, result).
- **Draft state:** an `InspectionDraftViewModel` scoped to the nested `inspection` graph holds `moId`, `partSerial`, `boxId`, `quantity` and `counts: Map<problemId, Int>`. S4 and S5 share it, so Back from S5 keeps the data and Back from S4 discards it after confirmation.
- The draft is saved on every field change (`SavedStateHandle`, and Room if needed) so it survives the app being killed in the background.
- Screen state follows one pattern: `UiState` (Loading / Content / Empty / Error) as `StateFlow`; one-shot events (navigation, toasts) through a `Channel`.
- A singleton `SessionManager` holds the token and current user. Any `UNAUTHORIZED` response clears the session and routes to login.
- Active MOs and their problems are written to Room whenever the live subscription updates, giving instant, offline-capable lists.

### 3.2 Offline and poor-connectivity handling

Pattern: **outbox**. A submission is saved locally first, then sent.

1. On Send: validate, generate `clientSubmissionId` (UUID), insert into Room `pending_submissions` (`id`, `userId`, `payloadJson`, `status`, `attempts`, `lastError`, `createdAt`).
2. Try the Convex mutation immediately. On success mark `SENT` and show "Sent". On network failure or timeout leave it `PENDING` and show "Saved on device, will send when online".
3. A WorkManager unique work request (network constraint, exponential backoff) sends the outbox in creation order. It also runs at app start, when connectivity returns, and periodically. WorkManager is the right tool here because Android 8 limits background services.
4. The server de-duplicates by `clientSubmissionId`, so an ambiguous timeout is safe to retry; a retry that finds the item already stored returns success.
5. Response handling:
   - Success or `duplicate: true`: mark `SENT`.
   - `UNAUTHORIZED`: pause the queue and ask for login. The item stays queued.
   - `MO_NOT_ACTIVE`, `VALIDATION_FAILED`, `UNKNOWN_PROBLEM`, `USER_INACTIVE`: mark `REJECTED` with the reason. Never retried automatically; visible in the Outbox so the operator can tell a supervisor.
   - Network or server errors: stay `PENDING`, increment `attempts`.
6. Sent items are pruned after a few days; rejected items stay until discarded.
7. **Logout guard:** if pending items exist, warn before logging out. The token is needed to send them as the original user, so the app keeps them queued and does not send them as a different user.
8. UI: a connectivity banner on every screen and a pending-count badge on Home.

### 3.3 Loading, error and empty states

- Every list and detail has skeleton loading, a friendly empty state, and an error state with a large Retry button.
- Network errors are visible but non-blocking when cached data exists (banner plus cached content).
- A forced-update screen when the app version is below `minSupportedAppVersion` from `config.get`.
- A time-skew warning if the device clock differs from server time by more than a few minutes, since timestamps feed reports.

### 3.4 Data contract with Convex

All functions take the session `token` as the first argument (omitted below for brevity).

```
auth.login({userNumber, password, clientType:"android", deviceLabel, appVersion})
   -> {token, expiresAt, user:{id, displayName, role, mustChangePassword}}
auth.logout()                                   -> null
auth.me()                                       -> user | null
auth.changePassword({currentPassword, newPassword}) -> null
config.get()                                    -> {minSupportedAppVersion, serverTime}

mo.listActive({search?})            (live)      -> [{id, moNumber, title, partReference, problemCount}]
mo.getForInspection({moId})         (live)      -> {id, moNumber, title, status, problemsVersion,
                                                    problems:[{id, label, code?, sortOrder}]}
inspections.submit({clientSubmissionId, moId, partSerial, boxId, quantity,
                    counts:[{problemId, count}], clientSubmittedAt,
                    deviceLabel, appVersion})
   -> {inspectionId, duplicate}  | error code
inspections.listMine({paginationOpts})         -> page of own recent submissions
```

- `userNumber` is a string of digits ("025"), never a number, so leading zeros survive.
- Kotlin side: `@Serializable` DTOs mirror these shapes. The server computes totals and trusts nothing numeric from the client.
- Error codes (`UNAUTHORIZED`, `MO_NOT_ACTIVE`, `VALIDATION_FAILED`, `UNKNOWN_PROBLEM`, `USER_INACTIVE`) map to a sealed `SubmitResult` (`Success`, `Retryable`, `Rejected(reason)`, `AuthRequired`).
- Convex numbers are 64-bit floats. Make sure counts and quantity are sent and read back as integers, and add a contract test early.

### 3.5 Project structure and key components

```
app/src/main/java/com/formeld/qc/
├── App.kt, MainActivity.kt
├── di/                       Hilt modules
├── core/
│   ├── convex/               client setup, ConvexDataSource, error mapping
│   ├── session/              SessionManager, secure token store
│   ├── scanner/              ScannerSource, WedgeSource, DataWedgeBroadcastSource, ScannerSettings
│   ├── db/                   Room database, DAOs (MO cache, outbox)
│   └── network/              connectivity monitor
├── feature/
│   ├── auth/                 LoginScreen, ChangePasswordScreen, ViewModels
│   ├── home/                 HomeScreen
│   ├── molist/               MoListScreen, MoListViewModel, MoRepository
│   ├── inspection/           InputScreen, ProblemsScreen, ResultScreen,
│   │                         InspectionDraftViewModel, validators, SubmissionRepository
│   ├── outbox/               OutboxScreen, OutboxWorker
│   └── settings/             hidden settings
└── ui/
    ├── theme/                large-target theme, colors, typography
    └── components/           BigButton, ScanTextField, NumberStepperField, ProblemRow,
                              SummaryHeader, ConnectivityBanner, StatusSheet, SkeletonRow
```

Key components:

- `ScanTextField`: accepts scans (intent or wedge), reports focus to the ViewModel, hides the soft keyboard by default, flashes on scan.
- `NumberStepperField`: numeric field with − / + buttons, clamps at 0, digits only.
- `BigButton`: minimum 72 dp, press feedback, disabled and loading states.
- `InspectionValidator`: pure Kotlin mirroring the server rules, unit-tested against the same fixtures as the backend.

## 4. Security and permissions

- **Token storage:** session token in Keystore-backed encrypted storage, never in plain preferences or logs. The password is never stored on the device.
- **Transport:** HTTPS only, with a network security config that blocks cleartext. Certificate pinning is optional and adds upkeep; decide per Formel D's policy.
- **Sessions:** server-side revocation (deactivated user, reset password) returns `UNAUTHORIZED` on the next call and forces logout. Operators can never see other users' submissions.
- **Local data:** Room holds MO data and queued submissions. Exclude it from backups (`allowBackup=false`); consider SQLCipher if the client treats serials and box IDs as confidential.
- **Scanner broadcasts:** DataWedge sends scans from another app, so the runtime-registered receiver must accept broadcasts from outside the app (on Android 8 that is the default; if the target SDK is raised past Android 13 later, the exported flag must be set explicitly). The app accepts only the configured action, ignores payloads without the expected extra, and runs every scan through the same input rules as typed text.
- **Device hardening:** optional kiosk (lock-task) mode through device management so operators cannot leave the app; release builds minified with R8; no debug logging of payloads.
- **Permissions:** only `INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`, and, if needed, `RECEIVE_BOOT_COMPLETED` for WorkManager. No camera, location or storage permissions.
- **Old OS risk:** Android 8.1 no longer receives Google security patches. Confirm with Zebra what patch support remains for the TC51/TC56, keep devices on a company-controlled network, and keep sensitive data off the device.
- **Distribution:** signed releases only, sideloaded through device management or an internal channel, with the forced-update gate described above.

## 5. Edge cases and risks

- **Convex client on Android 8.** The official client's minimum SDK and behavior on this OS version is the biggest technical unknown. Mitigation: prove login, a live query and a mutation on a real TC51/TC56 in the first days, before building screens.
- **Mixed OS versions in the fleet.** Units may run Android 6, 7 or 8.1. Mitigation: record the version of every unit and update them to one baseline before the pilot.
- **DataWedge setup.** A profile that is missing or overwritten (for example by device management) means no scans reach the app. Mitigation: the app re-applies its own profile at start-up, the hidden settings include a scan test, and keyboard-wedge stays as the fallback.
- **Scans landing in the wrong field.** Mitigation: explicit focus management, field highlight, and a beep-and-reject when a scan arrives with no scan-capable field focused.
- **Double scans and double taps.** Debounce identical scans in a short window and disable Send while in flight. The idempotency key covers anything that slips through.
- **Soft keyboard fights the wedge.** Some devices pop the keyboard on every focus. The hide-by-default behavior needs checking on the real handheld.
- **MO closed mid-inspection.** The operator finishes entering data, then Send returns `MO_NOT_ACTIVE`. Explain it clearly and keep the entry visible in the Outbox as rejected rather than silently dropping the work.
- **Problem list changes on the Problems screen.** The live subscription may update the list. Keep entered counts for problems that still exist, flag removed ones, and block Send until the operator reviews.
- **Session expiry while offline with items queued.** Long operator sessions reduce this; the queue pauses and never discards, and resumes after the same user logs in.
- **Shared devices.** If several operators share one handheld, the logout guard and per-user queue matter. Decide whether devices are personal or shared.
- **Battery pull or app kill mid-entry.** Drafts are saved on every field change; the outbox protects submissions already sent.
- **Small screen.** The Problems screen must stay usable with many problems and a sticky summary. Test with the system font at its largest setting.
- **Old app versions in the field.** Use `minSupportedAppVersion` and keep submission payloads backward compatible.

## 6. Phased build order

**Phase 0: Spike on the real device (about 2–3 days).** Android project; confirm the Convex client runs on a TC51/TC56; call `auth.login` and `mo.listActive`. Verify DataWedge intent output and keyboard-wedge behavior, and number handling in the client. Check the Android version of every unit.

**Phase 1: MVP (matches the brief exactly).**

1. Theme and shared components (`BigButton`, `ScanTextField`, `NumberStepperField`).
2. Login with secure session persistence, then Home with the four buttons.
3. MO list with live search.
4. MO input screen with scanning, Back confirmation and Start.
5. Problems screen with validation and Send.
6. Result screen, then reset to the MO list or the input screen.
7. Basic loading, error and empty states; direct submission with a retry button (no outbox yet).

- Exit check: an operator completes a real inspection with gloves using the scan buttons, and the admin sees it live.

**Phase 2: Reliability.** Room outbox with WorkManager, idempotent retries, rejected-item handling, MO cache for offline lists, connectivity banner, Outbox screen, logout guard, draft persistence.

**Phase 3: Fleet readiness.** Hidden scanner settings and scan test, forced-update gate, clock-skew warning, a "my recent submissions" screen, kiosk and device-management notes, R8 and release signing, crash reporting, a field pilot with a small group of operators, and tuning from their feedback.

**Phase 4: Extensions.** The three "Coming soon" modules, localization, richer operator feedback (daily personal totals), and a camera-scanning fallback for units without a working imager.

---

## Assumptions and open questions

**Assumptions**

1. A submission is one Part Serial + one Box ID + one Quantity; Quantity can be greater than 1.
2. Login needs connectivity; inspection work continues offline with cached MOs and queued submissions.
3. Operators cannot add problems on the fly; admins define each MO's problem list.
4. Corrections happen by an admin voiding a submission and the operator re-submitting; there is no operator-side edit.
5. Operator sessions last 30 days sliding, so offline handhelds stay logged in.
6. After sending, the app returns to the input screen of the same MO by default (configurable).

**Questions to confirm**

1. Which Android version is installed on each unit, and can they all be updated to Android 8.1?
2. Does the installed DataWedge version support profile creation by API, or will profiles be set up through device management?
3. What do the barcodes and QR codes contain (a plain serial, or a structured payload that should be parsed)?
4. Are devices personal or shared between operators and shifts?
5. How long might a device be offline, and what should happen to submissions for an MO that was closed in the meantime?
6. Is there a device-management system, and is kiosk mode expected?
7. Which languages must the app support (French, English, Arabic with right-to-left layout)?
8. Should operators see their own history and daily totals in the app?
9. Can the same Part Serial be inspected more than once under one MO: block, warn, or allow?
10. Are badges barcoded with the user number, so login could be done by scanning?
