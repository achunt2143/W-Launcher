# webOS-Launcher
This is an attemp to create a launcher based on webOS. The shortcuts will launch the appropriate application. Just Type... will allow you to search installed apps, search on Maps, YouTube, send texts, start phone calls, and search the web. The apps drawer lists apps in alphabetical order.

## Screenshots
<p>
  <img src="https://user-images.githubusercontent.com/43080643/186994007-cc42cc0d-2f58-47e2-b8a9-d81b24b3389b.png" width="135" title="home">
  <img src="https://user-images.githubusercontent.com/43080643/187508433-09bd046a-03dd-409e-a4d8-a8e533cfcf78.png" width="135" alt="app drawer">
  <img src="https://user-images.githubusercontent.com/43080643/187508481-f3a4e462-caf3-49a8-b206-75cf1fad68e4.png" width="135" alt="settings drawer">
  <img src="https://user-images.githubusercontent.com/43080643/186994031-20d0477c-ed0b-4675-9cc1-90621ce31818.png" width="135" alt="just type">
  <img src="https://user-images.githubusercontent.com/43080643/186994042-08eb5344-1d29-48a7-b707-025096266fe6.png" width="135" alt="just type call">
  <img src="https://user-images.githubusercontent.com/43080643/186994046-d0c228f9-ae6b-4776-a167-9b850693b92c.png" width="135" alt="just type search">
</p>



## Known bugs
* On some phones the shortcuts in the dock may not launch the intended application, specifically LG devices may launch Assistant Voice Search with phone and messaging
* Status bar coloring is a little buggy
* Contacts permission must be granted manually or launching Just Type... will crash launcher

## Future Tasks

* ~~Adding the Just Type... functionality~~ Just Type... has been implemented as of 8/26/2022
* ~~App drawer separation of system and downloaded apps~~ Tab layout has been implemented as of
  8/29/2022
* ~~Settings in app drawer - Work in progress~~ Settings in app drawer as of 8/30/2022
* Allow dock to be customizable - likely not going to be possible for a long time
* ~~Do better on the dock layout~~

## Commit History

8/31/2022 Some layout changes have been made and there is an about/help page for launcher.

8/30/2022 Settings in app drawer are functional. Icons have webOS look but will launch the intended
settings.

8/29/2022 System and Downloaded apps are now separated. Due to how Android handles flags on apps, it
is not perfect, but does a good job. Settings tab is there, and does pull some settings for launch,
but it is not finished.

8/29/2022 Just Type... had some bugs when calling or sending a text to a contact. There was a null
array error that would cause the launcher to crash. This should be fixed. Some errors also occurred
when searching that would cause the launcher to crash. This should also be fixed. This is a bug fix
commit. No new features have come yet.

8/26/2022 Just Type... is here! See release notes for more information about how it works.
Screenshots have been uploaded now to show the launcher. Multiple pages has been tough to figure
out, but that is next on the to-do list.

8/24/2022 My home button fix was not perfect. After testing on several different devices, sometimes
it would launch Gallery app and have no way to go home on my Samsung test devices. LG devices were a
mixed bag. Emulator worked just fine. This should be addressed. Some layout changes were also done
so things are properly centered and aligned. Dock background has been changed, as well as app drawer
page.

8/23/2022 Fixed a weird bug where launching phone when Samsung Level app is installed will launch
that instead of dialer. On press of home button now works to take user to home screen. Dock
background has been changed for debug purposes. This will be changed later.

8/19/2022 Some animations and layout issued have been adjusted. Dock icons now launch and close
appropriately. Some testing may need to be done on this still. Dock background may need to be
changed. App launcher will probably need to be its own activity moving forward to do some of the set
goals. App drawer is now the only issue.

8/13/2022 Added animations, fixed some formating issues with icons, sorting apps alphabetically

8/12/2022 Project pushed to Github and all functions work
