# webOS-Launcher
This is an attemp to create a launcher based on webOS. The shortcuts will launch your default applications.

## Known bugs
* Sometimes formatting is a bit off
* Placeholder image for dock background is currently being used until there is a suitable
  replacement

## Future Tasks

* Adding the Just Type... functionality
* App drawer separation of system and downloaded apps
* Settings in app drawer
* Allow dock to be customizable
* Do better on the dock layout

## Commit History

8/24/2022 My home button fix was not perfect. After testing on several different devices, sometimes it would launch Gallery app and have no way to go home on my Samsung test devices. LG devices were a mixed bag. Emulator worked just fine. This should be addressed. Some layout changes were also done so things are properly centered and aligned. Dock background has been changed, as well as app drawer page.

8/23/2022 Fixed a weird bug where launching phone when Samsung Level app is installed will launch
that instead of dialer. On press of home button now works to take user to home screen. Dock
background has been changed for debug purposes. This will be changed later.

8/19/2022 Some animations and layout issued have been adjusted. Dock icons now launch and close
appropriately. Some testing may need to be done on this still. Dock background may need to be
changed. App launcher will probably need to be its own activity moving forward to do some of the set
goals. App drawer is now the only issue.

8/13/2022 Added animations, fixed some formating issues with icons, sorting apps alphabetically

8/12/2022 Project pushed to Github and all functions work
