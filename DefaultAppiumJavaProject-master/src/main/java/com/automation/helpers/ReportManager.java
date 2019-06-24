package com.automation.helpers;

import com.fasterxml.uuid.Generators;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.relevantcodes.extentreports.model.Test;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elhanan on 8/16/17.
 */
public class ReportManager {
    private ExtentReports extent;
    private ExtentTest test;
    static final AtomicInteger lock = new AtomicInteger();
    private static UUID udid;
    private boolean reportFileClosed = false;
    private WebDriver screenShotDriver;
    private boolean testClosed = false;
    public boolean clickFailed;
    public Logger logger = Logger.getLogger(this.getClass());


    public ExtentReports getReport() {
        return this.extent;
    }

    public Test getTest() {
        return (Test) this.test.getTest();
    }

    public ExtentTest getExtentTest() {
        return this.test;
    }

    public void closeTestReport() {
        if (!reportFileClosed) {
            System.out.println("closeing Test Report file and test");
            if (!testClosed) {
                extent.endTest(test);
                testClosed = true;
            }
            extent.flush();
            extent.close();
            reportFileClosed = true;
        }
    }

    public void setScreenshotDriver(WebDriver driver) {
        this.screenShotDriver = driver;

    }

    public void startReportFile(String phoneDir, String suiteDir) {
        extent = new ExtentReports(System.getProperty("user.dir").replace(".idea/modules", "") + "/test-output/" + suiteDir + "_" + phoneDir + "_ExtentScreenshot.html", true);
        extent.addSystemInfo("Environment", "QA-Sanity"); //It will provide Execution Machine Information
    }

    public void saveScreenshot(WebDriver driver, LogStatus logStatus, String stepName, String details) {
        synchronized (lock) {
            String screenShotPath = null;
            try {
                if (driver != null) {
                    Thread.sleep(500);
                    screenShotPath = capture(driver);
                }
            } catch (Exception e) {
                e.printStackTrace();
                test.log(LogStatus.FAIL, stepName, details);
                return;
            }

            if (driver != null) {
                test.log(logStatus, stepName, details + test.addScreenCapture(screenShotPath));
            } else {
                test.log(logStatus, stepName, details);

            }
        }
    }

    public void saveScreenshot(LogStatus logStatus, String stepName, String details) {
        synchronized (lock) {
            test.log(logStatus, stepName, details);
        }
    }

    private String capture(WebDriver driver) throws IOException, InterruptedException {

        udid = Generators.timeBasedGenerator().generate();
        Thread.sleep(500);
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source;
        source = ts.getScreenshotAs(OutputType.FILE);
        String dest = System.getProperty("user.dir").replace("/.idea/modules", "") + "/test-output/Screenshots/" + udid + ".png";
        String relativeDest = "Screenshots/" + udid + ".png";

        File destination = new File(dest);
        FileUtils.copyFile(source, destination);

        return relativeDest;
    }

    public void startTest(String suiteName, String methodeName, String category) {
        test = extent.startTest((suiteName + " :: " + methodeName), methodeName); //Test Case Start Here
        test.assignAuthor("Elhanan Kon");
        test.assignCategory(category);
        testClosed = false;
        clickFailed = false;
        System.out.println("Test " + test.getTest().getName() + " were started");
    }

    public void closeTest() {
        if (!testClosed) {
            extent.endTest(test);
            testClosed = true;
            System.out.println("Test " + test.getTest().getName() + " were closed");
        }
    }

    public void reportException(Exception e) {
        reportException(e, true);
    }

    public void reportException(Exception e, boolean throwException) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        String message = errors.toString().substring(0, errors.toString().indexOf("("));
        int elementInfoIndex = errors.toString().indexOf("Element info");
        String elementInfo = "";
        if (elementInfoIndex > -1)
            elementInfo = errors.toString().substring(elementInfoIndex, errors.toString().indexOf("at sun.reflect.NativeConstructorAccessorImpl"));
        else {
            int elementExceptionIndex = errors.toString().indexOf("at sun.reflect.NativeMethodAccessorImpl");
            elementInfo = errors.toString().substring(0, elementExceptionIndex);
        }
        System.out.println("reportException Thread: " + Thread.currentThread().getName());
        saveScreenshot(LogStatus.FAIL, message, "Exception Info:" + elementInfo);
        if (throwException) {
            throw new RuntimeException(message);
        }
    }

}
