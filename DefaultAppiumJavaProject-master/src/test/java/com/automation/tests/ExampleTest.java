package com.automation.tests;

import com.automation.Pages.Hompage;
import com.automation.Pages.Login;
import com.automation.Pages.Register;
import com.automation.activities.LoginActivity;
import com.automation.helpers.DefaultTestClass;
import com.automation.helpers.DriverManager;
import com.automation.helpers.DriverMode;
import org.apache.log4j.Logger;
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


public class ExampleTest extends DefaultTestClass {

    private LoginActivity loginActivity;
    public Logger logger = Logger.getLogger(this.getClass());
    private String fullPathtoFile;

    @Test
    public void ChromeTest() {
        try {
            if (!driverManager.isMobile) {
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
            driverManager.navigateTo("http://localhost:3000","driver2");
            hompage.ClosePopup("driver2");
            login.clickLogin("driver2");
            login.fillogin(register.email.get(1),"Aa123456","driver2",true);

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
                driverManager.navigateTo("http://localhost:3000","driver");

            }
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
            WebElement track = driverManager.waitUntilWithCondition ("clickable", By.id("trackButton"));
            track.click();
           // DriverManager drivermanager = new DriverManager();
            String logName = "/Users/ehudkon/Downloads/juice-shop-master/logs/access.log." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
             boolean serch = driverManager.findLog("***FROMserch***",logName);
             //  String a = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd”))
             boolean coment =  driverManager.findLog("***comment***",logName);
             boolean  user = driverManager.findLog(register.email.get(0),logName);
             boolean password = driverManager.findLog("Aa123456",logName);
            boolean tracKorders = driverManager.findLog("***trackorders***",logName);
            if (serch || coment ||user || password || tracKorders ) {
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

