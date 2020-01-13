package com.neovia.returnOrders.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.neovia.returnOrders.model.Devolution;

/**
 * The Class ReadExcel.
 */
public class ReadExcel {
	
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LogManager.getLogger(ReadExcel.class);

	/**
	 * Instantiates a new read excel.
	 */
	public ReadExcel() {
	}
	/**
	 * DNI: 03130651Y
	 */

	/**
	 * Gets the devolution list.
	 *
	 * @param pathExcel the path excel
	 * @return the devolution list
	 */
	public List<Devolution> getDevolutionList(String pathExcel) {

		List<Devolution> lista = new ArrayList<>();

		FileInputStream file = null;
		try {
			file = new FileInputStream(new File(pathExcel));
		} catch (FileNotFoundException e) {
			LOGGER.error("The system can not find the file in the specified path: " + pathExcel);
		}

		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(file);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		Row row;

		// RECORREMOS TODAS LAS FILAS PARA MOSTRAR EL CONTENIDO DE CADA CELDA
		// SALTAMOS LA CABECERA
		row = rowIterator.next();
		boolean validatedRow;
		Devolution dev= null;

		while (rowIterator.hasNext()) {

			dev = new Devolution();
			row = rowIterator.next();
			validatedRow = true;
			String rma = "";
			// OBTENEMOS EL ITERATOR QUE PERMITE RECORRES TODAS LAS CELDAS DE UNA FILA
			Iterator<Cell> cellIterator = row.cellIterator();
			Cell celda;

			while (cellIterator.hasNext()) {

				celda = cellIterator.next();

				int cellNumber = celda.getColumnIndex();

				// SI EL NUMERO DE CELDA ES EL DEL RMA LO TRATAMOS PARA QUITAR EL GUION MEDIO
				if (cellNumber == 0) {

					if (celda.getCellType() == Cell.CELL_TYPE_STRING) {
						String[] token = celda.getStringCellValue().split("-");
						rma = token[0].trim();

					} else {
						LOGGER.error("Value of column " + celda.getColumnIndex() + " in row " + row.getRowNum()
						+ " is not text type");
						validatedRow = false;
					}
				}
				// SI EL NUMERO DE CELDA ES EL DEL STATUS LO TRATAMOS PARA AÑADIRLO AL RMA DE DEVOLUCION
				if (cellNumber == 1) {

					if (celda.getCellType() == Cell.CELL_TYPE_STRING) {
						rma = rma + celda.getStringCellValue().trim().toUpperCase();
						dev.setRma(rma);
					} else {
						LOGGER.error("Value of column " + celda.getColumnIndex() + " in row " + row.getRowNum()
						+ " is not text type");
						validatedRow = false;
					}
				}
				// SI EL NUMERO DE CELDA ES EL DEL CLIENTE LO AÑADIMOS AL OBJETO DEVOLUCION
				if (cellNumber == 23) {

					if (celda.getCellType() == Cell.CELL_TYPE_STRING) {
						dev.setCliente(celda.getStringCellValue().trim());
					} else {
						LOGGER.error("Value of column " + celda.getColumnIndex() + " in row " + row.getRowNum()
						+ " is not text type");
						validatedRow = false;
					}
				}

			}
			if (validatedRow) {
				lista.add(dev);

			}
		}

		return lista;

	}

}
