package com.automation.Pages;

import com.automation.activities.DefaultActivity;
import com.automation.helpers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Register extends DefaultActivity {
  public List<String> email = new ArrayList();
  private int currentIndex = 0;
    public Register(DriverManager manager) {
        super(manager);

    }

    public void regg( String  driverName) {
        email.add("asa" + new Date().getTime() + "@gmail.com");
        WebElement Email = manager.getCurrentDriver(driverName).findElement(By.id("emailControl"));
        Email.sendKeys(email.get(currentIndex++));
        WebElement Pasword = manager.getCurrentDriver(driverName).findElement(By.id("passwordControl"));
        Pasword.sendKeys("Aa123456");
        WebElement Rpassword = manager.getCurrentDriver(driverName).findElement(By.id("repeatPasswordControl"));
        Rpassword.sendKeys("Aa123456");
        WebElement Errow = manager.getCurrentDriver(driverName).findElement(By.id("mat-select-3"));
        Errow.click();
        WebElement Barhday = manager.getCurrentDriver(driverName).findElement(By.id("mat-option-78"));
        Barhday.click();
        WebElement Unser = manager.getCurrentDriver(driverName).findElement(By.id("securityAnswerControl"));
        Unser.sendKeys("300749");
        WebElement Register = manager.getCurrentDriver(driverName).findElement(By.id("registerButton"));
        System.out.println(email);

        Register.click();
    }
}
