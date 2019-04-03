package com.neovia.returnOrders.pom;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import com.neovia.returnOrders.model.Devolution;
import com.neovia.returnOrders.utils.WaitForPageToLoad;

/**
 * The Class ReceivePart.
 */
public class ReceivePart {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(ReceivePart.class);

	/** The driver. */
	private WebDriver driver;

	/** The base URL. */
	private static String baseURL = "https://spl.dhl.com";

	/** The customer. */
	@FindBy(how = How.NAME, using = "searchPartPanel:customer:customer")
	WebElement customer;

	/** The option HPE. */
	@FindBy(how = How.XPATH, using = "//select/option[2]")
	WebElement optionHPE;

	/** The option HPI. */
	@FindBy(how = How.XPATH, using = "//select/option[3]")
	WebElement optionHPI;

	/** The input return order. */
	@FindBy(how = How.NAME, using = "searchPartPanel:searchRONumberContainer:searchRONumber")
	WebElement inputReturnOrder;

	/**
	 * Instantiates a new receive part.
	 *
	 * @param driver the driver
	 */
	public ReceivePart(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	/**
	 * Make return.
	 *
	 * @param devolutionList the devolution list
	 * @param properties the properties
	 * @return the list
	 */
	public List<String> makeReturn(List<Devolution> devolutionList, Properties properties) {

		List<String> palletList = new ArrayList<>();
		boolean retryOrder = false;
		for (int i = 0; i < devolutionList.size(); i++) {

			String pallet = "";
			String[] token = null;
			//WaitForPageToLoad.waitForAlert(driver, 500);
			WaitForPageToLoad.waitForLoad(driver);
			WebElement error404 = null;
			try {
				// CONTROLAMOS EL ERROR 404
				error404 = driver.findElement(By.xpath("//h1"));
				if (error404.getText().contains("Estado HTTP 404 - /xdock/app/returnorder")) {
					LOGGER.error(
							"A 404 error has occurred, redirecting to the page : https://spl.dhl.com/xdock/app/returnorder/");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					WaitForPageToLoad.waitForAlert(driver, 500);
				}
			} catch (NoSuchElementException e) {
				// CONTINUAMOS CON EL PROCESO
			} 

			try {
				WebElement logout = driver.findElement(By.xpath("//h2"));
				if (logout.getText().contains("Cierre") || logout.getText().contains("Logout")) {
					// NOS VOLVEMOS A LOGEAR
					LOGGER.error("The session has been closed. Returning to Login");
					driver.get(baseURL);
					LoginPage loginPage = new LoginPage(driver);
					loginPage.login(properties.getProperty("username"), properties.getProperty("password"));
				    WaitForPageToLoad.waitForAlert(driver, 1000);
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
				    WaitForPageToLoad.waitForAlert(driver, 500);
				}
			} catch (NoSuchElementException e) {
				// CONTINUAMOS CON EL PROCESO
			} 

			try {
				WebElement login = driver.findElement(By.xpath("//h3"));
				if (login.getText().contains("Login") || login.getText().contains("")) {
					// NOS VOLVEMOS A LOGEAR
					LOGGER.error("The session has been closed. Returning to Login");
					driver.get(baseURL);
					LoginPage loginPage = new LoginPage(driver);
					loginPage.login(properties.getProperty("username"), properties.getProperty("password"));
				    WaitForPageToLoad.waitForAlert(driver, 1000);
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
				    WaitForPageToLoad.waitForAlert(driver, 500);
				}
			} catch (NoSuchElementException e) {
				// CONTINUAMOS CON EL PROCESO
			} 

			try {
				// CONTROLAMOS EL ERROR 404
				error404 = driver.findElement(By.xpath("//h1"));
				if (error404.getText().contains("Estado HTTP 404 - /xdock/app/returnorder")) {
					LOGGER.error(
							"A 404 error has occurred, redirecting to the page : https://spl.dhl.com/xdock/app/returnorder/");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					WaitForPageToLoad.waitForAlert(driver, 500);
				}
			} catch (NoSuchElementException e) {
				// CONTINUAMOS CON EL PROCESO
			} 
			
			WaitForPageToLoad.waitForLoad(driver);

			// 1.SEARCH PART
			if ("HPE".equals(devolutionList.get(i).getCliente())) {
				clickOnElement(optionHPE);
			} else {
				clickOnElement(optionHPI);
			}
			sendText(inputReturnOrder, devolutionList.get(i).getRma());
			LOGGER.info("Processing RMA " + devolutionList.get(i).getRma());
			
			WebElement loading = driver.findElement(By.xpath("//*[@id=\"ajax-loader\"]"));
        	try {
    			WaitForPageToLoad.waitForElementToBeGone(loading, 300, driver);
			} catch (org.openqa.selenium.TimeoutException e) {
				LOGGER.error("A TimeoutException ocurred in search part. Reprocessing the RMA...");
				driver.get("https://spl.dhl.com/xdock/app/returnorder/");
				WaitForPageToLoad.waitForLoad(driver);
				i--;
				continue;
			} catch (org.openqa.selenium.StaleElementReferenceException s) {
				LOGGER.error("A StaleElementReferenceException ocurred in search part. Reprocessing the RMA...");
				driver.get("https://spl.dhl.com/xdock/app/returnorder/");
				WaitForPageToLoad.waitForLoad(driver);
				i--;
				continue;
			}		
			// COMPROBAMOS SI EXISTE LA ORDEN
			try {
				// AL ENCONTRAR EL ELEMENTO FEEDBACKPANELERROR LOGUEAMOS EL ERROR Y LA ORDEN Y
				// SEGUIMOS CON LA SIGUIENTE
				WebElement msgError = driver.findElement(By.cssSelector("div.alert-danger > div > div"));
				LOGGER.warn(msgError.getText());
				if (retryOrder) {
					token = msgError.getText().split("ID");
					pallet = token[1].trim();
					palletList.add(pallet);
					// ELIMINAMOS REPETIDOS
					palletList = palletList.stream().distinct().collect(Collectors.toList());
					retryOrder = false;
				}
			} catch (NoSuchElementException e) {
				// AL NO SALIR EL MENSAJE DE ERROR PROCESAMOS LA ORDEN
				try {
					// CONTROLAMOS SI SELECT SHIP TO LOCATION ESTA VACIO
					WebElement location = driver.findElement(By.xpath("//*[@name=\"receivePartPanel:shipToLocation\"]/option[1]"));
					if (location.getText().contains("Choose One")) {
						LOGGER.error("The RMA " + devolutionList.get(i).getRma()
								+ " does not have Ship to Location. It will not be processed.");
						driver.get("https://spl.dhl.com/xdock/app/returnorder/");
						continue;
					}

				} catch (NoSuchElementException submit) {
					LOGGER.warn("It was not possible to find element ship to location, reprocessing the RMA...");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					i--;
					continue;
				}
				// 2. SELECT PALLET - VALIDAMOS LA DEVOLUCION
				try {
					WebElement receive = driver.findElement(By.name("receivePartPanel:notes"));
					receive.sendKeys(Keys.ENTER);
				} catch (NoSuchElementException submit) {
					LOGGER.warn("It was not possible to press the Receive element, reprocessing the RMA...");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					i--;
					continue;
				}
				// ESPERAMOS A QUE SE CARGUE EL ALERT SUCCESS PARA COJER EL NUMERO DE PALLET
	        	try {
					WaitForPageToLoad.waitForElementToBeGone(loading, 300, driver);
				} catch (org.openqa.selenium.TimeoutException e2) {
					LOGGER.error("A TimeoutException ocurred in select pallet. Reprocessing the RMA...");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					WaitForPageToLoad.waitForLoad(driver);
					i--;
					continue;
				} catch (org.openqa.selenium.StaleElementReferenceException s) {
					LOGGER.error("A StaleElementReferenceException ocurred in select pallet. Reprocessing the RMA...");
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					WaitForPageToLoad.waitForLoad(driver);
					i--;
					continue;
				}
				
				WebElement msgSuccess = null;
				try {
					msgSuccess = driver.findElement(By.cssSelector("div.alert-success > div > div"));
					LOGGER.info(msgSuccess.getText());
				} catch (NoSuchElementException submit) {
					driver.get("https://spl.dhl.com/xdock/app/returnorder/");
					LOGGER.warn("The Pallet ID element could not be read, re-processing the RMA...");
					i--;
					retryOrder = true;
					continue;
				}
				// GUARDAMOS EL NUMERO DE PALLET
				token = msgSuccess.getText().split("ID");
				pallet = token[1].trim();
				palletList.add(pallet);
				// ELIMINAMOS REPETIDOS
				palletList = palletList.stream().distinct().collect(Collectors.toList());
			}
			// VOLVEMOS A 1.SEARCH PALLET
			driver.get("https://spl.dhl.com/xdock/app/returnorder/");

		}
		return palletList;
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
