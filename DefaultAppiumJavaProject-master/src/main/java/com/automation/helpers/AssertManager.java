package com.automation.helpers;

import com.relevantcodes.extentreports.LogStatus;

public class AssertManager {

    public static DriverManager driverManager;

    public static void assertTest(boolean actual, boolean expected, String successMessage, String failedMessage, String description) {
        if (actual == expected) {
            driverManager.saveScreenshot(LogStatus.PASS, description, successMessage);
        } else {
            driverManager.saveScreenshot(LogStatus.FAIL, description, failedMessage);
            driverManager.htmlReporter.clickFailed = true;
        }
    }
}
