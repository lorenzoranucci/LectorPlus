package it.uniroma3.main;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
import it.uniroma3.extractor.pipeline.Statistics;
import it.uniroma3.extractor.util.io.XMLReader;
import it.uniroma3.model.extraction.FactsExtractor;
import it.uniroma3.model.extraction.FactsExtractor.ModelType;
import it.uniroma3.model.model.Model.PhraseType;

public class CompletePipeline {

    private Statistics stats;
    private XMLReader inputReader;

    /**
     * 
     * @param configFile
     */
    public CompletePipeline(String inputFile){
	System.out.println("\n**** COMPLETE PIPELINE ****");
	this.stats = new Statistics();
	this.inputReader = new XMLReader(inputFile);
    }

    /**
     * 
     * @param totArticle
     * @param chunckSize
     */
    public void runPipeline(int totArticle, int chunckSize){
	List<String> lines;
	int cont = 0;

	while (!(lines = inputReader.nextChunk(chunckSize)).isEmpty()
		&& cont < totArticle) {	    
	    System.out.print("Running next: " + lines.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    cont += lines.size();

	    lines.parallelStream()
	    .map(s -> Lector.getWikiParser().createArticleFromXml(s))
	    .map(s -> stats.addArticleToStats(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .map(s -> Lector.getEntitiesFinder().increaseEvidence(s))
	    .map(s -> Lector.getEntitiesTagger().augmentEvidence(s))
	    .forEach(s -> Lector.getTriplifier().extractTriples(s));

	    Lector.getTriplifier().updateBlock();
	    Lector.dischargePerThreadDBPS();
	    
	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    lines.clear();
	}
	System.out.println("************\nProcessed articles:\n" + stats.printStats());
	inputReader.closeBuffer();
    }
    
    /**
     * 
     */
    public void extractNovelFacts(){
	FactsExtractor extractor = new FactsExtractor();
<<<<<<< HEAD
	extractor.setModelForEvaluation(ModelType.TextExtChallenge, 
=======
	extractor.setModelForEvaluation(
		ModelType.TextExtChallenge, 
>>>>>>> c23b47b55d31d51c449198b315f736c581c07ca3
		"labeled_triples", 
		Configuration.getMinF(), 
		Configuration.getTopK(), 
		PhraseType.TYPED_PHRASES);
	extractor.run();
    }

}
