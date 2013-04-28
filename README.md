# akkdroid

Android-Scala-Akka project with separate client and server side - proof of concept.
Gradle buildfile is provided just for IntelliJ IDEA project files generation (it cannot perform proper build).

Project consists of three modules:
 * server - server-side app in Scala
 * client - client-side app in Scala for Android
 * core - common classes for client and server

## Setup instructions

1. Install [Android SDK](http://developer.android.com/sdk/index.html) (SDK Tools Only).
2. Run `tools/android` from newly unpacked Android SDK and install (at least) Android 2.3.3 (API 10).
3. Run `tools/android avd` and create Android 2.3.3 virtual machine.
4. Install [IntelliJ IDEA](http://www.jetbrains.com/idea/download/index.html)
5. Clone this repository and invoke `./gradlew idea` from inside. This will download all dependencies and generate IDEA project files.
6. Run IDEA and go to _Configure_ -> _Plugins_. Go to _Browse repositories..._, then find and install Scala plugin. Restart IDEA.
7. Open newly created project from IDEA (`akkdroid.ipr` file).
8. Configure Android SDK in IDEA. Go to _File_ -> _Project Structure..._ -> _SDKs_. Add new Android SDK. Provide path to your Android SDK installation, then choose Android 2.3.3.
9. Configure Android run configurations. Go to _Run_ -> _Edit configurations_. Add new _Android Application_ run configuration. Select `client` module and your previously created Android virtual machine as the target device.
