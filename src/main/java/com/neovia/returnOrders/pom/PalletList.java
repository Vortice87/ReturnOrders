package com.neovia.returnOrders.pom;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.neovia.returnOrders.utils.WaitForPageToLoad;

/**
 * The Class PalletList.
 */
public class PalletList {
	
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LogManager.getLogger(PalletList.class);

	/** The driver. */
	private WebDriver driver;

	/**
	 * Instantiates a new pallet list.
	 *
	 * @param driver the driver
	 */
	public PalletList(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	/**
	 * Seal pallet.
	 *
	 * @param palletList the pallet list
	 */
	public void sealPallet(List<String> palletList) {

	    WaitForPageToLoad.waitForAlert(driver, 500);
		// NACEGAMOS A LA PANTALLA DE LISTA DE PALETS
		driver.get("https://spl.dhl.com/xdock/app/pallets");
		
        LOGGER.info("Sealing pallets");

        for (int i = 0; i < palletList.size(); i++) {
        	LOGGER.info("Processing pallet ID " + palletList.get(i));
			WebElement palletFilter = driver.findElement(By.xpath("//th[3]/span/form/div[2]/a[2]"));
			clickOnElement(palletFilter);
		    WaitForPageToLoad.waitForAlert(driver, 500);

			WebElement inputfilter = driver.findElement(By.name("panel:filterItem:field:textField"));
			sendText(inputfilter, palletList.get(i));
			WaitForPageToLoad.waitForAlert(driver, 1500);

			try {
				WebElement seal = driver.findElement(By.xpath("//td[12]/div/button[2]"));
				clickOnElement(seal);
				// LOCALIZAMOS EL ELEMENTO LOADING
				WebElement loading = driver.findElement(By.xpath("//*[@id=\"ajax-loader\"]"));
	        	try {
	    			WaitForPageToLoad.waitForElementToBeGone(loading, 300, driver);
				} catch (org.openqa.selenium.TimeoutException e) {
					LOGGER.error("A TimeoutException ocurred in Pallet List. Reprocessing pallet ID...");
					driver.get("https://spl.dhl.com/xdock/app/pallets");
					WaitForPageToLoad.waitForLoad(driver);
					i--;
					continue;
				} catch (org.openqa.selenium.StaleElementReferenceException s) {
					LOGGER.error("A StaleElementReferenceException ocurred in Pallet List. Reprocessing pallet ID...");
					driver.get("https://spl.dhl.com/xdock/app/pallets");
					WaitForPageToLoad.waitForLoad(driver);
					i--;
					continue;
				}
				
				LOGGER.info("Pallet ID " + palletList.get(i) + " sealed");
			} catch (NoSuchElementException e) {
				LOGGER.warn("No records were found or the pallet is closed");
			} catch (ElementNotVisibleException nv) {
				LOGGER.error(nv.getMessage());
				i--;
				continue;
			} catch (WebDriverException we) {
				LOGGER.error(we.getMessage());
				i--;
				continue;
			}			
			
			WaitForPageToLoad.waitForAlert(driver, 2000);
		}
        LOGGER.info("Finished sealing process");

	}

	/**
	 * Click on element.
	 *
	 * @param element the element
	 */
	private void clickOnElement(WebElement element) {
		element.click();
	}

	/**
	 * Send text.
	 *
	 * @param element the element
	 * @param text the text
	 */
	private void sendText(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
		element.sendKeys(Keys.ENTER);
	}

}
