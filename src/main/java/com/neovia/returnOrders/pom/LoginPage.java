package com.neovia.returnOrders.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

/**
 * The Class LoginPage.
 */
public class LoginPage {
	
	/** The driver. */
	private WebDriver driver;
	
    /** The username driver. */
    @FindBy(how = How.NAME, using = "username")
	WebElement usernameDriver;
    
    /** The password drive. */
    @FindBy(how = How.NAME, using = "password")
	WebElement passwordDrive;
    
    /** The button. */
    @FindBy(how = How.ID, using = "OKButton")
	WebElement button;
	
    /**
     * Instantiates a new login page.
     *
     * @param driver the driver
     */
    public LoginPage(WebDriver driver) {
    	this.driver = driver;
    	PageFactory.initElements(driver, this);
    }
    
    /**
     * Login.
     *
     * @param username the username
     * @param password the password
     */
    public void login(String username ,String password){
    	
            sendText(usernameDriver,username);
            sendText(passwordDrive,password);
            clickOnElement(button);
    }
    
    /**
     * Click on element.
     *
     * @param element the element
     */
    public void clickOnElement(WebElement element){
        element.click();
    }
    
    /**
     * Send text.
     *
     * @param element the element
     * @param text the text
     */
    public void sendText(WebElement element,String text){
        element.clear();
        element.sendKeys(text);
    }
	
    
}
