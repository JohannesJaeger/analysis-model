package edu.hm.hafner.analysis;

import java.io.Reader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

import static org.apache.xerces.impl.Constants.*;

/**
 * Factory for XML Parsers that prevent XML External Entity attacks. Those attacks occur when untrusted XML input
 * containing a reference to an external entity is processed by a weakly configured XML parser.
 *
 * @author Ullrich Hafner
 * @see <a href="https://owasp.org/www-project-cheat-sheets/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML
 *         External Entity Prevention Cheat Sheet</a>
 */
public class SecureXmlParserFactory {
    private static final String[] ENABLED_PROPERTIES = {
//            XERCES_FEATURE_PREFIX + DISALLOW_DOCTYPE_DECL_FEATURE,   - If this feature is activated we cannot parse any XML elements with DOCTYPE anymore
            XMLConstants.FEATURE_SECURE_PROCESSING
    };
    private static final String[] DISABLED_PROPERTIES = {
            SAX_FEATURE_PREFIX + EXTERNAL_GENERAL_ENTITIES_FEATURE,
            SAX_FEATURE_PREFIX + EXTERNAL_PARAMETER_ENTITIES_FEATURE,
            SAX_FEATURE_PREFIX + RESOLVE_DTD_URIS_FEATURE,
            SAX_FEATURE_PREFIX + USE_ENTITY_RESOLVER2_FEATURE,
            XERCES_FEATURE_PREFIX + CREATE_ENTITY_REF_NODES_FEATURE,
            XERCES_FEATURE_PREFIX + LOAD_DTD_GRAMMAR_FEATURE,
            XERCES_FEATURE_PREFIX + LOAD_EXTERNAL_DTD_FEATURE
    };

    /**
     * Creates a new instance of a {@link DocumentBuilder} that does not resolve external entities.
     *
     * @return a new instance of a {@link DocumentBuilder}
     */
    public DocumentBuilder createDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            for (String enabledProperty : ENABLED_PROPERTIES) {
                try {
                    factory.setFeature(enabledProperty, true);
                }
                catch (ParserConfigurationException ignored) {
                    // ignore and continue
                }
            }
            for (String disabledProperty : DISABLED_PROPERTIES) {
                try {
                    factory.setFeature(disabledProperty, false);
                }
                catch (ParserConfigurationException ignored) {
                    // ignore and continue
                }
            }

            return factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException exception) {
            throw new IllegalArgumentException("Can't create instance of DocumentBuilder", exception);
        }
    }

    /**
     * Creates a new instance of a {@link SAXParser} that does not resolve external entities.
     *
     * @return a new instance of a {@link SAXParser}
     */
    public SAXParser createSaxParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            configureSaxParserFacory(factory);

            return factory.newSAXParser();
        }
        catch (ParserConfigurationException | SAXException exception) {
            throw new IllegalArgumentException("Can't create instance of SAXParser", exception);
        }
    }

    /**
     * Configures a {@link SAXParserFactory} so that it does not resolve external entities.
     *
     * @param factory
     *         the facotry to configure
     */
    public void configureSaxParserFacory(final SAXParserFactory factory) {
        factory.setValidating(false);
        factory.setXIncludeAware(false);

        for (String enabledProperty : ENABLED_PROPERTIES) {
            try {
                factory.setFeature(enabledProperty, true);
            }
            catch (ParserConfigurationException | SAXException ignored) {
                // ignore and continue
            }
        }
        for (String disabledProperty : DISABLED_PROPERTIES) {
            try {
                factory.setFeature(disabledProperty, false);
            }
            catch (ParserConfigurationException | SAXException ignored) {
                // ignore and continue
            }
        }
    }

    /**
     * Creates a new instance of a {@link XMLInputFactory} that does not resolve external entities.
     *
     * @return a new instance of a {@link XMLInputFactory}
     */
    public XMLInputFactory createXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }

    /**
     * Creates a new instance of a {@link XMLStreamReader} that does not resolve external entities.
     *
     * @param reader
     *         the reader to wrap
     *
     * @return a new instance of a {@link XMLStreamReader}
     */
    public XMLStreamReader createXmlStreamReader(final Reader reader) {
        try {
            return createXmlInputFactory().createXMLStreamReader(reader);
        }
        catch (XMLStreamException exception) {
            throw new IllegalArgumentException("Can't create instance of XMLStreamReader", exception);
        }
    }
}
