# UpDate

UpDate is an intelligent embedded system which utilizes phone activity and habits to display crucial information to your very own private group.

## No More Miscommuication

Currently, lack of communication in a household raises stress levels and may keep people on edge. As information becomes digital due to more social media presence, assumptions become more prevalent and verbal communication drops.

What if there was a way to track your habits and communicate your desires autonomously in real-time? Designed for families or college roommates, UpDate shares your activity or any important requests in real-time.

## Security is Extremely Important to us

UpDate utilized Firebase for Google Authentication and emulates a Pin/Fingerprint 2-Factor authentication if the user chooses for extra security.

You cannot join a group unless first invited via your email.

Most importantly, each user is able to set their personal security information:
 - Don't want your GPS location visible?  Disable GPS Tracking in-app
 - Don't want the app tracking phone-use? Disable background usage (future goal)

## Features

- Home Fragment: Dynamically restructures to display groups you are a part of or are invited to.
- Account Fragment: Allows the user to edit their email, alias, or picture as well as customize privacy settings.
- Maps Fragment: Dynamically displays location of permitting users in like groups.
- Groups Fragment: Information passed from home group thumbnails populates this view.

- Status Activity: Allows user to manually update their status and activity; updating group structures.
- Invite Activity: Allows user to invite a registered user to their group (invites pop up in real-time).
- Create Group Activity: Allows user to create a new group and stores in database with unique hash.
- Delete Group Activity: Allows owner of group to delete from database (deleted groups temporarily cached via firebase).

## Installation & Demo Instructions

Basic:
 1. Install the APK for the UpDate application (UpDate.apk), or alternatively, download the repository and emulate in Android Studio.
 2. Upon first opening the app, you will be prompted with a google authentication log in. Log in with an existing google account to proceed.
 3. Tap the create group button and give it any name (max limit 25 chars) and tap "Create". Your group will automatically be added to the home fragment.
 4. Tap on the newly created group to view the content - you will notice it only has your name as your status & activity are not set.
 5. Tap the red envelope to update your status. Fill in any text (up to 150 chars) and select a radio button for your activity and tap "Update Status" to record changes. (You may need to return to the home screen)
 6. The menu button is at the top left, tap it to open the menu drawer. Here you will find Home, Account, Map, & Logout Fragments.
 7. Tap the account fragment (you may be prompted to allow read permissions, accept if you intend on updating a picture). 
    NOTE: There are some rare occurances where certain devices ignore accepted permissions and throw security exceptions - we are working toward a universal fix for this issue.
 8. Update your name, email, and/or image and tap "Update Account" at the bottom to record changes.
 9. Tap the slider to enable background services (and accept any permission prompts).
 10. Tap the slider to set a pin if you wish, you will have to confirm it - then you can select how soon you will be required to reenter the pin after closing the app.
 11. Open the menu drawer and select the "Map" tab.
 12. Pins will display depending on whether a user has shared their location (and it is pinpoint accurate - updating every 10 minutes).
 13. Open the menu drawer and select the Home Tab.
 14. Tap the group you recently created and attempt to invite a user via email. You will find that the user needs to have registered with UpDate to be invited.
 15. Tap the "Delete Group" button to delete the group.
 16. Open the menu drawer and select the "Account tab"
 17. Tap the logout button to end the session.
 
To Test Inviting Members and Map Location:
 1. Have two or more devices with created groups.
 2. On one device, enter the group fragment and invite the other user via their registered email.
 3. Go to the Home Fragment on the second device and click the green checkmark in the group invitation.
 4. Enter the group and notice the invited user can't delete the group.
 5. Set a status on both devices and ensure that location services are on in the Account Fragment.
 6. Go into the map view and zoom in to see the markers associated with each group.
    NOTE: Emulated devices may not reflect your current GPS location & two nearby devices may produce markers that overlap.
