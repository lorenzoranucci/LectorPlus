package it.uniroma3.main.pipeline;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.bean.WikiTriple.TType;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.io.ResultsWriterWrapper;
import it.uniroma3.extractor.util.io.ntriples.NTriplesWriter;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
import it.uniroma3.model.model.ModelBM25;
import it.uniroma3.model.model.ModelNB;
import it.uniroma3.model.model.ModelNBFilter;
import it.uniroma3.model.model.ModelNBind;
import it.uniroma3.model.model.ModelTextExt;
/**
 * 
 * @author matteo
 *
 */
public class FactsExtractor {

    private Model model;

    private NTriplesWriter writer_facts;
    private ResultsWriterWrapper writer_provenance;
    
    private NTriplesWriter writer_ontological_facts;
    private ResultsWriterWrapper writer_provenance_ontological;

    /**
     * 
     * @param model
     * @param db_write
     */
    public FactsExtractor(){
	this.writer_facts = new NTriplesWriter(Configuration.getOutputFactsFile());
	this.writer_ontological_facts = new NTriplesWriter(Configuration.getOutputOntologicalFactsFile());
	this.writer_provenance = new ResultsWriterWrapper(Configuration.getProvenanceFile());
	this.writer_provenance_ontological = new ResultsWriterWrapper(Configuration.getProvenanceOntologicalFile());
    }

    /**
     * 
     * @param type
     * @param labeled_table
     * @param minFreq
     * @param topK
     * @param typePhrase
     * @return
     */
    public void setModelForEvaluation(ModelType type, String labeled_table, int minFreq, int topK, double cutoff, PhraseType typePhrase){
	switch(type){
	case BM25:
	    model = new ModelBM25(Lector.getDbmodel(false), labeled_table, minFreq);
	    break;
	case NB:
	    model = new ModelNB(Lector.getDbmodel(false), labeled_table, minFreq);
	    break;
	case NBind:
	    model = new ModelNBind(Lector.getDbmodel(false), labeled_table, minFreq);
	    break;
	case NBfilter:
	    model = new ModelNBFilter(Lector.getDbmodel(false), labeled_table, minFreq);
	    break;
	case TextExtChallenge:
	    model = new ModelTextExt(Lector.getDbmodel(false), labeled_table, minFreq, topK, cutoff);
	    break;
	}
    }

    /**
     * Process the triple to label.
     * It can not have the same entities as subject and object. 
     * Return a true value if we can extract a new facts, false otherwise.
     * 
     * @param t
     * @return
     */
    private boolean processRecord(WikiTriple t){
	Pair<String, Double> prediction = model.predictRelation(t.getSubjectType(), t.getPhrasePlaceholders(), t.getObjectType());

	// assign a relation
	String relation = prediction.key;

	// if there is ...
	if (relation!=null){
	    if (relation.contains("(-1)")){
		relation = relation.replace("(-1)", "");
		if (!Lector.getDBPedia().getRelations(t.getInvertedSubject(), t.getInvertedObject()).equals(relation)){
		    writer_provenance.provenance(t.getWikid(), t.getSection(), t.getWholeSentence(), t.getInvertedLectorSubject(), 
			    t.getInvertedSubject(), relation, t.getInvertedLectorObject(), t.getInvertedObject());
		    writer_facts.write(t.getInvertedSubject(), relation, t.getInvertedObject());
		}
	    }else{
		if (!Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject()).equals(relation)){
		    writer_provenance.provenance(t.getWikid(), t.getSection(), t.getWholeSentence(), t.getSubject(), t.getWikiSubject(), relation, t.getObject(), t.getWikiObject());
		    writer_facts.write(t.getWikiSubject(), relation, t.getWikiObject());
		}
	    }
	    return true;
	}else{
	    return false;
	}
    }

    /**
     * 
     * @return
     */
    private int runExtractionFacts(){
	int facts_extracted = 0;
	String allUnlabeledTriplesQuery = "SELECT * FROM unlabeled_triples";
	try (Statement stmt = Lector.getDbmodel(false).getConnection().createStatement()){	
	    try (ResultSet rs = stmt.executeQuery(allUnlabeledTriplesQuery)){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String section = rs.getString(2);
		    String sentence = rs.getString(3);
		    String phrase_original = rs.getString(4);
		    String phrase_placeholder = rs.getString(5);
		    String pre = rs.getString(6);
		    String post = rs.getString(7);
		    String subject = rs.getString(8);
		    String subject_type = rs.getString(10);
		    String object = rs.getString(11);
		    String object_type = rs.getString(13);

		    WikiTriple t = new WikiTriple(wikid, section, sentence, phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			if (processRecord(t)){
			    facts_extracted+=1;
			}
		    }
		}
	    }

	}catch(SQLException e){
	    e.printStackTrace();
	}

	return facts_extracted;
    }


    /**
     * 
     * @param wikid
     * @param sentence
     * @param subject_type
     * @param object
     * @return
     */
    private boolean processOntologicalRecord(String wikid, String sentence, String subject_type, String object){
	// assign a relation
	String relation = stupidNationalityRelationChooser(subject_type);

	// if there is ...
	if (relation != null){
	    if (!Lector.getDBPedia().getRelations(wikid, object).equals(relation)){
		writer_provenance_ontological.provenance(wikid, "#Abstract", sentence, "TITLE", wikid, relation, "NAT", object);
		writer_ontological_facts.write(wikid, relation, object);
	    }
	    //Lector.getDbfacts(false).insertNovelFact(t, relation);
	    return true;
	}else{
	    return false;
	}
    }

    /**
     * 
     */
    private int runExtractionOntological() {
	int facts_extracted = 0;
	String allUnlabeledTriplesQuery = "SELECT * FROM nationality_collection";
	try (Statement stmt = Lector.getDbmodel(false).getConnection().createStatement()){	
	    try (ResultSet rs = stmt.executeQuery(allUnlabeledTriplesQuery)){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String sentence = rs.getString(2);
		    String subject_type = rs.getString(3);
		    String object = rs.getString(4);

		    if (processOntologicalRecord(wikid, sentence, subject_type, object)){
			facts_extracted+=1;
			/*
			if (facts_extracted % 5000 == 0 && facts_extracted > 0)
			    System.out.println("Extracted " + facts_extracted + " novel facts.");
			    */
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return facts_extracted;
    }

    /**
     * Chose between "nationality" and "country" and assign a relation for the 
     * nationality evidence in the first sentence of the article.
     * 
     * 
     * @param subject_type
     * @return
     */
    private String stupidNationalityRelationChooser(String subject_type){
	String relation = null;
	if (!subject_type.equals("[none]")){
	    if (Lector.getDBPedia().isChildOf("[Person]", subject_type)){
		relation = "nationality";
	    }else{
		relation = "country";
	    }
	}
	return relation;
    }



    /**
     * 
     */
    public void run(){
	System.out.println("\nFacts Extraction");
	System.out.println("----------------");
	
	System.out.print("\tExtracting normal facts ... ");
	int facts_extracted = runExtractionFacts();
	System.out.println("\t" + facts_extracted + " facts.");
	
	System.out.print("\tExtracting ontological facts ... ");
	int ont_facts_extracted = runExtractionOntological();
	System.out.println("\t" + ont_facts_extracted + " facts.");

	// close the output stream
	try {
	    writer_facts.done();
	    writer_ontological_facts.done();
	    writer_provenance.done();
	    writer_provenance_ontological.done();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


}
