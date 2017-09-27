package it.uniroma3.main.pipeline.articleparser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;
import it.uniroma3.main.bean.WikiArticle;
import it.uniroma3.main.util.inout.TSVReader;

/**
 * 
 * 
 * 
 * 
 * @author matteo
 *
 */
public class MarkupParser {

    // this is a list of all the wikid that we do not want to highlight as entities
    private Set<String> blacklist_wikilinks;	

    // this is a list of all the anchor texts that we do not want to highlight as entities
    private Set<String> blacklist_names;

    /**
     * Initalize all the lists at the creation.ß
     */
    public MarkupParser(){
	this.blacklist_wikilinks = new HashSet<String>();
	this.blacklist_wikilinks.addAll(TSVReader.getLines2Set(Configuration.getCurrenciesList()));
	this.blacklist_wikilinks.addAll(TSVReader.getLines2Set(Configuration.getProfessionsList()));
	this.blacklist_names = new HashSet<String>();
	this.blacklist_names.addAll(TSVReader.getLines2Set(Configuration.getNationalitiesList()));
    }

    /**
     * Detect wikilinks that contain template in the anchor text and remove them.
     * 
     * For example, wikilinks such as:
     * 		[[Mount_Rushmore|{{template}} pass mount]]
     * 
     * become:
     * 		{{template}} pass mount
     * 
     * In case the rendered name is null, or it is only a special character, we 
     * remove it keep only the wikid in the wikilink.
     * 
     * @param originalText
     * @return
     */
    private String removeTemplateWikilinks(String originalText){
	String specialCharacters = "^[" + "0-9/@#!-*\\$%^'&._+={}()" + "]+$" ;

	Pattern TEMPLATEWIKILINK = Pattern.compile("\\[\\[+" + "([^\\]|\\|]+)\\|" + "('')?([^\\{\\]]*\\{\\{[^\\}]*\\}\\}[^\\]]*)?('')?" + "\\]\\]+");
	Matcher m = TEMPLATEWIKILINK.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	String rendered;
	while(m.find()){
	    if (m.group(3) != null && !m.group(3).matches(specialCharacters)){
		if (m.group(2) != null && m.group(4) != null){
		    rendered = m.group(2) + m.group(3) + m.group(4);
		}else{
		    rendered = m.group(3);
		}
		m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
	    }else if (m.group(1) != null){
		m.appendReplacement(cleanText, Matcher.quoteReplacement("[[" + m.group(1) + "]]"));
	    }
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * Detect wrong marked wikilinks that were originally rendered with a template
     * but we remove them in a previous step.
     * 
     * For example, wikilinks such as:
     * 		[[Mount_Rushmore#Height|{{template}}]]
     * 
     * become, after the cleaning step:
     * 		[[Mount_Rushmore#Height|]]
     * 
     * @param originalText
     * @return
     */
    public String cleanEmptyTemplateWikilinks(String originalText){
	Pattern EMPTYWIKILINK = Pattern.compile("\\[\\[+" + "([^\\]]+)\\|" + "\\]\\]+");
	Matcher m = EMPTYWIKILINK.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	while(m.find()){
	    String toRender = m.group(1).replaceAll("_", " ").replaceAll("#", " ");
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(toRender));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * Remove any wikilink from the input markup text.
     * 
     * e.g. [[any wikilink|wikilinkAnchorText]] -> wikilinkAnchorText
     * 
     * @param originalText
     * @return
     */
    public String cleanAllWikilinks(String originalText) {
	Pattern ENTITY = Pattern.compile("('')?" + "\\[\\[+" + "([^\\]\\|]*\\|)?" + "('')?([^\\]]*?)('')?" + "\\]\\]+" + "('')?");
	Matcher m = ENTITY.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	String rendered;
	while(m.find()){
	    if (m.group(3) != null && m.group(5) != null){
		rendered = m.group(3) + m.group(4) + m.group(5);
	    }else if ( m.group(1) != null && m.group(6) != null){
		rendered = m.group(1) + m.group(4) + m.group(6);
	    }else{
		rendered = m.group(4);
	    }
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * 
     * @param originalText
     * @return
     */
    public String removeCommonSenseWikilinks(String originalText){
	String noCommonSenseWikilink = removeListofWikilinks(originalText);
	noCommonSenseWikilink = removeTemplateWikilinks(noCommonSenseWikilink);
	noCommonSenseWikilink = removeLowerCaseWikilinks(noCommonSenseWikilink);
	return noCommonSenseWikilink;
    }


    /**
     * Remove only commonsense wikilinks.
     * We chose commonsense wikilinks using the following heuristic:
     * 
     * 		none upper case characters in the anchor text. 
     * 
     * So the following will be not commonsense wikilinks:
     *  - iPhone
     *  - Real Madrid
     *  - etc.
     * 
     *  e.g. [[multinational corporation|multinational]] -> multinational
     * 
     * @param originalText
     * @return
     */
    private String removeLowerCaseWikilinks(String originalText){
	Pattern COMMONSENSE = Pattern.compile("\\[\\[+" + "([^\\]]*\\|)?" + "('')?" + "([a-z][^A-Z].*?)" + "('')?" + "\\]\\]+");
	Matcher m = COMMONSENSE.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	String rendered;
	while(m.find()){
	    if (m.group(2) != null && m.group(4) != null){
		rendered = m.group(2) + m.group(3) + m.group(4);
	    }else{
		rendered = m.group(3);
	    }
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }



    /**
     * 
     * @param originalText
     * @return
     */
    private String removeListofWikilinks(String originalText){
	String listIdentifier = Lector.getWikiLang().getListIdentifiers().get(0).replaceAll("_",  "");
	Pattern LISTOF = Pattern.compile("\\[\\[+" + "(" + listIdentifier+ "[^|]*+\\|)" + "('')?" + "([^]]+?)" + "('')?" + "\\]\\]++");
	Matcher m = LISTOF.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	String rendered;
	while(m.find()){
	    if (m.group(2) != null && m.group(4) != null){
		rendered = m.group(2) + m.group(3) + m.group(4);
	    }else{
		rendered = m.group(3);
	    }
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * Harvest all the wiki-links from the original article and replace them with 
     * a specific SE-ORG tag. At the same time, it assigns the wikilinks to the article.
     * 
     * It:
     *  - solve the redirects
     *  - solve the blank-list entities
     *  
     * 	    [[Byzantine Empire|Byzantines]]  -->   SE-ORG<Byzantines>
     * 
     * Note that:
     *  - we eliminate paragraphs wikilinks. For example:
     * 	    [[Byzantine Empire#History|Byzantines]]  -->   SE-ORG<Byzantines>
     * 
     * - we eliminate only numbers wikilinks. For example:
     * 	    [[Fifa Word Cup#2002 Korea Japan|2000]]  -->   2000
     * 
     * @param originalText
     * @param article
     * @return
     */
    public String harvestAllWikilinks(String originalText, WikiArticle article) {
	Map<String, Set<String>> wikilinks = new HashMap<String, Set<String>>();
	/*
	 * We need to use some *strong* filtering here to avoid considering strange cases like
	 * the wiki links between ")" and "Mahavira" in the article: en.wikipedia.org/wiki/Gautama_Buddha 
	 * We adopt this heuristic: if the rendered entity has only special characters we skip it.
	 * We use the following regex to express special character that we do not want alone.
	 * We also skip wikiliks composed by only numbers.
	 */
	String specialCharacters = "^[" + "0-9/@#!-*\\$%^'&._+={}()" + "]+$" ;

	/*
	 * The following regex captures all the Wikilinks (after removed common-sense ones, see above)
	 * Named entities wikilinks can be sorrouned by '', which can stay inside square brackets or outside.
	 */
	Pattern ENTITY = Pattern.compile("('')?" + "\\[\\[+" + "([^\\]\\|]*\\|)?" + "('')?([^\\]]*?)('')?" + "\\]\\]+" + "('')?");
	Matcher m = ENTITY.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();

	/*
	 * For each matching ...
	 */
	String wikid;
	String rendered;
	while(m.find()){
	    if (m.group(2) != null){
		wikid = m.group(2).replaceAll(" ", "_").substring(0, m.group(2).length()-1).replaceAll("#[^|]+", "");
		if ( m.group(3) != null && m.group(5) != null){
		    rendered = m.group(3) + m.group(4) + m.group(5);
		}else if ( m.group(1) != null && m.group(6) != null){
		    rendered = m.group(1) + m.group(4) + m.group(6);
		}else{
		    rendered = m.group(4);
		}
	    }else{
		rendered = m.group(4);
		wikid = m.group(4).replaceAll(" ", "_").replaceAll("#[^\\]]+", "");
	    }

	    String categoryIdentifier = Lector.getWikiLang().getCategoryIdentifiers().get(0);
	    if (!wikid.startsWith(categoryIdentifier+":") && !rendered.startsWith(categoryIdentifier+":")){
		if (Configuration.solveRedirect())
		    wikid = Lector.getDBPedia().getRedirect(wikid);

		/*
		 * eliminate blacklisted entities
		 * nationalities, currencies, professions 
		 */
		if (blacklist_wikilinks.contains(wikid) || blacklist_names.contains(rendered)){
		    m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));

		}else{
		    if (!rendered.matches(specialCharacters) && !rendered.isEmpty()){
			if (!wikilinks.containsKey(rendered))
			    wikilinks.put(rendered, new HashSet<String>());
			// we quote it, for later
			wikilinks.get(rendered).add("<" + Matcher.quoteReplacement("SE-AUG<" + wikid +  ">") + ">");
			m.appendReplacement(cleanText, "<" + Matcher.quoteReplacement("SE-ORG<" + wikid + ">") + ">");
		    }else{
			m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
		    }
		}
	    }
	}

	m.appendTail(cleanText);
	article.addWikilinks(wikilinks);

	return cleanText.toString();
    }

    /**
     * Detect all the wikilinks and normalize them in text.
     * 
     * E.g.:  .
     * 		.. artist SE-ORG<Britney_Spears> by SE-ORG<JIVE_Records>.
     * became: 
     * 		... artist Britney Spears by JIVE Records.
     * 
     * @param originalText
     * @return
     */
    public String removeAllWikilinks(String originalText) {
	Pattern WIKILINK = Pattern.compile("<[A-Z-]+" + "<([^>]*?)>>");
	Matcher m = WIKILINK.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	while(m.find()){
	    String renderedName = m.group(1).replaceAll("_", " ");
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(renderedName));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "de");
	Lector.init("FE");

	MarkupParser mp = new MarkupParser();

	String text = "Obwohl der etymologische Ursprung des Wortes arabisch ist, entstanden die ersten "
		+ "Algorithmen im [[Antikes_Griechenland|antiken Griechenland]]. Zu den wichtigsten "
		+ "Beispielen gehören das [[Sieb des Eratosthenes]] zum Auffinden von [[Primzahlen]], welches "
		+ "im Buch ''Einführung in die Arithmetik'' von [[Nikomachos_von_Gerasa|Nikomachos]] beschrieben "
		+ "wurde und der [[Euklidischer_Algorithmus|euklidische Algorithmus]] zum Berechnen des "
		+ "[[Größter gemeinsamer Teiler|größten gemeinsamen Teilers]] zweier [[natürliche Zahl|natürlicher Zahlen]] "
		+ "aus dem Werk „[[Elemente_(Euklid)|die Elemente]]“. Einer der ältesten Algorithmen, die sich mit einer "
		+ "[[reelle Zahl|reellen Zahl]] beschäftigen, ist der [[Auslöschung (numerische Mathematik)#Beispiel: "
		+ "Algorithmus des Archimedes zur Kreiszahlberechnung|Algorithmus des Archimedes]] zur Approximation von "
		+ "[[Kreiszahl|]], was zugleich auch eines der ältesten [[numerische Mathematik|numerischen Verfahren]] ist.";

	System.out.println(mp.harvestAllWikilinks(text, WikiArticle.makeDummyArticle()));
	
    }
}
