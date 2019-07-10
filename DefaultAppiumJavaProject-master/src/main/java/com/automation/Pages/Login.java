package com.automation.Pages;

import com.automation.activities.DefaultActivity;
import com.automation.helpers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.automation.helpers.AssertManager.driverManager;


public class Login extends DefaultActivity {
    public Login(DriverManager manager) {
        super(manager);
    }

    public  void clickLogin(String  driverName) {
        WebElement login = manager.getCurrentDriver(driverName).findElement(By.xpath("//*[@id=\"navbarLoginButton\"]"));
        login.click();
    }
    public void clicknotregisteryet (String  driverName)
    {
        WebElement Reg = manager.getCurrentDriver(driverName).findElement(By.cssSelector("body > app-root > div > mat-sidenav-container > mat-sidenav-content > app-login > div > mat-card > a:nth-child(3)"));
        Reg.click();
    }

    public boolean validateAlreadylogin(String drivername){
        List<WebElement> logout = manager.getCurrentDriver(drivername).findElements(By.xpath("//*[@id=\"navbarLogoutButton\"]/span"));

        if (logout.size() > 0 && logout.get(0).getText().toLowerCase().contains("logout"))
        {
            System.out.println("validating logined: text: " + logout.get(0).getText());
            return true;

        }else{
            return false;
        }
    }
    
    public void fillogin(String user ,String password,String drivername,boolean checklogin) throws Exception {
        WebElement Email = manager.getCurrentDriver(drivername).findElement(By.id("email"));
        Email.sendKeys (user);

        WebElement Password = manager.getCurrentDriver(drivername).findElement(By.id("password"));
        Password.sendKeys(password);
        WebElement presslogin = manager.getCurrentDriver(drivername).findElement(By.id("loginButton"));
        System.out.println(user);
        presslogin.click();
        boolean networking = manager.validateResponseCode(1, manager.driver1);
        System.out.println("abcd");
       List<WebElement> logout = manager.getCurrentDriver(drivername).findElements(By.xpath("//*[@id=\"navbarLogoutButton\"]/span"));
       if ((logout.size() > 0)&& checklogin==true)
       {

           throw new Exception("test faild");

       }
       else  System.out.println("test passd");
    }
}
      
    
           
       
           
       
       
        
        

