package it.uniroma3.extractor._articleparser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.uniroma3.extractor.bean.WikiLanguage;
/**
 * This reader takes an XML content and extract fields of interest from it.
 * 
 * @author matteo
 *
 */
public class XMLParser {
    
    private static final Logger logger = LoggerFactory.getLogger(WikiLanguage.class);
    
    /**
     * It extracts a the wikid from the field "title" in the xml.
     * 
     * @param page
     * @return
     */
    public String extractsWikid(String page){
	return getFieldFromXmlPage(page, "title").replaceAll(" ", "_");
    }
    
    /**
     * Parse the XML and returns the content of the field given as a parameter.
     * E.g.  <title> content </title>, <id> content </id>, <ns> content </ns>
     * 
     * @param content
     * @param field
     * @return
     */
    public String getFieldFromXmlPage(String content, String field){
	String XML_START_TAG_FIELD = "<" + field + ">";
	String XML_END_TAG_FIELD = "</" + field + ">";
	int start = content.indexOf(XML_START_TAG_FIELD);
	int end = content.indexOf(XML_END_TAG_FIELD);
	if (start == -1 || end == -1 || start + XML_START_TAG_FIELD.length() > end) {
	    logger.error("Field " + field + " not available the article.");
	    return "";
	}
	return StringEscapeUtils.unescapeHtml4(content.substring(start + XML_START_TAG_FIELD.length(), end));
    }

    /**
     * Parse the XML and returns the real content, inside the "text" field.
     * We differentiate it only because of the different regex (to improve).
     * 
     * E.g.  <text xml:space="preserve"> text </text>, 
     * 
     * @param s
     * @return
     */
    public String getWikiMarkup(String s) {
	String XML_START_TAG_FIELD = "<text xml:space=\"preserve\">";
	String XML_END_TAG_FIELD = "</text>";
	// parse out actual text of article
	int textStart = s.indexOf(XML_START_TAG_FIELD);
	int textEnd = s.indexOf(XML_END_TAG_FIELD, textStart);
	if (textStart == -1 || textEnd == -1 || textStart + XML_START_TAG_FIELD.length() > textEnd) {
	    logger.error("No content available for the article.");
	    return "";
	}
	return s.substring(textStart + XML_START_TAG_FIELD.length(), textEnd);
    }

}
