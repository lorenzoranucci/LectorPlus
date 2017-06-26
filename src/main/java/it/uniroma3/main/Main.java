package it.uniroma3.main;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class Main {

    /**
     * This is the complete pipeline.
     * We first initialize Lector with the componenets that are needed (i.e. from the pipeline)
     * and at the end we close all the connections with the databases and the spotlight (if there are).
     * 
     * @param inputPath
     */
    private static void completeInMemoryProcess(WikiLanguage lang){
	Lector.init(lang, Configuration.getPipelineSteps());
	CompletePipeline cp = new CompletePipeline(Configuration.getOriginalArticlesFile());
	cp.runPipeline(Configuration.getNumArticlesToProcess(), Configuration.getChunkSize());
	cp.extractNovelFacts();
	Lector.closeAllConnections();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
<<<<<<< HEAD
	String[] languages = new String[]{"en","es", "it", "de", "fr"};
=======
	String[] languages = new String[]{"it", "en", "es", "de", "fr"};
>>>>>>> c23b47b55d31d51c449198b315f736c581c07ca3
	for (String lang : languages){
	    Configuration.init(args);
	    Configuration.setParameter("language", lang);
	    //Configuration.setParameter("dataFile", "/Users/khorda/Documents/Universita/supporto-MATTEO/dbpediachallenge-lector/data");
	    Configuration.setParameter("dataFile", "/Users/matteo/Desktop/data");
	    Configuration.printDetails();
	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	    completeInMemoryProcess(wikiLang);
	}
    }
}
