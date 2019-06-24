package com.automation.activities;

import com.automation.helpers.DriverManager;
import com.automation.helpers.DriverMode;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoginActivity extends DefaultActivity {
    public Logger logger = Logger.getLogger(this.getClass());

    public LoginActivity(DriverManager manager) {
        super(manager);
    }

    public void login(String userName, String password) {
        WebElement usernameinput = manager.getCurrentDriver().findElement(getuserNameLocator());
        usernameinput.sendKeys(userName);

        WebElement passwordinput = manager.getCurrentDriver().findElement(getPasswordLocator());
        passwordinput.sendKeys(password);

        clickConnectBTN();
    }

    private void clickConnectBTN() {
        if (manager.isMobile) {
            manager.clickElement(manager.getCurrentDriver().findElements(getConectBtnLocator()).get(1), "Connect");
        } else {
            WebElement connectBtn = manager.getCurrentDriver().findElement(getConectBtnLocator());
            connectBtn.click();
            manager.saveScreenshot(LogStatus.PASS, "Clicked on " + "Connect button" + " element", "Click were succeeded");
        }
    }

    private By getuserNameLocator() {
        return manager.isMobile ? By.xpath("//android.widget.EditText[contains(@text,'Username')]") : By.id("username");
    }

    private By getuserNameLocator(DriverMode mode) {
        By by = null;
        switch (mode) {
            case IOS:
                by = By.xpath("//android.widget.EditText[contains(@text,'Username')]");
                break;
            case ANDROID:
                by = By.xpath("//android.widget.EditText[contains(@text,'Usrname')]");
                break;
            case WEB:
                by = By.id("username");
                break;
        }
        return by;
    }

    private By getPasswordLocator() {
        return manager.isMobile ? By.xpath("//android.widget.EditText[contains(@text,'Password')]") : By.id("password");
    }

    private By getConectBtnLocator() {
        return manager.isMobile ? By.xpath("//android.widget.Button[contains(@text,'SIGN IN')]") : By.xpath("//input[@type = 'submit']");
    }
}
