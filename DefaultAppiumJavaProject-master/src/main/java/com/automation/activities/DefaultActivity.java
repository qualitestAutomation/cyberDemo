package com.automation.activities;


import com.automation.helpers.DriverManager;
import com.automation.helpers.DriverMode;
import com.automation.helpers.ScrollMode;
import com.automation.helpers.SwipeDirections;
import com.relevantcodes.extentreports.LogStatus;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DefaultActivity {

    public DriverManager manager;

    public Logger logger = Logger.getLogger(this.getClass());

    public DefaultActivity(DriverManager manager) {
        this.manager = manager;
    }

    public void clickBackOnTheDevice() throws InterruptedException {
        Thread.sleep(1000);
        try {
            Object deviceType = ((AppiumDriver) manager.getCurrentDriver()).getCapabilities().getCapability("platformName");

            if (deviceType.equals("Android")) {
                ((AndroidDriver) manager.getCurrentDriver()).pressKeyCode(AndroidKeyCode.BACK);
            } else clickBackOnIOSDevice();

            logger.info("pressed on the back icon.");
            manager.saveScreenshot(LogStatus.INFO, "clicking Back On The Device", "clicked Back On The Device");
            Thread.sleep(500);
        } catch (Exception e) {
            logger.info("Fail to press on the back icon.");
            e.printStackTrace();
        }
        Thread.sleep(1000);
    }

    public void clickBackOnIOSDevice() {
        WebElement backButton = manager.getCurrentDriver().findElement(getiOSGeneralBackbuttonLocator());
        backButton.click();
    }

    protected By getiOSGeneralBackbuttonLocator() {
        return manager.mode == DriverMode.ANDROID ? By.id("com.waze:id/titleBarCloseButton") : By.xpath("//XCUIElementTypeButton[contains(@label,'back') or contains(@label,'Back')]");
    }

    public void pressOnTheLayoutWithThatString(List<WebElement> layoutList, String nameToPress) {

        for (WebElement we : layoutList) {
            List<WebElement> elementsInLayout = we.findElements(getElementsInLayoutLocator());
            for (WebElement el : elementsInLayout) {
                if (el.getText().equals(nameToPress)) {
                    el.click();
                    logger.info("Pressed On The cell with the String:  " + nameToPress);
                    manager.saveScreenshot(LogStatus.PASS, "Pressed On The cell with the String:  " + nameToPress, "Success");
                    return;
                }
            }
        }
        logger.info("Couldn't find The cell with the String:  " + nameToPress);
        manager.saveScreenshot(LogStatus.FAIL, "Couldn't find The cell with the String:  " + nameToPress + nameToPress, "Failed");
    }

    private By getElementsInLayoutLocator() {
        return (manager.mode == DriverMode.ANDROID) ? By.className("android.widget.TextView") : By.className("XCUIElementTypeStaticText");
    }

    public void newSelectMenuItem(String itemName, boolean iOSgenralSettingsMenu) {
        logger.info("Looking for Item: " + itemName + "(newSelectMenuItem)");
        try {
            WebElement item = manager.scrollTo(ScrollMode.CONTAINS, itemName, SwipeDirections.DOWN);
            item.click();
            manager.saveScreenshot(LogStatus.PASS, "Selecting " + itemName + " item.", "Item " + itemName + " selected.");
            logger.info("Item " + itemName + " selected.");
        } catch (Exception e) {
            e.printStackTrace();
            manager.saveScreenshot(LogStatus.FAIL, "Selecting " + itemName + " item.", "Item " + itemName + " were NOT selected.");
            throw new RuntimeException("Item " + itemName + " was NOT found");
        }
    }

    public void newSelectMenuItem(String itemName) {
        newSelectMenuItem(itemName, false);
    }

    private void handleAlert(boolean accept) {
        try {
            Alert alert = (manager.getCurrentDriver()).switchTo().alert();
            logger.info("Alert text: " + alert.getText());
            if (accept) {
                alert.accept();
            } else alert.dismiss();
            logger.info("Alert accepted");
            return;
        } catch (Exception e) {
            logger.info("No alert found");
        }
    }

    public void handleAlertWithText(boolean accept, String wantedtext) {
        String text = "";
        try {
            Alert alert = manager.getCurrentDriver().switchTo().alert();
            text = alert.getText().toLowerCase();
            logger.info("Alert text: " + text);
            boolean locationAlert = text.contains("location");
            if ((accept && text.contains(wantedtext.toLowerCase())) || locationAlert) {
                alert.accept();
                if (!locationAlert) {
                    logger.info("Alert with text " + wantedtext + " accepted");
                } else logger.info("Location Alert accepted");
            } else {
                alert.dismiss();
                logger.info("No alert found ,text were " + text + ". The alert were dismissed ");
            }
        } catch (Exception e) {
            logger.info("No alert found");
        }
    }
}