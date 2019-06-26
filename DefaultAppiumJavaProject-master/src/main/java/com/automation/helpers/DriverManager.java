package com.automation.helpers;


import com.google.common.base.Predicate;
import com.relevantcodes.extentreports.LogStatus;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automation.helpers.ReportManager.lock;
import static io.appium.java_client.service.local.flags.GeneralServerFlag.SESSION_OVERRIDE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by mkalash on 2/7/17.
 */
public class DriverManager {

    public boolean isMobile;
    private DesiredCapabilities capabilities;
    protected WebDriver driver = null;
    public WebDriver driver1 = null;
    public WebDriver driver2 = null;
    public WebDriverWait wait = null;
    public ReportManager htmlReporter = new ReportManager();
    private AppiumDriverLocalService s;
    public DriverMode mode;
    private JSONObject currentDeviceInfo = null;
    public Logger logger = Logger.getLogger(this.getClass());
    public final int SEARCHBUTTON = 66;

    public WebDriver getCurrentDriver(String name) {
        if (name.equals("driver"))
            return driver;
        else if(name.equals("driver1"))
                return driver1;
        else return driver2;
    }

    public WebDriver getCurrentDriver() {
            return driver;
    }

    public void setCurrentDriver(DriverManager manager) {
        driver = driver;
    }

    public void closeTestReport() {
        htmlReporter.closeTestReport();
    }

    public void startReportFile(String phoneDir, String suiteDir) {
        htmlReporter.startReportFile(phoneDir, suiteDir);
    }

    public void saveScreenshot(LogStatus logStatus, String stepName, String details) {
        String context = "";
        if (isMobile) {
            context = ((AppiumDriver) driver).getContext();
            switchContext("native");
        }
        htmlReporter.saveScreenshot(driver, logStatus, stepName, details);
        if (isMobile) {
            ((AppiumDriver) driver).context(context);
        }
    }

    public void saveScreenshot(WebDriver appiumDriver, LogStatus logStatus, String stepName, String details) {
        try {
            htmlReporter.setScreenshotDriver(appiumDriver);
            htmlReporter.saveScreenshot(logStatus, stepName, details);
        } catch (NullPointerException e) {
            e.printStackTrace();
            htmlReporter.saveScreenshot(logStatus, stepName, details);
        }
        htmlReporter.setScreenshotDriver(driver);
    }

    public void startTest(String suiteName, String methodeName, String category) {
        htmlReporter.startTest(suiteName, methodeName, category);
    }

    public JSONArray getJsonFromResources(String fileName, String directoryOfFile) {

        JSONArray json = null;
        String fullPath = "src/" + directoryOfFile + "/resources/" + fileName + ".json";
        File f = new File(fullPath);
        if (f.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(fullPath);
                String jsonTxt = IOUtils.toString(is);
                json = new JSONArray(jsonTxt);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public JSONObject getWantedJsonObject(JSONArray array, String property, String propertyValue) throws JSONException {

        JSONObject jSONObject = null;
        for (int number_of_devices = 0; number_of_devices < array.length(); number_of_devices++) {
            jSONObject = array.getJSONObject(number_of_devices);
            if (jSONObject.getString(property).equals(propertyValue)) {
                return jSONObject;
            }
        }
        return null;
    }


    public void startDriver(DriverMode driverMode, String udid,String  currentDriver) {
        try {
            this.mode = driverMode;
            capabilities = new DesiredCapabilities();
            if (driverMode == DriverMode.WEB) {
                setSelenium();
            } else {
                JSONArray array = getJsonFromResources("devices", "main");
                currentDeviceInfo = getWantedJsonObject(array, "udid", udid);
                setAppium(udid);
            }

            startDriver(currentDriver);
            System.out.println("The driver started.");
            if (!isMobile) {
                driver.manage().window().fullscreen();
            }
            if (currentDriver.equals("driver")) {
                saveScreenshot(LogStatus.PASS, "The driver started.", "Success");
            }else if (currentDriver.equals("driver1")) {
                saveScreenshot(driver1,LogStatus.PASS, "The driver started.", "Success");
            }else if (currentDriver.equals("driver2")) {
                saveScreenshot(driver2,LogStatus.PASS, "The driver started.", "Success");
            }

        } catch (Exception e) {
            e.printStackTrace();
//            saveScreenshot(LogStatus.FATAL, "The driver didn't start." +((e.getMessage() != null) ? e.getMessage(): ""), "Failed");
            throw new RuntimeException("Couldn't start the driver");
        }
    }

    private void setAppium(String udid, String pathToBuildFile) {
        logger.info("taking the capabilities from Json");
        try {
            if (pathToBuildFile.isEmpty()) {
                capabilities.setCapability(MobileCapabilityType.APP, "");
            } else {
                capabilities.setCapability(MobileCapabilityType.APP, pathToBuildFile);
            }
            capabilities = new DesiredCapabilities();
            capabilities.setCapability("automationName", currentDeviceInfo.getString("automationName"));
            capabilities.setCapability("platformVersion", currentDeviceInfo.getString("platformVersion"));
            capabilities.setCapability("browserName", currentDeviceInfo.getString("browserName"));
            capabilities.setCapability("platformName", currentDeviceInfo.getString("platformName"));
            boolean isAndroidBuild = capabilities.getCapability("platformName").toString().contains("Android");
            if (isAndroidBuild) {
                capabilities.setCapability("unicodeKeyboard", true);
                capabilities.setCapability("resetKeyboard", true);
                capabilities.setCapability("appPackage", currentDeviceInfo.getString("appPackage"));
                capabilities.setCapability("appWaitPackage", currentDeviceInfo.getString("appWaitPackage"));
                capabilities.setCapability("appWaitActivity", currentDeviceInfo.getString("appWaitActivity"));
                capabilities.setCapability("appActivity", currentDeviceInfo.getString("appActivity"));
                capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
                capabilities.setCapability("clearSystemFiles", true);
            } else {
                capabilities.setCapability("bundleId", currentDeviceInfo.getString("bundleId"));
                capabilities.setCapability("usePrebuiltWDA", true);
                capabilities.setCapability("preventWDAAttachments", true);
                capabilities.setCapability("keychainPath", "/Users/jenkins/Library/Keychains/MyNewKeychain.keychain-db");
                capabilities.setCapability("keychainPassword", "qualitest1234");
                capabilities.setCapability("xcodeConfigFile", System.getProperty("user.dir") + "/src/main/resources/XCUITest.xcconfig");
                logger.info("xcodeConfigFile path is : " + System.getProperty("user.dir") + "/src/main/resources/XCUITest.xcconfig");
            }
            capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
            capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
            capabilities.setCapability("clearSystemFiles", true);
            capabilities.setCapability("udid", udid);
            capabilities.setCapability("deviceName", currentDeviceInfo.getString("deviceName"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (s == null) {
            s = new AppiumServiceBuilder().usingAnyFreePort()
                    .withArgument(SESSION_OVERRIDE).build();
        }
    }

    private void setAppium(String udid) {
        setAppium(udid, "");
    }

    private void setSelenium() {
        System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + "/src/main/resources/geckodriver");
    }

    public void navigateTo(String url,String  currentDriver) {
        if (currentDriver.equals("driver")) {
            driver.navigate().to(url);
            saveScreenshot(LogStatus.PASS, "Navigating to: " + url, "Success");
        }else if (currentDriver.equals("driver1")) {
            driver1.navigate().to(url);
            saveScreenshot(driver1,LogStatus.PASS, "Navigating to: " + url, "Success");
        }else if (currentDriver.equals("driver2")){
            driver2.navigate().to(url);
            saveScreenshot(driver2,LogStatus.PASS, "Navigating to: " + url, "Success");
        }
    }

    private void startDriver(String currentDriver) throws InterruptedException {
        synchronized (lock) {
            switch (mode) {
                case IOS:
                    startAppiumServer();
                    driver = new IOSDriver<WebElement>(s.getUrl(), capabilities);
                    break;
                case ANDROID:
                    startAppiumServer();
                    driver = new AndroidDriver<WebElement>(s.getUrl(), capabilities);
                    break;
                case WEB:
                    if (currentDriver.equals("driver")) {
                        driver = new FirefoxDriver();
                        driver.manage().window().maximize();
                        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
                        wait = new WebDriverWait(driver, 10 * 60);
                    }else if (currentDriver.equals("driver1")){
                        driver1 = new FirefoxDriver();
                         driver1.manage().window().maximize();
                         driver1.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
                         wait = new WebDriverWait(driver1, 10 * 60);
                    }else if (currentDriver.equals("driver2")){
                        driver2 = new FirefoxDriver();
                        driver2.manage().window().maximize();
                        driver2.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
                        wait = new WebDriverWait(driver2, 10 * 60);
                    }

                    break;
            }
            if (isMobile && !s.isRunning()) {
                s.start();
            }
        }
        //Thread.sleep(6000);

    }

    private void startAppiumServer() {
        if (!s.isRunning()) {
            s.start();
        }
        isMobile = true;
    }

    public WebElement scrollTo(AppiumDriver driver, ScrollMode scrollMode, String text, SwipeDirections IosSwipeDirection, boolean isiosButton) throws NoSuchElementException {
        WebElement found = null;
        switch (scrollMode) {
            case TEXT:
                found = driver.findElement(MobileBy
                        .AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                                + "new UiSelector().text(\"" + text + "\"));"));
                break;
            case CONTAINS:
                if (mode == DriverMode.ANDROID) {
                    for (int i = 0; i < 2; i++) {
                        try {
                            found = driver.findElement(MobileBy
                                    .AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                                            + "new UiSelector().textContains(\"" + text + "\"));"));
                        } catch (Exception e) {
                            logger.warn("Dident find item: " + text + " on the " + i + " attempt");
                            scroll(SwipeDirections.UP);
                        }
                    }
                    break;
                } else {
                    String targetCell = "//XCUIElementTypeCell/XCUIElementTypeStaticText[contains(@name,'" + text + "')]";
                    if (isiosButton) {
                        targetCell = targetCell.replace("XCUIElementTypeCell/XCUIElementTypeStaticText", "XCUIElementTypeButton");
                    }

                    //identifying the parent Table
                    RemoteWebElement parent = (RemoteWebElement) driver.findElement(getiOSTableLocator());
                    String parentID = parent.getId();
                    HashMap<String, String> scrollObject = new HashMap<String, String>();
                    scrollObject.put("element", parentID);
                    // Use the predicate that provides the value of the label attribute
                    scrollObject.put("predicateString", "label CONTAINS '" + text + "'");
                    // scroll to the target element
                    driver.executeScript("mobile:scroll", scrollObject);
                    WebElement cellWithText = driver.findElement(By.xpath(targetCell));
                    logger.info("Found item " + cellWithText.getText() + "(ScroolTo)");
                    return cellWithText;
                }
        }

        return found;
    }

    public WebElement scrollTo(ScrollMode mode, String text, SwipeDirections IosSwipeDirection) {
        return scrollTo((AppiumDriver) driver, mode, text, IosSwipeDirection, false);
    }

    private By getiOSTableLocator() {
        return By.className("XCUIElementTypeTable");
    }

    public void scroll(SwipeDirections direction) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", direction.toString());
        js.executeScript("mobile: scroll", scrollObject);
    }

    public void swipingHorizontally(SwipeDirections direction, boolean isAndroid) throws InterruptedException {
        Dimension size = driver.manage().window().getSize();
        System.out.println(size);
        int startx = (int) (size.width * 0.70);
        int endx = (int) (size.width * 0.30);
        int starty = size.height / 2;
        switch (direction) {
            case RIGHT_TO_LEFT:
                o_left:
                if (isAndroid)
                    ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 2000); // it swipes from right to left
                else {
                    ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 5); // it swipes from right to left
                }
                break;
            case LEFT_TO_RIGHT:
                if (isAndroid)
                    ((AppiumDriver) driver).swipe(endx, starty, startx, starty, 2000); // it swiptes from left to right
                else {
                    ((AppiumDriver) driver).swipe(endx, starty, startx, starty, 5); // it swiptes from left to right
                }
                break;
        }
        System.out.println("Swiped to direction:" + direction);
        //ATUReports.add("Swiped to direction:" + direction, "Clicked succeeded.", "Clicked succeeded..", LogAs.PASSED,
        //new CaptureScreen((CaptureScreen.ScreenshotOf.BROWSER_PAGE)));
        Thread.sleep(2000);
    }

    public WebDriver switchContext(String wantedContext) {
        Set contextNames = ((AppiumDriver) driver).getContextHandles();
        for (Object context :
                contextNames) {
            String name = context.toString();
            if (name.toLowerCase().contains(wantedContext.toLowerCase()))
                ((AppiumDriver) driver).context(name);
        }
        return driver;
    }

    public String getCapability(String name) {
        return capabilities.getCapability(name).toString();
    }

    public void closeTest() {
        htmlReporter.closeTest();
    }

    public void closeDriver(WebDriver driver) {
        try {
            driver.quit();
        } catch (Exception e) {
            closeNode();
        }
        System.out.println("Appium driver were stopped ");
    }

    public void closeNode() {
        if (s != null && s.isRunning()) {
            s.stop();
        }

        System.out.println("Appium node were stopped ");
    }

    public void reportException(Exception e) {
        htmlReporter.reportException(e);
    }

    public void reportException(Exception e, boolean throwException) {
        htmlReporter.reportException(e, throwException);
    }

    public String executecommand(String command) {
        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println("Result line: " + line + "\n");
                output.append(line + "\n");
            }
            p.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public void JSsenddKeys(String value, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
//        executor.executeScript("arguments[0].sendKeys('"+ value + "');", element);
        executor.executeScript("arguments[0].setAttribute('value','" + value + "'", element);
    }

    public void swipeDown() {

        if (isMobile) {
            ((AppiumDriver) driver).context("NATIVE_APP");
            Dimension size = driver.manage().window().getSize();
            int starty = (int) (size.height * 0.8);
            int endy = (int) (size.height * 0.20);
            int startx = size.width / 2;
            ((AppiumDriver) driver).swipe(startx, starty, startx, endy, 1000);
        }
    }

    public void smallSwipeDown() {

        ((AppiumDriver) driver).context("NATIVE_APP");
        Dimension size = driver.manage().window().getSize();
        int starty = (int) (size.height * 0.8);
        int endy = (int) (size.height * 0.70);
        int startx = size.width / 2;
        ((AppiumDriver) driver).swipe(startx, starty, startx, endy, 1000);

    }

    public void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int endy = (int) (size.height * 0.8);
        int starty = (int) (size.height * 0.20);
        int startx = size.width / 2;
        ((AppiumDriver) driver).swipe(startx, starty, startx, endy, 1000);
    }

    public void bigSwipe(SwipeDirections direction) {
        for (int i = 0; i < 2; i++)
            scroll(direction);
    }

    public void doubleClick(WebElement element, String description) {
        try {
            if (mode == DriverMode.ANDROID || mode == DriverMode.WEB) {
                doubleTapElementAndroid(element);
            } else if (mode == DriverMode.IOS) {
                doubleTapElementiOS((RemoteWebElement) element);
            }
            logger.info("Double click on the element " + description);
            saveScreenshot(LogStatus.PASS, "Double click on the element " + description, "Success");

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Couldn't double click on the element " + description);
            saveScreenshot(LogStatus.FAIL, "Couldn't double click on the element " + description, "Failed");
        }
    }

    private void doubleTapElementiOS(RemoteWebElement element) {
        HashMap<String, Object> tapObject = new HashMap<String, Object>();

        tapObject.put("x", 0.5); // in pixels from left

        tapObject.put("y", 0.5); // in pixels from top

        tapObject.put("tapCount", 2.0);  // double tap

        tapObject.put("element", element.getId()); // the id of the element we want to tap

        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("mobile: tap", tapObject);
    }

    protected void sendKeyboardKeys(int number, String description) {

        try {
            ((AndroidDriver) driver).pressKeyCode(number);
            logger.info("Press on the " + description + " key on the keyboard.");
            saveScreenshot(LogStatus.PASS, "Press on the " + description + " key on the keyboard.", "True");


        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Fail to press on the key" + description);
            saveScreenshot(LogStatus.FAIL, "Fail to press on the key" + description, "False");
        }
    }


    public void clickSearch() {
        if (mode == DriverMode.ANDROID)
            sendKeyboardKeys(SEARCHBUTTON, "Search");
        else
            iOSclickSearch();
    }

    public boolean doubleTapElementAndroid(WebElement element) {
        int x, y;
        try {
            x = ((MobileElement) element).getCenter().getX();
            y = ((MobileElement) element).getCenter().getY();
            ((AppiumDriver) driver).tap(1, x, y, 100);
            try {
                Thread.sleep(50);
            } catch (Exception e1) {
            }
            ((AppiumDriver) driver).tap(1, x, y, 100);
            try {
                Thread.sleep(100);
            } catch (Exception e1) {
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private By getIOSChangeKeyboardLocator() {
        return By.xpath("//XCUIElementTypeButton[@value = 'עברית']");
    }

    private By getIOSSearchButtonLocator() {
        return By.xpath("//XCUIElementTypeButton[contains(@name,'Search')]");
    }

    private void iOSclickSearch() {
        WebElement changeKeboard = waitUntilWithCondition("clickable", getIOSChangeKeyboardLocator());

        if (changeKeboard != null) {
            changeKeboard.click();
        }
        WebElement searchButton = driver.findElement(getIOSSearchButtonLocator());
        clickElement(searchButton, "Search");
    }

    public WebElement waitUntilWithCondition(String expectedConditions, By locator) {
        try {
            switch (expectedConditions.toLowerCase()) {
                case "clickable":
                    return wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElement(locator)));
            }
        } catch (Exception e) {
            logger.info("Timeout waiting to element to be " + expectedConditions);
        }
        return null;
    }

    public WebElement waitUntilWithCondition(String expectedConditions, WebElement elem) {
        try {
            switch (expectedConditions.toLowerCase()) {
                case "clickable":
                    return wait.until(ExpectedConditions.elementToBeClickable(elem));
            }
        } catch (Exception e) {
            logger.info("Timeout waiting to element to be " + expectedConditions);
        }
        return null;
    }

    public void clickElement(WebElement element, String description, boolean reportException) // clicking element
    {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            System.out.println("Clicked on " + description + " element");
            saveScreenshot(LogStatus.PASS, "Clicked on " + description + " element", "Clicked succeeded");
        } catch (Exception msg) {
            if (reportException) {
                msg.printStackTrace();
                saveScreenshot(LogStatus.FAIL, "Clicked on " + description + " element", "Click were not succeeded");
                htmlReporter.clickFailed = true;
                System.out.println("click Failed value: " + htmlReporter.clickFailed);
            }
        }
    }

    public void clickElement(WebElement element, String description) // clicking element
    {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            System.out.println("Clicked on " + description + " element");
            saveScreenshot(LogStatus.PASS, "Clicked on " + description + " element", "Clicked succeeded");
        } catch (Exception msg) {
            msg.printStackTrace();
            saveScreenshot(LogStatus.FAIL, "Clicked on " + description + " element", "Click were not succeeded");
            htmlReporter.clickFailed = true;
            System.out.println("click Failed value: " + htmlReporter.clickFailed);
        }
    }

    public void clickElement(By by, String description) // clicking element
    {
        WebElement element = null;
        try {
            element = driver.findElement(by);
        } catch (Exception e) {
            saveScreenshot(LogStatus.FAIL, "Trying to click element " + description, "Couldn't find the element");
            htmlReporter.clickFailed = true;
            System.out.println("click Failed value: " + htmlReporter.clickFailed);
            return;
        }
        clickElement(element, description, true);
    }

    public void clickElement(By by, String description, boolean reportException) // clicking element
    {
        WebElement element = null;
        try {
            element = driver.findElement(by);
        } catch (Exception e) {
            if (reportException) {
                saveScreenshot(LogStatus.FAIL, "Trying to click element " + description, "Couldn't find the element");
                htmlReporter.clickFailed = true;
                System.out.println("click Failed value: " + htmlReporter.clickFailed);
            }
            return;
        }
        clickElement(element, description, reportException);
    }

    // This function send keys to input, and verify that this keys appear in
    // input
    public void sendKeysToWebElementInput(WebElement web_element, String target_input) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(web_element));
            web_element.clear();
            web_element.sendKeys(target_input);
            System.out.println("Target keys sent to WebElement: " + target_input);
            saveScreenshot(LogStatus.PASS, "Entered and sent the string: " + target_input, "Target keys sent.");

            if (isMobile) {
                try {
                    ((AppiumDriver) driver).hideKeyboard();
                } catch (Exception e) {
                    System.out.println("hideKeyboard exception");
                }
            }

        } catch (Exception msg) {
            msg.printStackTrace();
            System.out.println("Fail to sent target keys: " + target_input);
            saveScreenshot(LogStatus.FAIL, "Entered and sent the string: " + target_input, "String were NOT sent");
            throw new RuntimeException("String were NOT sent");
        }
    }

    public void sendKeysToWebElementInput(By by, String target_input) {
        WebElement web_element = driver.findElement(by);
        sendKeysToWebElementInput(web_element, target_input);
    }

    public void verifyThatTheTextOfTheElementIsAsExpected(WebElement element, String... params) {
        String orignalName = element.getText();
        if (orignalName != null) {
            for (String name : params) {
                if (orignalName.equals(name)) {
                    System.out.println("The text of the element is: " + orignalName + " as expected.");
                    saveScreenshot(LogStatus.PASS, "The text of the element is: " + orignalName + " as expected.", "True");

                    return;
                }
            }
        }
        System.out.println("The text of the element is: " + orignalName + " not as expected.");
        saveScreenshot(LogStatus.FAIL, "The text of the element is: " + orignalName + "not as expected.", "False");

    }

    public Boolean verifyTextPresence(By elementLocator, String wantedText) {
        Boolean passed = false;
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(elementLocator, wantedText));
            WebElement element = driver.findElement(elementLocator);

            if (element.isDisplayed()) {
                String text = element.getText();
                if (text.contains(wantedText)) {
                    passed = true;
                    System.out.println("The wanted text appeaerd in the wanted element. The text is:  " + text);
                    saveScreenshot(LogStatus.PASS, "The wanted text appeaerd in the wanted element. The text is:  " + text, "Success");

                } else {
                    System.out.println("Can't find the  wanted text " + wantedText + " found the text: " + text);
                    saveScreenshot(LogStatus.FAIL, "Can't find the  wanted text " + wantedText + " found the text: " + text, "Failed");

                }
            } else {
                System.out.println("Can't find the wanted element and text,He is not displayed");
                saveScreenshot(LogStatus.FAIL, "Can't find the wanted element and text,He is not displayed", "Failed");

            }
        } catch (Exception e) {
            System.out.println("Can't find the wanted element and text,He is not displayed");
            saveScreenshot(LogStatus.FAIL, "Can't find the wanted element and text,He is not displayed", "Failed");

        }

        return passed;
    }

    public boolean tryToCompareAttributeValue(WebElement element, String attributName, String wantedValue) {
        try {
            return element.getAttribute(attributName).contains(wantedValue);
        } catch (Exception e) {
            return false;
        }
    }

    public void highlight(WebElement element) {
        if (!isMobile) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", element);
        }
    }

    void waitForVisibility(WebElement element) {

        try {
            Thread.sleep(500);
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (org.openqa.selenium.TimeoutException e) {
            logger.info("Waiting for element visibiliy failed");
            e.printStackTrace();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.info("Waiting for element visibiliy failed");
            e.printStackTrace();
        } catch (Exception e) {
            logger.info("Waiting for element visibiliy failed");
            e.printStackTrace();
        }
    }

    public void longTapOnLocation(int x, int y, int duration, String description) // clicking element
    {

        try {
            if (mode == DriverMode.ANDROID) {
                TouchAction action = new TouchAction((AppiumDriver) driver);
                action.longPress(x, y, duration).release().perform();
            } else {
                TouchAction action = new TouchAction((AppiumDriver) driver);
                action.press(x, y).waitAction(500).release().perform();
            }
        } catch (Exception msg) {
            saveScreenshot(LogStatus.FAIL, "Long Tap was NOT done on location: x: " + x + ",y: " + y + " , duration: " + duration, "Tap  did not succeeded");
            htmlReporter.clickFailed = true;
            logger.info("click Failed value: " + htmlReporter.clickFailed);
            return;
        }
        logger.info("Long Tap was done on location: x: " + x + ",y: " + y + " , duration: " + duration + ", for " + description);
        saveScreenshot(LogStatus.PASS, "Long Tap was done on location: x: " + x + ",y: " + y + " , duration: " + duration + ", for " + description, "Clicked succeeded");

    }

    private boolean isElementDisplayed(WebElement element) {

        boolean isDisplay = false;
        try {
            if (element.isDisplayed()) {
                isDisplay = true;
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            isDisplay = false;
        }
        return isDisplay;
    }

    public boolean isElementDisplayed(By by) {
        boolean isDisplay = false;

        try {
            WebElement element = driver.findElement(by);
            if (element.isDisplayed()) {
                isDisplay = true;
            }
        } catch (WebDriverException e) {
            isDisplay = false;
        }
        return isDisplay;
    }

    public void verifyElementIsDisplayed(WebElement element, String description) {

        if (isElementDisplayed(element)) {
            logger.info("The element: " + description + " is displayed.");
            saveScreenshot(LogStatus.PASS, "Element is displayed.", "True");

        } else {
            logger.info("The element: " + description + " is not displayed.");
            saveScreenshot(LogStatus.FAIL, "Element is NOT displayed.", "Element is NOT displayed.");

        }

    }

    public void tapOnNearElement(WebElement nearTo, int xDistance, int yDistance, String description) {
        tapOnNearElement(nearTo, xDistance, yDistance, description, true);
    }

    public void tapOnNearElement(WebElement nearTo, int xDistance, int yDistance, String description, boolean reportClick) {
        //for negetive distance use negetive(-) number
        tapOnTheScreenByCoordinates(nearTo.getLocation().getX() + xDistance, nearTo.getLocation().getY() + yDistance, description, reportClick);
    }

    protected void tapOnTheScreenByCoordinates(int x, int y, String description, boolean reportTrial) {
        try {
            ((AppiumDriver) driver).tap(1, x, y, 1);
            logger.info("Clicked on " + description + " element at: x: " + x + ", y: " + y);
            if (reportTrial) {
                saveScreenshot(LogStatus.PASS, "Clicked on " + description + " element ", "Clicked succeeded");
            }

        } catch (Exception msg) {
            logger.info("Fail to Tap on the screen on the element " + description);
            msg.printStackTrace();
        }
    }

    public void swipeLeft() {

        ((AppiumDriver) driver).context("NATIVE_APP");
        Dimension size = driver.manage().window().getSize();
        int startx = (int) (size.width * 0.8);
        int endx = (int) (size.width * 0.20);
        int starty = size.height / 2;
        ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 1000);

    }

    public void swipeRight() {

        ((AppiumDriver) driver).context("NATIVE_APP");
        Dimension size = driver.manage().window().getSize();
        int endx = (int) (size.width * 0.8);
        int startx = (int) (size.width * 0.20);
        int starty = size.height / 2;
        ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 1000);
    }

    public void scrollDown() {
        if (mode == DriverMode.ANDROID) {
            scroll(SwipeDirections.DOWN);
        } else {
            swipeDown();
        }
    }

    public void waitForElementBeDisplay(By by, int timeOutInSec) {
        if (timeOutInSec == 0) {
            throw new NoSuchElementException("Failed to located :" + by);
        }
        try {
            Thread.sleep(1000);
            timeOutInSec--;
            WebElement element = driver.findElement(by);
            if (element.isDisplayed()) {
                return;
            } else {
                waitForElementBeDisplay(by, timeOutInSec);
            }
        } catch (Exception e) {
            logger.info("element doesn't exist, trying again. iteration number: " + timeOutInSec + " exception message: " + e.getMessage());
            waitForElementBeDisplay(by, timeOutInSec);
        }
    }

    public boolean waitForElementBeDisplayWithoutThrowingTimeoutException(By by, int timeOutInSec) {
        if (timeOutInSec == 0) {
            return false;
        }
        try {
            Thread.sleep(1000);
            timeOutInSec--;
            WebElement element = driver.findElement(by);
            if (element.isDisplayed()) {
                return true;
            } else {
                waitForElementBeDisplayWithoutThrowingTimeoutException(by, timeOutInSec);
                return false;
            }
        } catch (Exception e) {
            logger.info("element doesn't exist, trying again. iteration number: " + timeOutInSec + " exception message: " + e.getMessage());
            waitForElementBeDisplayWithoutThrowingTimeoutException(by, timeOutInSec);
            return false;
        }
    }

    protected boolean waitUntilTextIsPresent(By by, String regex, long timeoutSeconds, long pollingMilliseconds) {
        return waitUntilTextIsPresent(by, regex, timeoutSeconds, pollingMilliseconds, true);
    }

    protected boolean waitUntilTextIsPresent(By by, String regex, long timeoutSeconds, long pollingMilliseconds, boolean reportFail) {
        final String[] text = {""};
        final String[] lasttext = {""};
        boolean foundText = false;
        try {
            final WebElement webElement = driver.findElement(by);
            new FluentWait<AppiumDriver>((AppiumDriver) driver)
                    .withTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .pollingEvery(pollingMilliseconds, TimeUnit.MILLISECONDS)
                    .until((Predicate<AppiumDriver>) d -> {
                        Pattern p = Pattern.compile(regex);
                        lasttext[0] = webElement.getText();
                        if (lasttext[0] != null) {
                            text[0] = lasttext[0];
                            Matcher m = p.matcher(text[0]);
                            boolean found = m.find();
                            if (found) {
                                logger.info("Found string: " + text[0]);
                            } else {
                                logger.info("Current string: " + text[0]);
                            }
                            return (found);
                        } else return false;
                    });

            saveScreenshot(LogStatus.PASS, "Verifying message appeared", "Found message: " + text[0]);
            foundText = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Did NOT Find the message with regex: " + regex + " ,last text is: " + lasttext[0]);
            if (reportFail) {
                saveScreenshot(LogStatus.FAIL, "Verifying message appeared", "Did NOT Find the message with regex: " + regex + " ,last text is: " + text[0]);
            }
            foundText = false;
        }
        return foundText;
    }

    public void swipDownUntillButtonBeenFoundAndCick(By by, String description) {
        int timeOut = 10;
        while (timeOut > 0) {
            try {
                Thread.sleep(1000);
                WebElement element = driver.findElement(by);
                if (element.isDisplayed()) {
                    clickElement(element, description);
                    break;
                }
            } catch (Exception e) {

            }
            swipeDown();
            timeOut--;
        }
    }

    public boolean isExists(By elementIdentifier, int timeoutSeconds) {
        return isExists(elementIdentifier, timeoutSeconds, false);
    }

    public Boolean isExists(By elementIdentifier, int timeoutSeconds, boolean iosHidden) {
        Map<String, String> formatedBy = formatByIdentifier(elementIdentifier);
        String pageSource = "";
        if (mode == DriverMode.ANDROID) {
            return androidIsExists(timeoutSeconds, formatedBy, pageSource);
        } else if (mode == DriverMode.IOS)
            return iosIsExists(elementIdentifier, iosHidden);
        else return null;
    }

    private boolean iosIsExists(By elementIdentifier, boolean isHiddenElement) {
        try {
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            WebElement element = driver.findElement(elementIdentifier);
            if (isHiddenElement) {
                logger.info("iosIsExists returning true because its an ios hidden element");
                return true;
            } else {
                boolean displayed = element.isDisplayed();
                logger.info("iosIsExists returning is displayed: " + displayed);
                return displayed;
            }
        } catch (Exception e) {
            logger.info("iosIsExists didn't find the elementIdentifier " + elementIdentifier);
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        }
    }

    private boolean androidIsExists(int timeoutSeconds, Map<String, String> formatedBy, String pageSource) {
        Pattern p = Pattern.compile("<.+(enabled=\"true\")?.=\"" + formatedBy.get("identifier") + ".+(visible=\"true\")?");
        try {
            pageSource = driver.getPageSource();
        } catch (Exception e) {
            logger.info("problem with the getPageSource: " + e.toString() + ":\n" + e.getStackTrace().toString());
        }
        for (int i = 0; i < timeoutSeconds; i++) {
            if (pageSource.isEmpty()) {
                i--;
            }
            Matcher m = p.matcher(pageSource);
            if (m.find()) {
                logger.info("Found the object " + formatedBy.get("byText") + " on second number " + (i + 1));
                return true;
            } else if ((i + 1) < timeoutSeconds) {
                try {
                    pageSource = driver.getPageSource();
                } catch (Exception e) {
                    logger.info("index " + i + " attempt: problem with the getPageSource: " + e.toString() + ":\n" + e.getStackTrace().toString());
                }
            }
        }
        logger.info("Did NOT find the object " + formatedBy.get("byText") + " after " + timeoutSeconds + " seconds");
        return false;
    }

    //Do NOT work on all xpaths(only the ones needed to start the test)
    private Map<String, String> formatByIdentifier(By by) {
        Map<String, String> formatedBy = new HashMap<>();
        formatedBy.put("byText", by.toString());
        int index = -1;
        if (formatedBy.get("byText").toLowerCase().contains("xpath")) {
            index = formatedBy.get("byText").indexOf("'");
            int lastIndex = formatedBy.get("byText").lastIndexOf("'");
            int parenthesisIndex = (formatedBy.get("byText").contains("and")) ?
                    formatedBy.get("byText").indexOf(")") : -1;
            String formated = (parenthesisIndex > 0 ?
                    formatedBy.get("byText").substring(index + 1, parenthesisIndex - 1) :
                    formatedBy.get("byText").substring(index + 1, lastIndex));
            formatedBy.put("identifier", formated);
        } else {
            index = formatedBy.get("byText").indexOf(":");
            int len = formatedBy.get("byText").length();
            formatedBy.put("identifier", formatedBy.get("byText").substring(index + 2, len));
        }
        return formatedBy;
    }

    // [M.E] - Get element's parent
    public WebElement getElementParent(WebElement element) {
        return element.findElement(By.xpath(".."));
    }

    // [M.E] - wait for a specific activty to be displayed
    public boolean waitForActivityToBeDisplayed(String activityName, int secondTimeout) {
        try {
            String line;
            String output = null;
            int secondCounter = 0;
            do {
                Process p = new ProcessBuilder(new String[]{"adb", "-s", System.getProperty("udid"), "shell", "dumpsys", "window", "windows |", "grep -E 'mCurrentFocus|mFocusedApp'"})
                        .redirectErrorStream(true)
                        .start();
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = input.readLine()) != null) {
                    output += line;
                }
                if (output.contains(activityName))
                    return true;
                logger.info(output);
                secondCounter++;
                Thread.sleep(3000);
            } while (secondCounter < secondTimeout);

            return false;

        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    // [M.E] - simulate swipe\scroll gesture
    public void pressMoveToAndRelease(int startX, int startY, int endX, int endY, String description) {
        try {
            new TouchAction((AppiumDriver) driver).press(startX, startY).moveTo(endX, endY).release().perform();
            AssertManager.assertTest(true, true, description + "succeeded", description + " failed", description);
        } catch (Exception e) {
            AssertManager.assertTest(false, true, description + "succeeded", e.getMessage(), description);
        }
    }


    // [M.E] - tap on an area and check if a condition is an element by is displayed afterwards
    // widthArea: 0 - 1
    // heightArea: 0 - 1
    // eachIntervalAddition: pixels
    // areaRadius: the boundries of the tested areas, pixels
    public boolean tapOnAreaRadiusAndCheckCondition(double widthArea, double heightArea, int eachIntervalAddition, int areaRadius, By by, Conditions condition) {
        return tapOnAreaRadiusAndCheckCondition(widthArea, heightArea, eachIntervalAddition, areaRadius, 1500, 500, by, Conditions.IS_VISIBLE, "Find Element by tapping on it's area");
    }

    // [M.E] - tap on an area and check if a condition is an element by is displayed afterwards
    // widthArea: 0 - 1
    // heightArea: 0 - 1
    // eachIntervalAddition: pixels
    // areaRadius: the boundries of the tested areas, pixels
    // fallback - function to run in certain condition
    public boolean tapOnAreaRadiusAndCheckCondition(double widthArea, double heightArea, int eachIntervalAddition, int areaRadius, int intervalTimeout, int intervalPooling, By by, Conditions condition, String description) {
        Dimension size = driver.manage().window().getSize();
        int middleX = (int) (size.width * widthArea), middleY = (int) (size.height * heightArea), addition = eachIntervalAddition, boundry = areaRadius;
        boolean reportOpened = false;

        // check the diagonal lines of the screen ( upper left and upper right )
        for (int x1 = middleX - addition, y1 = middleY - addition, x2 = middleX + addition, y2 = middleY - addition; x1 > middleX - boundry; x1 -= addition, y1 += addition, x2 += addition, y2 -= addition) {
            tapOnTheScreenByCoordinates(x1, y1, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
            tapOnTheScreenByCoordinates(x2, y2, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
        }

        // check the diagonal lines of the screen ( bottom right and bottom left)
        for (int x1 = middleX + addition, x2 = middleX - addition, y1 = middleY + addition, y2 = middleY - addition; x1 < middleX + boundry; x1 += addition, y1 += addition, x2 -= addition, y2 -= addition) {
            tapOnTheScreenByCoordinates(x1, y1, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
            tapOnTheScreenByCoordinates(x2, y2, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
        }

        // check vertical lines of the screen
        for (int x1 = middleX, y1 = middleY + addition, y2 = middleY - addition; x1 < middleX + boundry; y1 += addition, y2 -= addition) {
            tapOnTheScreenByCoordinates(x1, y1, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
            tapOnTheScreenByCoordinates(x1, y2, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
        }

        // check horizontal lines of the screen
        for (int x1 = middleX, y1 = middleY + addition, y2 = middleY - addition; x1 < middleX + boundry; y1 += addition, y2 -= addition) {
            tapOnTheScreenByCoordinates(y1, x1, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
            tapOnTheScreenByCoordinates(y2, x1, "Report ", false);
            if (checkIfElementConditionMetWithoutTrowingAnError(by, intervalTimeout, intervalPooling, description, condition))
                return true;
        }

        saveScreenshot(LogStatus.FAIL, description, description + " FAILED");
        htmlReporter.clickFailed = true;
        return false;

    }

    public boolean checkIfElementConditionMetWithoutTrowingAnError(By by, int timeoutMili, int poolingMili, String description, Conditions condition) {
        return checkIfElementConditionMetWithoutTrowingAnError(by, timeoutMili, poolingMili, description, condition, "");
    }

    // [M.E] - Check if element is visible without throwing an error in case it's not
    public boolean checkIfElementConditionMetWithoutTrowingAnError(By by, int timeoutMili, int poolingMili, String description, Conditions condition, String text) {
        try {

            Wait<WebDriver> customWait = new FluentWait<WebDriver>(driver)
                    .withTimeout(timeoutMili, MILLISECONDS)
                    .pollingEvery(poolingMili, MILLISECONDS)
                    .ignoring(NoSuchElementException.class);
            if (condition == Conditions.IS_CLICKABLE)
                ((FluentWait<WebDriver>) customWait).until(ExpectedConditions.elementToBeClickable(by));
            else if (condition == Conditions.IS_VISIBLE)
                ((FluentWait<WebDriver>) customWait).until(ExpectedConditions.visibilityOfElementLocated(by));
            else if (condition == Conditions.TEXT_IS_PRESENT)
                ((FluentWait<WebDriver>) customWait).until(ExpectedConditions.textToBePresentInElementLocated(by, text));


            saveScreenshot(LogStatus.PASS, description == "" ? description : "Verifying element condition", "Element condition was met");
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void TapOnElement(WebElement element, String description) // clicking element
    {

        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            TouchAction action = new TouchAction((AppiumDriver) driver);
            action.press(element).release().perform();
            logger.info("Tap was done on " + description + " element");
            saveScreenshot(LogStatus.PASS, "Tap was done on " + description + " element", "Clicked succeeded");

        } catch (Exception msg) {
            saveScreenshot(LogStatus.FAIL, "Tap was done on " + description + " element", "Tap  did not succeeded");
            htmlReporter.clickFailed = true;
            logger.info("click Failed value: " + htmlReporter.clickFailed);
        }

    }

    // [M.E] - Return  by element type and element id with xpath in case of id is identical to element text
    // OSandElementType - OS.ANDROID \ OS.IOS
    public By getElementbyTextId(String elementText, HashMap<DriverMode, String> OSandElementType) {
        return mode == DriverMode.ANDROID ? MobileBy.xpath("//" + OSandElementType.get(DriverMode.ANDROID) + "[@resource-id = '" + elementText + "']") : MobileBy.xpath("//" + OSandElementType.get(DriverMode.IOS) + "[@label = '" + elementText + "']");
    }

    // [M.E] - Return element by by id
    public By getElementbyTextId(String elementText) {
        return mode == DriverMode.ANDROID ? MobileBy.id(elementText) : MobileBy.AccessibilityId(elementText);
    }

    // [M.E] -  Return element by it's text
    // OSandElementType - OS.ANDROID \ OS.IOS
    public By getElementbyText(String elementText, HashMap<DriverMode, String> OSandElementType) {
        return mode == DriverMode.ANDROID ? MobileBy.xpath("//" + OSandElementType.get(DriverMode.ANDROID) + "[@text = '" + elementText + "']") : MobileBy.xpath("//" + OSandElementType.get(DriverMode.IOS) + "[@text = '" + elementText + "']");
    }

    public void swipingHorizontallyByCoordinates(SwipeDirections direction, boolean isAndroid, int startx, int starty, int endx) throws InterruptedException {
        Dimension size = driver.manage().window().getSize();
        logger.info("window size: " + size);

        try {
            switch (direction) {
                case RIGHT_TO_LEFT:
                    if (isAndroid)
                        ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 2000); // it swipes from right to left
                    else {
                        ((AppiumDriver) driver).swipe(startx, starty, endx, starty, 5); // it swipes from right to left
                    }
                    break;
                case LEFT_TO_RIGHT:
                    if (isAndroid)
                        ((AppiumDriver) driver).swipe(endx, starty, startx, starty, 2000); // it swiptes from left to right
                    else {
                        ((AppiumDriver) driver).swipe(endx, starty, startx, starty, 5); // it swiptes from left to right
                    }
                    break;
            }
            logger.info("Swiped to direction:" + direction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(2000);
    }

    public AppiumDriver switchContext(String wantedContext, AppiumDriver driver) {
        Set contextNames = driver.getContextHandles();
        for (Object context :
                contextNames) {
            String name = context.toString();
            if (name.contains(wantedContext)) {
                driver.context(name);
                logger.info("switched Context to " + name);
            }
        }
        if (!driver.getContext().contains(wantedContext)) {
            if (mode == DriverMode.ANDROID) {
                throw new WebDriverException("Failed to switch context");
            } else {
                logger.info("Couldn't find context " + wantedContext + ", Driver mode = " + mode);
            }
        }
        return driver;
    }

    public void switchWindowByName(String name) {
        for (String handle :
                driver.getWindowHandles()) {
            String title = driver.switchTo().window(handle).getTitle();
            logger.info("handle" + handle + " ,title:" + title);
            if (title.toLowerCase().contains(name.toLowerCase()))
                break;
        }
    }

    public List<LogEntry> captureLogCat(String tag) {
        LogEntries entireLogBuffer = driver.manage().logs().get("logcat");
        List<LogEntry> relevantEntries = new ArrayList<>();
        Iterator<LogEntry> logIter = entireLogBuffer.iterator();
        while (logIter.hasNext()) {
            LogEntry entry = logIter.next();
            if (entry.getMessage().contains(tag)
                    && entry.getTimestamp() > htmlReporter.getExtentTest().getStartedTime().getTime()) {
                relevantEntries.add(entry);
                logger.info("adding" + entry.getMessage());
            }
        }
        return relevantEntries;
    }

    public void swipeVerticalFromElement(AppiumDriver driver, By by, SwipeDirections directions) {
        WebElement point = driver.findElement(by);
//        driver.context("NATIVE_APP");
        Dimension size = driver.manage().window().getSize();
        int endy = 0;
        int starty = 0;
        int startx = size.width / 2;
        if (directions == SwipeDirections.DOWN) {
            if (mode == DriverMode.ANDROID) {
                starty = 0;
                endy = point.getLocation().getY();
            } else {
                endy = 0;
                starty = point.getLocation().getY();
            }
        } else {
            if (mode == DriverMode.ANDROID) {
                starty = point.getLocation().getY();
                endy = size.height - 200;
            } else {
                endy = point.getLocation().getY();
                starty = (int) (size.height - 200);
            }
        }
        driver.swipe(startx, starty, startx, endy, 1000);
    }

    public void clickbyjavascript(WebDriver chromDriver, WebElement alreadyAwazer) {
        JavascriptExecutor executor = (JavascriptExecutor) chromDriver;
        executor.executeScript("arguments[0].click();", alreadyAwazer);
    }

    public void androidFastSendKeys(String text) {
        try {
            new ProcessBuilder(new String[]{"adb", "-s", System.getProperty("udid"), "shell", "input", "text", text})
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedReader getBufferReader( String fullPathtoFile) throws IOException {
        BufferedReader bufferedReader = null;

        // FileReader reads text files in the default encoding.
        FileReader fileReader =
                new FileReader(fullPathtoFile);

        // Always wrap FileReader in BufferedReader.
        bufferedReader =
                new BufferedReader(fileReader);

        return bufferedReader;
    }

    public boolean findLog(String whatToFind,String fullPathtoFile) {
        Boolean found = false;
        BufferedReader bufferedReader = null;
        try {
            String line = null;
            // Always wrap FileReader in BufferedReader.
            bufferedReader = getBufferReader(fullPathtoFile);

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(whatToFind)) {
                    found = true;
                    System.out.println("we found what we are looking" + ' ' + line.toString());
                    return found;
                }
            }
            System.out.println("we don't found what we are looking");

        } catch (FileNotFoundException ex) {
            logger.info(
                    "Unable to open file");
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.info(
                    "Error reading file '");
            // Or we could just do this:
            ex.printStackTrace();
        } finally {
            // Always close files.
            try {
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return found;
    }

}
