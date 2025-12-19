package prime_numbers_reader;

import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import prime_numbers_reader.utils.Utils;

public class PrimeNumbersContentHandler extends DefaultHandler {

	private final static Logger LOG = Logger.getLogger(PrimeNumbersContentHandler.class.getName());
	// Regex: column letters ([A-Z]+), row digits ([0-9]+)
	String regex = "([A-Z]+)([0-9]+)";
	Pattern pattern = Pattern.compile(regex);
	private SharedStrings ss;
	private boolean isCellValue;
	private String currentCellRef;
	private StringBuilder cellValue = new StringBuilder();
	private String cellType;

	public PrimeNumbersContentHandler(SharedStrings ss) {
		this.ss = ss;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		// c => start of cell
		if ("c".equals(qName)) {
			currentCellRef = atts.getValue("r"); // cell reference like A1, B2
			cellType = atts.getValue("t");
		}
		// "v" = value
		if ("v".equals(qName)) { // "v" = value
			isCellValue = true;
			cellValue.setLength(0); // reset buffer
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("v".equals(qName)) { // Print cell reference and value
			isCellValue = false;
			String[] cellRef = splitCellRef(currentCellRef);
			// Skip first line (Header)
			if ("1".equals(cellRef[1])) {
				return;
			}
			// Read B column only
			if (!"B".equals(cellRef[0])) {
				return;
			}
			String value = cellValue.toString();

			// If cell type is "s", it's a shared string index
			if ("s".equals(cellType)) {
				int idx = Integer.parseInt(value);
				value = ss.getItemAt(idx).getString();
			}
			writeValueIfIsValid(value);
		}

	}

	private void writeValueIfIsValid(String value) {
		try {
			BigInteger number = new BigInteger(value.trim());
			if (Utils.isPrime(number))
				LOG.info(number.toString());
		} catch (Exception e) {
			// Nothing to do
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isCellValue) {
			cellValue.append(ch, start, length);
		}
	}

	private String[] splitCellRef(String cellRef) {
		Matcher matcher = pattern.matcher(cellRef);

		if (matcher.matches()) {
			try {
				String column = matcher.group(1); // e.g. "AA"
				String row = matcher.group(2); // e.g. "10"
				return new String[] { column, row };
			} catch (Exception e) {
				return new String[] { "", "" };
			}
		}
		return new String[] { "", "" };
	}

}
