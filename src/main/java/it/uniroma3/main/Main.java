package it.uniroma3.main;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.main.pipeline.ArticleParser;
import it.uniroma3.main.pipeline.Complete;
import it.uniroma3.main.pipeline.EntityDetection;
import it.uniroma3.main.pipeline.FactsExtractor;
import it.uniroma3.main.pipeline.TriplesExtractor;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
/**
 * 
 * @author matteo
 *
 */
public class Main {

    /**
     * This is the complete pipeline.
     * We first initialize Lector with the components that are needed (i.e. from the pipeline)
     * and at the end we close all the connections with the databases and the spotlight (if there are).
     * 
     * @param inputPath
     */
    private static void completeInMemoryProcess(WikiLanguage lang){
	Lector.init(lang, Configuration.getPipelineSteps());
	Complete cp = new Complete(Configuration.getOriginalArticlesFile());
	cp.runPipeline(Configuration.getNumArticlesToProcess(), Configuration.getChunkSize());
	cp.extractNovelFacts();
	Lector.closeAllConnections();
    }

    /**
     * This is a pipeline in which we execute one step at a time. 
     * Need for testing.
     * 
     * @param inputPath
     */
    protected static void singleStepProcess(WikiLanguage lang){
	Lector.init(lang, Configuration.getPipelineSteps());

	if (Configuration.getPipelineSteps().contains("AP")){
	    ArticleParser ap = new ArticleParser(Configuration.getOriginalArticlesFile(), Configuration.getParsedArticlesFile());
	    ap.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("ED")){
	    EntityDetection ed = new EntityDetection(Configuration.getParsedArticlesFile(),  Configuration.getAugmentedArticlesFile());
	    ed.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("TE")){
	    TriplesExtractor te = new TriplesExtractor(Configuration.getAugmentedArticlesFile());
	    te.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("FE")){
	    FactsExtractor extractor = new FactsExtractor();
	    extractor.setModelForEvaluation(
		    ModelType.valueOf(Configuration.getLectorModelName()),
		    "labeled_triples", 
		    Configuration.getMinF(), 
		    Configuration.getTopK(),
		    Configuration.getCutOff(),
		    PhraseType.TYPED_PHRASES);
	    extractor.run();
	}

	Lector.closeAllConnections();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("language", lang);
	    System.out.println("\n===================================");
	    System.out.println("Starting a new LectorPlus execution");
	    System.out.println("===================================");
	    Configuration.printFullyDetails();
	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());

	    if (Configuration.getPipelineSteps().size() == 4 || (Configuration.getPipelineSteps().size() == 3 && !Configuration.getPipelineSteps().contains("FE")))
		completeInMemoryProcess(wikiLang);
	    else
		singleStepProcess(wikiLang);
	}
    }
}
