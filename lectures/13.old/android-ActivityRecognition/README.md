Detecting User Activity changes using Activity Recognition Transition API
=========================================================================
The codelab can be found [here](https://codelabs.developers.google.com/codelabs/activity-recognition-transition/index.html?index=..%2F..index#6).

We carry phones with us everywhere, but until now, it's been hard for apps to adjust their
experience to a user's continually changing environment and activity.

To do this in the past, developers spent valuable engineering time combining various signals
(location,sensor, etc.) to determine when an activity like walking or driving had started or ended.
Even worse, when apps are independently and continuously checking for changes in user activity,
battery life suffers.

The Activity Recognition Transition API solves these problems by providing a simple API that does
all the processing for you and just tells you what you actually care about: when a user's activity
has changed. Your app simply subscribes to a transition in activities you are  interested in and the
API notifies you of the changes

As an example, a messaging app can ask, "tell me when the user has entered or exited a vehicle", to
set the user's status as busy. Similarly, a parking detection app can ask,"tell me when the user has
exited a vehicle and started walking", to save the user's parking location.

In this codelab, you will learn how to use the Activity Recognition Transition API to determine when
a user starts/stop an activity like walking or running.


Pre-requisites
--------------
Android API Level > v14
Android Build Tools > v21
Google Support Repository

Android Studio 3.5 or later to run the code
A device/emulator running on Oreo or later (this codelab targets Android 10)

Familiarity with Android development and some familiarity with callbacks.


Getting Started
---------------
Visit the [codelab here](https://github.com/googlecodelabs/activity_transitionapi-codelab). Feel
free to look at the complete module to see how the final code works.


Support
-------

Patches are encouraged, and may be submitted by forking this project and submitting a pull request
through GitHub.
