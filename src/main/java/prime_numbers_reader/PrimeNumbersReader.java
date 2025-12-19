package prime_numbers_reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
			LOG.log(Level.SEVERE, "File " + inputFile + " doesn't exist!");
			System.exit(0);
		}

		LOG.info("Prime numbers: ");
		importNumbersFromXlsxAndLogPrimes(inputFile);
	}

	/**
	 * Read numbers from the first sheet, column B and log primes
	 * 
	 * @param file .xlsx to import
	 */
	static void importNumbersFromXlsxAndLogPrimes(File file) {
		try (OPCPackage pckg = OPCPackage.open(file)) {
			XSSFReader reader = new XSSFReader(pckg);
			SharedStrings ss = reader.getSharedStringsTable();
			Iterator<InputStream> sheets = reader.getSheetsData();
			ContentHandler handler = new PrimeNumbersContentHandler(ss);
			// Handle only first sheet
			if (sheets.hasNext()) {
				InputStream sheet = sheets.next();
				XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
				parser.setContentHandler(handler);
				parser.parse(new InputSource(sheet));
				sheet.close();
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error during xlsx import.", e);
		}
	}

}
