package com.neovia.returnOrders;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.neovia.returnOrders.model.Devolution;
import com.neovia.returnOrders.pom.Home;
import com.neovia.returnOrders.pom.LoginPage;
import com.neovia.returnOrders.pom.PalletList;
import com.neovia.returnOrders.pom.ReceivePart;
import com.neovia.returnOrders.utils.ReadExcel;
import com.neovia.returnOrders.utils.WaitForPageToLoad;

/**
 * Created by Victor Ulises Bedmar Lam on 04/04/2019.
 * DNI: 03130651Y
 */
/**
 * The Class ReturnOrdersApplication.
 */
public class ReturnOrdersApplication {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(ReturnOrdersApplication.class);

	/** The pathdriver. */
	private static String PATHDRIVER = "drivers/";

	/** The base URL. */
	private static String baseURL = "https://spl.dhl.com";

	/** The driver. */
	static WebDriver driver;

	/** The properties. */
	public static Properties properties;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		setup();
		signUp();
	}

	/**
	 * Setup.
	 */
	public static void setup() {

		System.setProperty("webdriver.chrome.driver", PATHDRIVER + "chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("start-maximized");
		options.addArguments("--incognito");
		driver = new ChromeDriver(options);
        
		properties = new Properties();
		File config = new File("config.properties");
		// CARGAMOS EL PROPERTIES
		if (config.exists()) {
			try (InputStream ins = new FileInputStream(config)) {
				properties.load(ins);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		} else {
			LOGGER.error("The file does not exist in the specified path");
			System.exit(1);
		}
	}

	/**
	 * Sign up.
	 */
	public static void signUp() {

	
    	LOGGER.info("--- [ INITIALIZING THE TRACE OF THE ORDERS RETURN PROCESS ] ---");
		driver.get(baseURL);
		WaitForPageToLoad.waitForAlert(driver, 500);

		// CREAMOS LOS OBJETOS DE LAS CLASES PAGE
		LoginPage loginPage = new LoginPage(driver);
		Home home = new Home(driver);
		ReceivePart receivePart = new ReceivePart(driver);
		PalletList palletList = new PalletList(driver);
		// NOS LOGEAMOS
		LOGGER.info("Initializing access to the Login page");
		
		
		loginPage.login(properties.getProperty("username"), properties.getProperty("password"));
		// NAVEGAMOS
		LOGGER.info("Navigating to the Receive Part page");
		home.navigateToReceivePart();
		// LEEMOS EL FICHERO EXCEL
		LOGGER.info("Reading the excel file from the path " + properties.getProperty("excel.path"));
		ReadExcel lectura = new ReadExcel();
		List<Devolution> devolutionList = lectura.getDevolutionList(properties.getProperty("excel.path"));
		LOGGER.info("Total " + devolutionList.size() + " records");
		WaitForPageToLoad.waitForLoad(driver);
		// DEVOLVEMOS TODA LA LISTA DE PRODUCTOS
		LOGGER.info("Preparing the return orders");
		List<String> listToSeal = receivePart.makeReturn(devolutionList, properties);
		if (listToSeal.size() != 0) {
			// ELIMINAMOS REPETIDOS
			listToSeal = listToSeal.stream().distinct().collect(Collectors.toList());
			// PASAMOS A CERRAR LOS PALLETS(SEAL)
			LOGGER.info("Navigating to the Pallet List page");
			palletList.sealPallet(listToSeal);
		} else {
			LOGGER.warn("There are no pallets to seal.");
		}

		// ELIMINANDO FICHERO A CARPETA REMOVED
		File excel = new File(properties.getProperty("excel.path"));
		if (excel.exists()) {
			Date today = new Date();
			long timeMilli = today.getTime();
			try {
				Files.copy(Paths.get(properties.getProperty("excel.path")),
						Paths.get(properties.getProperty("removed.path") + timeMilli + "_completed.xlsx"),
						StandardCopyOption.REPLACE_EXISTING);
				excel.delete();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}

    	LOGGER.info("--- [ FINISH TRACE OF EXECUTION OF PROCESS OF RETURNS OF ORDERS ] ---");
		driver.close();
	}

}
