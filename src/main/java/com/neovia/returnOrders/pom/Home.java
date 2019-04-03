package com.neovia.returnOrders.pom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

import com.neovia.returnOrders.utils.WaitForPageToLoad;

/**
 * The Class Home.
 */
public class Home {
	
	/** The driver. */
	private WebDriver driver;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(Home.class);
	
	/** The x dock. */
	@FindBy(how = How.XPATH, using = "//*[@id=\"nav-bar\"]/span[8]/a")
	WebElement xDock;
    
    /**
     * Instantiates a new home.
     *
     * @param driver the driver
     */
    public Home(WebDriver driver) {
    	this.driver = driver;
    	PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to receive part.
     */
    public void navigateToReceivePart() {
    	WaitForPageToLoad.waitForAlert(driver,500);
    	clickOnElement(xDock);
    	driver.get("https://spl.dhl.com/xdock/app/returnorder/");
    	
    	try {
        	// CONTROLAMOS EL ERROR 404
        	WebElement error404 = driver.findElement(By.xpath("//h1"));
            if(error404.getText().contains("Estado HTTP 404 - /xdock/app/returnorder")) {
				LOGGER.error("A 404 error has occurred, redirecting to the page : https://spl.dhl.com/xdock/app/returnorder/");
            	driver.get("https://spl.dhl.com/xdock/app/returnorder/");
            	WaitForPageToLoad.waitForAlert(driver,500);
            }
		} catch (NoSuchElementException e) {
			// CONTINUAMOS CON EL PROCESO
		}
    }
    
    /**
     * Click on element.
     *
     * @param element the element
     */
    public void clickOnElement(WebElement element){
        element.click();
    }

}
