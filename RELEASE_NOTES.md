# Release Notes

## Version 3.5.0 — September 22, 2026

This release brings an authentic Palm webOS 2.2.4 heritage visual refresh (recreated directly from Palm Pre 2 system assets) combined with modern Android 15 & Material You dynamic system theming.

### Palm webOS 2.2.4 Heritage Visual Refresh
- **Open-Source Lato Typography**: Bundled the complete Google Fonts Lato family (under SIL OFL) across all screens, headers, app labels, tabs, cards, and notification panels.
- **Quick Launch Frosted Glass Dock**: Recreated the authentic webOS frosted glass shelf with a 1.5dp specular highlight line along the top edge, ambient edge stroke, and unconstrained 54dp adaptive floating icons with tactile spring press feedback.
- **"Just Type" Real-Time Search**: Added a webOS-style 42dp pill search container with instant real-time app filtering across all tabs as you type, and a dedicated tap-to-clear button.
- **Material 3 Tonal Pill Tabs**: Replaced plain text tabs with title-case Lato Bold labels and an active elastic pill indicator with generous touch padding.
- **Clean App Drawer Grid**: Removed nested layout overhead, eliminated forced artificial squircle boxes, and styled floating 56dp icons with soft text legibility shadows.

### 3-Way Theming System
- **Material You (Dynamic System)**: Dynamically samples wallpaper palette using Android 12+ Monet (`system_accent1_*` and `system_neutral1_*`) across the Quick Launch dock, header gradient, search container, active tab pill, and frosted drawer backdrop tint.
- **Classic (Light)**: Authentic Palm webOS blue header, frosted glass shelf, and high-contrast dark search text.
- **Obsidian (Dark)**: Deep onyx glass, gunmetal chrome header, and obsidian dock.
- Live real-time preview and switching in the About & Settings page.

### Notification Dashboard Overhaul
- **3D Card Stacking**: Visual stacked deck effect for multiple notifications from the same app with layered shadows.
- **Gesture Cycling**: Swipe up/down to cycle through stacked notifications without dismissing them.
- **Action Intent Pills**: Slide left to reveal interactive action pills directly on the notification card; slide right to dismiss.
### Bug Fixes & Resolved Issues
- **Fixed System Insets & Screen Scaling on Large Devices (Fixes #24)**: Improved window insets and layout scaling across modern displays (such as Pixel 9 Pro XL), ensuring the Quick Launch dock and notification panel respect gesture navigation pills and status bar cutouts without overlapping.
- **Persistent "Just Type" Search (Fixes #15)**: Replaced ephemeral widget logic with a native, persistent "Just Type" search container in the App Drawer header that never disappears across screen locks or app pauses.
- **Notification Edge Clipping**: Fixed horizontal clipping glitch affecting stacked notifications during cycle and dismiss transitions.

### F-Droid & IzzyOnDroid Reproducible Builds
- Implemented `compileSdk { version = release(37) { minorApiLevel = 0 } }` for compatibility with build environments (#21).
- Disabled `dependenciesInfo.includeInApk` to eliminate proprietary binary blobs and ensure 100% reproducible builds (#13).

## Version 3.0.0 — July 4, 2026

This is the biggest visual and functional update W Launcher has had since the original webOS-inspired look was introduced. It's a full pass at making the whole launcher — home screen, dock, and app drawer — actually feel like a classic Palm/HP webOS Mojo & Enyo device again, instead of just wearing a few webOS-flavored icons on top of a generic layout. Bumping to a major version because of how much of the UI changed and because it's now a real Home app for the first time.

### Light & Dark themes, done properly

- Replaced the old five-way theme picker (Classic/Classic3/Mochi/Modern/System) with a single Light/Dark choice. Old installs on "Classic3" quietly migrate to Dark so nobody gets bumped back to Light without asking.
- Light is no longer just Dark with different text colors — it's got its own blue identity across the whole app (headers, buttons, dock, icons), matching the classic webOS blue rather than reusing Dark's gunmetal gray in the wrong places.
- Fixed a bug where the About page's header and status bar ignored the theme entirely and always rendered in Dark's colors.
- Fixed Light theme text legibility — app-drawer labels get a subtle shadow, and the About & Settings page sits on a translucent scrim, so text stays readable against the wallpaper no matter how bright it is underneath.

### Apps Drawer redesign

- New tabbed header (System / Downloads / Settings) and squircle icon tiles, matching Mojo/Enyo's app-launcher chrome instead of the old flat list look.
- Added a genuine frosted-glass background in Light theme. It used to just lay a translucent wallpaper over whatever was still on screen behind it, which let the home screen's recent-app cards and dock visibly ghost through — jarring, not translucent. Now it blurs and tints that content instead, so it reads as intentional glass.
- Rounded the bottom corners of the drawer so it reads as a card sitting just above the dock, and made the status bar go transparent while it's open instead of tinting to the header color.
- Fixed a real bug where the drawer button could spawn an unlimited number of stacked drawer instances — it now properly toggles open and closed. Root cause was a leftover safety check that was silently auto-closing the drawer moments after it opened, essentially at random; that's gone now and the button just does what it looks like it should.

### Dock redesign

- Replaced the flat, theme-blind dock bar with a frosted-glass tray that matches the drawer — blue in Light, gunmetal in Dark — with rounded top corners and a soft shadow so it reads as a floating tray rather than a bar glued to the bottom of the screen.
- Dock icons now use the same frosted squircle tiles as the app drawer instead of a plain circular halo, so the two finally look like they belong to the same app.
- The dock now stays visible and fully usable while the app drawer is open on top of it, instead of sliding away — you can tap a dock app without closing the drawer first.

### New: long-press app actions

- Long-pressing an app icon in the drawer now brings up the same kind of "app actions" popup stock Android launchers show: the app's own shortcuts (e.g. an email app's "Compose"), Add to Dock, App Info, and Uninstall.
- Long-pressing a dock icon (without dragging it) shows the same popup, but with Remove from Dock in place of Add to Dock/Uninstall, since that's the action that actually makes sense there.
- Shortcuts only show up once W Launcher is actually set as your Home app — see below.

### About & Settings, combined

- About and Settings used to be two separate screens (About had a gear icon that drilled into Settings). They're now one page — About & Settings — themed to match, with the outdated About blurb rewritten to reflect what the launcher actually does today.

### Home screen polish

- Rounded the corners on the home screen / drawer / settings container for a more card-like feel, consistent with the rounded dock and drawer.
- Fixed the notification panel's dock-jumping and animation glitches.

### Fixed: W Launcher is now a proper Home app

- The manifest previously only declared the `LAUNCHER` category, not `HOME` — meaning it could never actually be selected as your device's default Home app, no matter how launcher-like it looked. That's fixed, so it can now be set as Home like it was always meant to be. This is also what unlocks the shortcuts in the new long-press menu, since Android only grants shortcut access to the current Home app.
