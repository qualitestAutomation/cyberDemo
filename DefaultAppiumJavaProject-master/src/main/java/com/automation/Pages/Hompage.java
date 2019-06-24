package com.automation.Pages;

import com.automation.activities.DefaultActivity;
import com.automation.helpers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Hompage extends DefaultActivity {
    public Hompage(DriverManager manager) {
        super(manager);
    }

    public void ClosePopup(String  driverName) {
        WebElement Closd = manager.getCurrentDriver(driverName).findElement(By.xpath("//*[@id=\"cdk-overlay-0\"]/snack-bar-container/app-welcome-banner/div/button"));
        Closd.click();
    }
}
