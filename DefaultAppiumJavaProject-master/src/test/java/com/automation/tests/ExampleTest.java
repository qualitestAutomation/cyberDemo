package com.automation.tests;

import com.automation.Pages.Hompage;
import com.automation.Pages.Login;
import com.automation.Pages.Register;
import com.automation.activities.LoginActivity;
import com.automation.helpers.DefaultTestClass;
import com.automation.helpers.DriverManager;
import com.automation.helpers.DriverMode;
import com.automation.helpers.ReportManager;
import com.google.api.client.util.DateTime;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;
import com.automation.Pages.Register;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebDriver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;


public class ExampleTest extends DefaultTestClass {

    private LoginActivity loginActivity;
    public Logger logger = Logger.getLogger(this.getClass());
    private String fullPathtoFile;

    @Test
    public void ChromeTest() {
        try {
            if (!driverManager.isMobile) {
                //driverManager.navigateTo("http://localhost:3333","driver");
                driverManager.navigateTo("http://localhost:3000","driver");
            }
            // loginActivity = new LoginActivity(driverManager);
            //loginActivity.login("jenkinsman", "jenkins1234");

            Hompage hompage = new Hompage(driverManager);
            hompage.ClosePopup("driver");
            Login login = new Login(driverManager);
            login.clickLogin("driver");
            login.clicknotregisteryet("driver");
            Register register = new Register(driverManager);
            register.regg("driver");

            driverManager.startDriver(DriverMode.WEB,"","driver1");
            //driverManager.navigateTo("http://localhost:3333","driver1");
            driverManager.navigateTo("http://localhost:3000","driver1");

            //Hompage hompage1 = new Hompage(driverManager);
            hompage.ClosePopup("driver1");
//            Login login1 = new Login(driverManager);
            login.clickLogin("driver1");
            login.clicknotregisteryet("driver1");
//            Register register1 = new Register(driverManager);
            register.regg("driver1");
            login.fillogin(register.email.get(1),"Aa123456","driver1",false);

            driverManager.startDriver(DriverMode.WEB,"","driver2");
            //driverManager.navigateTo("http://localhost:3333","driver2");
            driverManager.navigateTo("http://localhost:3000","driver2");
            hompage.ClosePopup("driver2");

            boolean logined = login.validateAlreadylogin("driver2");
            if (!logined) {
                login.clickLogin("driver2");
                login.fillogin(register.email.get(1), "Aa123456", "driver2", true);

                ChromeOptions options = new ChromeOptions();
                // add whatever extensions you need
                // for example I needed one of adding proxy, and one for blocking
                // images
                // options.addExtensions(new File(file, "proxy.zip"));
                // options.addExtensions(new File("extensions",
                // "Block-image_v1.1.crx"));

                DesiredCapabilities cap = DesiredCapabilities.chrome();
                cap.setCapability(ChromeOptions.CAPABILITY, options);

                // set performance logger
                // this sends Network.enable to chromedriver
                LoggingPreferences logPrefs = new LoggingPreferences();
                logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
                cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
                System.out.println(logPrefs + "dddd");


            }

            finelizeTest();
            // Thread.sleep(8000);

        } catch (Exception e) {
            e.printStackTrace();
            driverManager.reportException(e);
        }
    }


    @Test
    public void ChromeTest1  () {
        try {
            if (!driverManager.isMobile) {
              // driverManager.navigateTo("http://localhost:3333","driver");
                driverManager.navigateTo("http://localhost:3000","driver");

            }
            Date currentTime = new Date();
            currentTime = DateUtils.addHours(currentTime, -3);
            // loginActivity = new LoginActivity(driverManager);
            //loginActivity.login("jenkinsman", "jenkins1234");
            String driverName="driver";
            Hompage hompage = new Hompage(driverManager);
            hompage.ClosePopup("driver");
            WebElement Register = driverManager.getCurrentDriver(driverName).findElement(By.id("searchQuery"));
            Register.sendKeys("***FROMserch***");
            WebElement apple = driverManager.getCurrentDriver(driverName).findElement(By.xpath("/html/body/app-root/div/mat-sidenav-container/mat-sidenav-content/app-search-result/div/div/mat-table/mat-row[1]/mat-cell[1]/img"));
            apple.click();
            WebElement comment = driverManager.getCurrentDriver(driverName).findElement(By.id("mat-input-1"));
            comment.sendKeys("***comment***");
            WebElement sbmit = driverManager.getCurrentDriver(driverName).findElement(By.id("submitButton"));
            sbmit.click();
            WebElement closd = driverManager.getCurrentDriver(driverName).findElement(By.xpath("//*[@id=\"mat-dialog-0\"]/app-product-details/mat-dialog-content/footer/button"));
            closd.click();
            Login login = new Login(driverManager);
            login.clickLogin("driver");
            Register register = new Register(driverManager);
            login.clicknotregisteryet("driver");
            register.regg("driver");
            login.fillogin(register.email.get(0),"Aa123456","driver",false);
            WebElement userbutton = driverManager.getCurrentDriver(driverName).findElement(By.id("userMenuButton"));
            userbutton.click();
            Thread.sleep(5000);
            //WebElement trackorders = driverManager.getCurrentDriver(driverName).findElement(By.xpath("//*[@id=\"cdk-overlay-3\"]/div/div/button[4]"));
            WebElement trackorders = driverManager.waitUntilWithCondition ("clickable", By.xpath("//button[contains(text(),' Track Orders ')]"));
            driverManager.clickbyjavascript(driverManager.getCurrentDriver(),trackorders);
            WebElement order = driverManager.waitUntilWithCondition ("clickable", By.id("orderId"));
            order.sendKeys("***trackorders***");
           // order.sendKeys("\"trackorders\"");
            WebElement track = driverManager.waitUntilWithCondition ("clickable", By.id("trackButton"));
            track.click();
           // DriverManager drivermanager = new DriverManager();
            String logName = "/Users/ehudkon/Downloads/juice-shop-master/logs/access.log." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //String logName = "/home/ubuntu/Documents/juice-shop-master/logs/access.log." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

             boolean serch = driverManager.findLog("***FROMserch***",logName,currentTime);
             boolean serchurl = driverManager.findLog("%22FROMserch%22",logName,currentTime);
             boolean serchbase64 = driverManager.findLog("KioqFROMserchKioq",logName,currentTime);
             boolean serchhexa = driverManager.findLog("2a2a2aROMserch2a2a2a",logName,currentTime);
             //  String a = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd”))
             boolean coment =  driverManager.findLog("***comment***",logName,currentTime);
            boolean comenturl = driverManager.findLog("%22coment%22",logName,currentTime);
            boolean comentbase64 = driverManager.findLog("KioqcomentKioq",logName,currentTime);
            boolean comenthexa = driverManager.findLog("2a2a2acoment2a2a2a",logName,currentTime);
             boolean  user = driverManager.findLog(register.email.get(0),logName,currentTime);
            boolean userurl = driverManager.findLog("%22user%22",logName,currentTime);
            boolean userbase64 = driverManager.findLog("KioquserKioq",logName,currentTime);
            boolean userhexa = driverManager.findLog("2a2a2auser2a2a2a",logName,currentTime);
             boolean password = driverManager.findLog("Aa123456",logName,currentTime);
            boolean passwordurl = driverManager.findLog("%22password%22",logName,currentTime);
            boolean passwordbase64 = driverManager.findLog("KioqpasswordKioq",logName,currentTime);
            boolean passwordhexa = driverManager.findLog("2a2a2apassword2a2a2a",logName,currentTime);

            boolean tracKorders = driverManager.findLog("***trackorders***",logName,currentTime);
            boolean tracKordersurl = driverManager.findLog("%22trackorders%22",logName,currentTime);
            boolean tracKordersbase64 = driverManager.findLog("KioqtrackordersKioq",logName,currentTime);
            boolean tracKordershexa = driverManager.findLog("2a2a2arackorders2a2a2a",logName,currentTime);

            if (serch || coment ||user || password || tracKorders || serchurl || serchbase64 || serchhexa || userurl || userbase64 || userhexa || passwordurl || passwordbase64 || passwordhexa || tracKordersurl || tracKordersbase64 || tracKordershexa) {
                System.out.println("test passd");
            }
            else
                throw new Exception("test faild");





            finelizeTest();

            // Thread.sleep(8000);

        } catch (Exception e) {
            e.printStackTrace();
            driverManager.reportException(e);
        }
    }
}

