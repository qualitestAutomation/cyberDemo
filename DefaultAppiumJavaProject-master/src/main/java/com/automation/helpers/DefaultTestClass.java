package com.automation.helpers;

import com.relevantcodes.extentreports.ExtentTestInterruptedException;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public class DefaultTestClass {

    public DriverManager driverManager = new DriverManager();
    public ExtentTestInterruptedException testexception;
    public Logger logger = Logger.getLogger(this.getClass());


    @AfterClass()
    public void afterClass() {
        System.out.println("closing the report(AfterClass)" + Thread.currentThread().getName());

    }

    @BeforeMethod()
    public void beforeMethod(Method method) throws JSONException {
        logger.info("beforeMethod started on thread " + Thread.currentThread().getName());
        driverManager.startReportFile(method.getName(), "Default");
        driverManager.startTest("Appium template", method.getName(), "Functional test");

        System.setProperty("Mode",DriverMode.WEB.toString());
       // System.setProperty("udid","ad0617024472b30b01");*/


        String currentMode = System.getProperty("Mode");
        DriverMode mode = null;
        switch (currentMode.toLowerCase()) {
            case "android":
                mode = DriverMode.ANDROID;
                driverManager.isMobile = true;
                break;
            case "web":
                mode = DriverMode.WEB;
                break;
            case "ios":
                mode = DriverMode.IOS;
                driverManager.isMobile = true;
                break;
        }
        String udid = System.getProperty("udid");
        Thread.currentThread().setName("udid:" + udid);
        driverManager.startDriver(mode, udid,"driver");

    }


    @AfterMethod()
    public void closeAppiumSession() {
        driverManager.closeDriver(driverManager.driver1);
        driverManager.closeDriver(driverManager.driver);
        driverManager.closeDriver(driverManager.driver2);
        driverManager.closeNode();
        driverManager.closeTest();
        driverManager.closeTestReport();
        System.out.println("quited the driver on thread: " + Thread.currentThread().getName());
    }

    public void finelizeTest() {
        System.out.println("finelizeTest on thread " + Thread.currentThread().getName());

        if (driverManager.htmlReporter.clickFailed) {
            System.out.println("Click failed(finelizeTest) on thread: " + Thread.currentThread().getName());
            throw new RuntimeException("test failed");
        } else {
            driverManager.htmlReporter.saveScreenshot(LogStatus.INFO, "Test finished", "Done");
            System.out.println("Done on thread: " + Thread.currentThread().getName());
        }
    }


}
