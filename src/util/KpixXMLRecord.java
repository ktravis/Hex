package util;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author tonyj
 */
public class KpixXMLRecord extends KpixRecord {

    private final String xml;
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    KpixXMLRecord(int recordType, int recordLength, String xml) {
        super(recordType, recordLength);
        this.xml = xml;
    }

    public String getRawXML() {
        return xml;
    }

    public Document getParsedXML() throws RuntimeException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (Exception x) {
            throw new RuntimeException("Error parsing XML record",x);
        }
    }

    @Override
    public String toString() {
        return "KpixXMLRecord{" + super.toString() + ", xml=" + getParsedXML().toString()  + '}';
    }
}
