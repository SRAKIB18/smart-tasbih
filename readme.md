Act as a Senior Android Developer, UI/UX Designer, Software Architect, and Kotlin Expert.

Build a complete, professional, production-quality Android application named "Smart Tasbih – Digital Dhikr Counter".

IMPORTANT:
Do not create just a UI prototype. Build a fully functional Android application with working navigation, database, counter logic, settings, history, localization, and persistent storage.

==================================================

1. TECHNOLOGY STACK
==================================================

- Language: Kotlin
- IDE: Android Studio
- UI: Jetpack Compose
- Design: Material 3
- Architecture: MVVM + Clean Architecture
- Database: Room
- Settings: DataStore Preferences
- Background tasks: WorkManager
- Dependency Injection: Hilt
- Navigation: Navigation Compose
- Minimum SDK: Android 10 (API 29)
- Target SDK: Latest stable SDK
- Offline-first architecture.
- No login or internet connection required.
- Use modular, reusable, maintainable code.

==================================================
2. APP DESIGN AND UI/UX
==================================================

Create an elegant, premium Islamic-inspired interface.

Design direction:

- Minimalist and modern.
- Deep forest green, emerald, muted gold, and neutral surfaces.
- Beautiful typography.
- Rounded cards and subtle shadows.
- Smooth animations and transitions.
- Clean spacing and consistent iconography.
- Professional dashboard.
- Responsive layouts for different Android screen sizes.
- Follow Material 3 design guidelines.

THEMES:

1. Light Mode.
2. Dark Mode.
3. System Default Mode.

Allow users to choose themes from Settings.

DARK MODE:

- Deep dark background.
- Comfortable contrast.
- Readable Arabic text.
- Avoid excessive brightness.
- Properly styled buttons, cards, dialogs and navigation.

==================================================
3. LANGUAGE SUPPORT
==================================================

Support two complete application languages:

1. বাংলা (Bangla)
2. English

Requirements:

- Language selection in Settings.
- Translate all UI labels, buttons, menus, notifications, dialogs, errors and empty states.
- No mixed-language UI.
- Proper Bangla font rendering.
- RTL support for Arabic Dhikr text.
- Arabic text must remain correctly aligned in both languages.
- Language selection should persist after restarting the app.

Default language: Bangla, if appropriate for the device locale.

==================================================
4. HOME DASHBOARD
==================================================

Create a professional dashboard containing:

- App name and logo.
- Greeting section.
- Today's total Dhikr count.
- Total completed daily targets.
- Active Tasbih count.
- Overall daily progress.
- Quick access to recent Tasbih.
- Tasbih list.
- Add new Tasbih button.
- Search and filter options.

Each Tasbih card should show:

- Tasbih name.
- Arabic text.
- Bengali/English transliteration.
- Daily Target.
- Today's Daily Count.
- Previous Pending.
- Progress indicator.
- Completion status.

Allow multiple Tasbih entries without a hard-coded limit.

==================================================
5. MULTIPLE TASBIH MANAGEMENT
==================================================

Users can:

- Create unlimited Tasbih entries.
- Edit Tasbih.
- Delete Tasbih.
- Duplicate Tasbih.
- Archive Tasbih.
- Search Tasbih.
- Reorder Tasbih.
- Pin favorite Tasbih.

Every Tasbih must have a permanent unique ID.

Example:
TASBIH_001
TASBIH_002
TASBIH_003

Fields:

- Unique ID.
- Tasbih name.
- Arabic text.
- Transliteration.
- Translation/meaning.
- Daily Target.
- Previous Pending.
- Created date.
- Active/archive status.
- Optional notes.

Allow custom daily targets for each Tasbih.

==================================================
6. MOST IMPORTANT: SMART COUNTING ALGORITHM
==================================================

Implement the exact algorithm described below.

Each Tasbih has independent counting data.

Data fields:

- dailyTarget
- previousPending
- dailyCount
- previousCompletedToday
- lifetimeCount
- currentDate

CORE RULE:

FIRST: Complete Previous Pending.
SECOND: Add ALL remaining counts to Today's Daily Count.

IMPORTANT:
Daily Target is a goal, NOT a maximum limit.

Do not cap dailyCount at dailyTarget.

Example 1:

Daily Target = 100
Previous Pending = 50
Today's Reading = 100

Calculation:

- today count = 100
- previous pending = 50
- next day if reading 150 then next day count first = 100 then pending count = 50 other wise its continue

Example 2:

Daily Target = 100
Previous Pending = 0
Today's Reading = 150

Calculation:

- Daily Count = 150.
- Previous Pending remains 0.
- No Next Day Pending is created.
- All 150 counts belong to today's reading.

Example 3:

Daily Target = 100
Previous Pending = 50
Today's Reading = 200

Calculation:

- 50 completes Previous Pending.
- Remaining 150 goes to Daily Count.
- Daily Count = 150.
- Previous Pending = 0.
- Lifetime count increases by 200.
- No Extra Count.
- No Next Day Pending.

Example 4:

Daily Target = 100
Previous Pending = 50
Today's Reading = 120

Calculation:

- Previous Pending = 50-(120-100)=30.
- Daily Count = 100.

Example 5:

Daily Target = 100
Previous Pending = 50
Today's Reading = 30

Calculation:

- Previous Pending remaining = 50.
- Daily Count = 30.
- total pending remaining = 50+70 = 120
  
IMPORTANT DAILY RESET RULE:

At the start of a new local calendar day:

1. Calculate yesterday's unmet Daily Target.
2. If yesterday's dailyCount is less than dailyTarget, create pending equal to that difference.
3. Add that unmet amount to the existing pending, if any.
4. Never convert excess dailyCount into pending.
5. Reset dailyCount to 0.
6. Reset previousCompletedToday to 0.
7. Keep lifetimeCount unchanged.
8. Preserve each Tasbih's independent history.

Formula:

newPending = oldPendingRemaining + max(0, dailyTarget - yesterdayDailyCount)

However, avoid counting the same pending twice.

Important:
Previous Pending is consumed before Daily Count, so track previous completion separately and calculate the unmet daily target from dailyCount only.

Never allow one Tasbih's activity to affect another Tasbih's pending or history.

Use database transactions for counter updates and date rollover.

==================================================
7. COUNTER SCREEN
==================================================

Create a premium, distraction-free counter screen.

Display:

- Arabic Dhikr text.
- Transliteration.
- Meaning.
- Large central circular counter button.
- Today's Daily Count.
- Daily Target.
- Previous Pending.
- Previous Pending completion progress.
- Remaining Daily Target.
- Lifetime Count.

Counter controls:

- Tap to increment.
- Long press optional configurable behavior.
- Undo last count.
- Reset today's count with confirmation.
- Haptic feedback.
- Optional click sound.
- Full-screen counter mode.
- Keep screen awake option.
- Prevent accidental touches.

Animations:

- Smooth number transitions.
- Circular progress animation.
- Button press animation.
- Target completion animation.
- Optional subtle completion celebration.

Do not force a maximum counter value at Daily Target.

==================================================
8. DAILY HISTORY AND ANALYTICS
==================================================

Create a dedicated History section.

Features:

- Daily history.
- Weekly history.
- Monthly history.
- Yearly history.
- Calendar view.
- Tasbih-specific history.
- Lifetime statistics.

Show:

- Total daily readings.
- Previous pending completed.
- Daily target.
- Daily target completion percentage.
- Pending carried forward.
- Lifetime total.

Allow users to select a date and view individual Tasbih records.

Charts:

- Daily activity bar chart.
- Weekly progress.
- Monthly activity.
- Target completion statistics.

History must be immutable by default. If editing history is supported, clearly recalculate affected pending records and future days.

==================================================
9. SETTINGS
==================================================

Create a comprehensive Settings screen.

General:

- Language: Bangla / English.
- Theme: Light / Dark / System.
- Default daily target.
- First day of week.
- Date format.
- Number format.

Counter:

- Vibration on/off.
- Sound on/off.
- Keep screen awake.
- Button size.
- Confirmation before reset.
- Undo behavior.

Notifications:

- Daily reminder.
- Custom reminder time.
- Multiple reminder schedules.
- Enable/disable notifications.

Data:

- Backup.
- Restore.
- Export CSV.
- Export PDF.
- Clear history with confirmation.
- Reset application data with confirmation.

About:

- App version.
- Privacy policy.
- Developer information.
- Feedback option.

==================================================
10. NOTIFICATIONS AND REMINDERS
==================================================

Use WorkManager or suitable Android scheduling APIs.

Features:

- Daily Dhikr reminders.
- Custom reminder times.
- Optional incomplete target reminder.
- Notification permission handling.
- Respect Do Not Disturb where applicable.
- Avoid duplicate notifications.
- Correct local time handling.

==================================================
11. DATABASE DESIGN
==================================================

Use Room Database.

Suggested entities:

TasbihEntity:

- id: String (Primary Key)
- name: String
- arabicText: String
- transliteration: String
- meaning: String
- dailyTarget: Long
- previousPending: Long
- lifetimeCount: Long
- createdAt: Long
- isArchived: Boolean

DailyProgressEntity:

- id: Long (Primary Key)
- tasbihId: String (Foreign Key)
- date: String
- dailyCount: Long
- previousCompletedToday: Long
- pendingAtStart: Long
- pendingAtEnd: Long
- dailyTarget: Long

CountHistoryEntity:

- id: Long
- tasbihId: String
- date: String
- count: Long
- timestamp: Long

Use indexes and foreign keys.
Use Room transactions.
Use database migrations.
Do not lose data during application updates.

==================================================
12. DATA SAFETY
==================================================

- Save counter changes immediately.
- Preserve counts after app termination.
- Handle device restart.
- Handle date changes correctly.
- Avoid duplicate date records.
- Support database migration.
- Provide export and backup.
- Prevent negative counts.
- Handle very large counts safely.
- Use Long instead of Int for lifetime counts.
- Make Undo restore the exact previous state.
- Use atomic transactions for counter updates.

==================================================
13. ADDITIONAL ADVANCED FEATURES
==================================================

- Custom Tasbih categories.
- Favorite Tasbih.
- Personal notes.
- Multiple daily targets.
- Custom target per Tasbih.
- Searchable Tasbih library.
- Optional preset Dhikr collection.
- Tasbih completion badges.
- Daily streak tracking.
- Progress widgets.
- Quick settings shortcut if feasible.
- Backup and restore.
- Offline functionality.
- Accessibility support.
- TalkBack support.
- Dynamic font scaling.

==================================================
14. NAVIGATION
==================================================

Use a bottom navigation bar with:

1. Home
2. Tasbih
3. History
4. Statistics
5. Settings

Implement proper navigation and back handling.

==================================================
15. CODE QUALITY
==================================================

Requirements:

- No placeholder buttons.
- No fake functionality.
- No hardcoded demo-only behavior in production.
- No incomplete screens.
- No unnecessary internet permissions.
- No unnecessary third-party SDKs.
- Clean code structure.
- Reusable Compose components.
- StateFlow and ViewModel.
- Repository pattern.
- Proper error handling.
- Unit tests for the counting algorithm.
- UI tests for important flows.

==================================================
16. DEVELOPMENT WORKFLOW
==================================================

Build the application in stages.

Stage 1:
Project setup, Gradle dependencies, theme, colors, typography, navigation.

Stage 2:
Room database, entities, DAO, repository, ViewModels.

Stage 3:
Smart counting engine and automated tests.

Stage 4:
Home dashboard and Tasbih management.

Stage 5:
Counter screen and animations.

Stage 6:
History, calendar and analytics.

Stage 7:
Bangla/English localization and RTL support.

Stage 8:
Settings, notifications and reminders.

Stage 9:
Backup, restore, export and data safety.

Stage 10:
Testing, optimization and release build.

For each stage:

- Provide complete file paths.
- Provide complete Kotlin code.
- Include all required imports.
- Include Gradle dependencies.
- Explain where each file belongs.
- Ensure the project compiles.
- Fix errors before proceeding.
- Never omit code with phrases such as "same as above" or "implement similarly".

Start by presenting the complete project architecture and folder structure, then implement Stage 1.

FINAL GOAL:

Deliver a complete, beautiful, premium-quality, offline-first Android Tasbih application with accurate independent multi-Tasbih counting, Bangla and English language support, dark mode, persistent history, and the exact smart pending algorithm described above.
