package prime_numbers_reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;

public class PrimeNumbersReader {

	private final static Logger LOG = Logger.getLogger(PrimeNumbersReader.class.getName());

	public static void main(String args[]) {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("mylogging.properties"));
		} catch (SecurityException | IOException e) {
			LOG.log(Level.SEVERE, "Error during LOGGER configuration.", e);
		}
		if (args == null || args.length != 1) {
			LOG.log(Level.SEVERE, "Write the path and the name of input file as argument.");
			System.exit(0);
		}

		File inputFile = new File(args[0]);
		LOG.info("Reading input file: " + inputFile);
		if (!inputFile.exists()) {
			LOG.log(Level.SEVERE, "File " + inputFile + "doesn't exist!");
			System.exit(0);
		}

		List<Integer> importedNumbers = importNumbersFromXlsx(inputFile);
		LOG.info("Prime numbers: ");
		importedNumbers.stream().filter(number -> isPrime(number)).forEach(number -> LOG.info(number.toString()));

	}

	/**
	 * @param file .xlsx to import
	 * @return the list of the positive decimal numbes from the first sheet, column
	 *         B (without header)
	 */
	static List<Integer> importNumbersFromXlsx(File file) {
		List<Integer> numbers = new ArrayList<>();
		IOUtils.setByteArrayMaxOverride(1000000000);
		try (InputStream input = new BufferedInputStream(new FileInputStream(file));
				Workbook wb = WorkbookFactory.create(file)) {
			if (wb.getNumberOfSheets() < 1) {
				throw new IllegalStateException("Excel file " + file + " must contain at least one sheet!", null);
			}
			Sheet sheet = wb.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.rowIterator();
			// Skip the first row (header)
			if (rowIterator.hasNext()) {
				rowIterator.next();
			}
			rowIterator.forEachRemaining(row -> {
				Cell cell = row.getCell(1);
				if (cell != null) {
					Integer result = getPositiveIntValueOfCellOrNull(cell);
					if (result != null) {
						numbers.add(result);
					}
				}
			});
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error during xlsx import.", ex);
		}
		return numbers;
	}

	/**
	 * @param cell the cell from excel file to check
	 * @return integer value, only if the cell contains positive number without
	 *         decimal places or null
	 */
	private static Integer getPositiveIntValueOfCellOrNull(Cell cell) {
		try {
			switch (cell.getCellType()) {
			case STRING:
				int i = Integer.valueOf(cell.getStringCellValue().trim());
				return i > 0 ? i : null;
			case NUMERIC:
				double d = cell.getNumericCellValue();
				// only positive numbers without decimal places are allowed
				return (d % 1 == 0 && d > 0) ? (int) d : null;
			default:
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	static boolean isPrime(int number) {
		if (number <= 1)
			return false;
		if (number == 2 || number == 3)
			return true;
		for (int i = 2; i <= Math.sqrt(number); i++) {
			if (number % i == 0)
				return false;
		}
		return true;
	}
}
