package it.uniroma3.bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
/**
 * 
 * 
 * @author matteo
 *
 */
public class WikiLanguage {
    private String code;
    private Properties properties;
    private static final String SEPARATOR = ",";

    /**
     * 
     * @param lang
     */
    public WikiLanguage(String code, String langProperties) {
	properties = new Properties();
	try {
	    
	    this.code = code;
	    properties.load(new FileInputStream(langProperties));

	} catch (IOException e) {
	    System.out.println("Language not found.");
	    e.printStackTrace();
	    System.exit(-1);
	}

    }


    /**
     * 
     * @param key
     * @return
     */
    private List<String> getValues(String key) {
	String val = properties.getProperty(key);
	if (val == null)
	    return Collections.emptyList();
	List<String> values = new ArrayList<String>();
	if (!val.contains(SEPARATOR))
	    values.add(val);
	else{
	    @SuppressWarnings("resource")
	    Scanner scanner = new Scanner(val).useDelimiter(SEPARATOR);
	    while (scanner.hasNext())
		values.add(scanner.next());
	    scanner.close();
	}
	return values;
    }

    /**
     * 
     * @return
     */
    public List<String> getFooterIdentifiers() {
	List<String> footerIdentifiers = getValues("see_also");
	footerIdentifiers.addAll(getValues("references"));
	footerIdentifiers.addAll(getValues("further_reading"));
	footerIdentifiers.addAll(getValues("external_links"));
	footerIdentifiers.addAll(getValues("related_pages"));
	footerIdentifiers.addAll(getValues("notes"));
	return footerIdentifiers;
    }

    /**
     * 
     * @return
     */
    public List<String> getRedirectIdentifiers() {
	return getValues("redirect");
    }
    
    /**
     * 
     * @return
     */
    public List<String> getDayArticleIdentifiers() {
	return getValues("day_article");
    }

    /**
     * 
     * @return
     */
    public List<String> getDisambiguationIdentifiers() {
	return getValues("disambiguation");
    }

    /**
     * 
     * @return
     */
    public List<String> getDisambiguationTextIdentifiers() {
	return getValues("disambiguation_text");
    }

    /**
     * 
     * @return
     */
    public List<String> getCategoryIdentifiers() {
	return getValues("category");
    }

    /**
     * 
     * @return
     */
    public List<String> getTemplateIdentifiers() {
	return getValues("template");
    }

    /**
     * 
     * @return
     */
    public List<String> getDiscussionIdentifiers() {
	return getValues("discussion");
    }

    
    /**
     * 
     * @return
     */
    public List<String> getListIdentifiers() {
	return getValues("list");
    }

    /**
     * 
     * @return
     */
    public List<String> getImageIdentifiers() {
	return getValues("image");
    }

    /**
     * 
     * @return
     */
    public List<String> getPortalIdentifiers() {
	return getValues("portal");
    }

    /**
     * 
     * @return
     */
    public List<String> getFileIdentifiers() {
	return getValues("file");
    }

    /**
     * 
     * @return
     */
    public List<String> getHelpIdentifiers() {
	return getValues("help");
    }


    /**
     * 
     * @return
     */
    public List<String> getIdentifiers(String key) {
	List<String> identifiers = getValues(key);
	return identifiers;
    }


    /**
     * @return the code
     */
    public String getCode() {
	return code;
    }

}
