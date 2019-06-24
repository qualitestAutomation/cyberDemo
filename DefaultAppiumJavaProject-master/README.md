# DefaultAppiumJavaProject
This is the template for a single project for testing Appium and Selenium(hybrid testing) in Java

Before you start to use this template pleas do the following:

1. Node.js installed on the Computer.
2. Android SDK installed and configured for the appropriate OS versions.
3.Setting the PATH environment variable with ADB(Android).
4. Cloning project Appium-iOS-Inspector(iOS) for identifying iOS objects:
   https://github.com/mykola-mokhnach/Appium-iOS-Inspector.
5. Appium installed globally on the computer.
6. iOS special instructions:
   In order for Appium to work with an app there are some preconditions that must be met:
   First od all you need to work on an a Mac.
   Xcode must be installed on the Mac.
   The app must be signed and configured for testing.
   For iOS automation, Appium relies on system frameworks provided by Apple. For iOS 9.2 and below, Apple's only automation    technology was called UIAutomation, and it ran in the context of a process called "Instruments". As of iOS 10, Apple has    completely removed the UIAutomation instrument, thus making it impossible for Appium to allow testing in the way it used    to. Fortunately, Apple introduced a new automation technology, called XCUITest, beginning with iOS 9.3. For iOS 10 and      up, this will be the only supported automation framework from Apple.
   Appium has built in support for XCUITest beginning with Appium 1.6. For the most part in the beta version.

   For Appium to work properly some software should be installed prior to the installation.
   These are the initial setup:
   1. libimobiledevice - install using brew install libimobiledevice --HEAD
   2. ios-deploy - install using npm install -g ios-deploy
   3. Carthage - brew install carthage
   4. Appium must be installed(>=1.6.4 version in my case. globally).npm install -g appium
   5.In order to use XCUITest you need to do the following setup:
     Create a new project with a unique "Product Name" , "Organization Name",  and specify your "Team". Build the app on the      iPhone and get your new provisioning profile.Make sure the device trust you.
     Create a .xcconfig file somewhere on your file system and add the following to it:
     {
       DEVELOPMENT_TEAM = <Team ID>
       CODE_SIGN_IDENTITY = iPhone Developer
     }
     Use the path of this file on the Capability “xcodeConfigFile”.
     Use the value “XCUITest”  on the Capability “automationName”.

   If you're still having problems it means the basic automatic configuration is not enough then move to the full manuely      setup:
   Go to appium installation folder and from there:
   Cd appium/node_modules/appium-xcuitest-driver/WebDriverAgent
   Write in the terminal the command to fetch the dependencies:
	 Find the directory above “Scripts” directory and in there type
   /Scripts/bootstrap.sh -d and then /Scripts/bootstrap.sh
           If you have more than one xcode installed Change the Xcode Version to XCODE 8.3.2(for example) :
           sudo xcode-select --switch /path/to/Xcode.app/

  Open WebDriverAgent.xcodeproj in Xcode. For both the WebDriverAgentLib and WebDriverAgentRunner(Its on the same folder as   the previous step) select "Automatically manage signing" in the "General" tab, and change the bundle id for the target by   going into the "Build Settings" tab, from com.facebook.WebDriverAgentRunner to something that Xcode will accept.
  Try building them on the device(using menu Product-> Test).
  Run the test with the appropriate capabilities.


  1. Helpful links:
  2. https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md
  3. https://github.com/appium/appium-xcuitest-driver#development
    
How to work with this project:
There is an example test class: ExampleTest
 1. Working with Selenium:
   Under the DefaultTestClass class in the beforeMethod method you need to define the Driver mode to be "WEB"
   Hard coded: System.setProperty("Mode","WEB") or via tools such as Jenkins.The Automation will use Chrome browser.
   
 2.Working with Appium:
  a. Under the DefaultTestClass class in the beforeMethod method you need to define the Driver mode to be "ANDROID" or "IOS" and the wanted device udid(System.setProperty("udid","ad0617024472b30b01") or via tools such as Jenkins).
  b. Under main resources folder define the device to use on the devices.json:
     You must provide the appPackage and appActivity(and if needed appWaitActivity and appWaitPackage) for Android and bundleId under iOS for the AUT.
     You must provide the udid for both OS.
  
 
