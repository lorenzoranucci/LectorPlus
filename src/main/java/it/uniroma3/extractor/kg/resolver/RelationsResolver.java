package it.uniroma3.extractor.kg.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.kg.normalizer.DBPediaNormalizer;
import it.uniroma3.extractor.util.KeyValueIndex;
/**
 * 
 * @author matteo
 *
 */
public class RelationsResolver {

    private KeyValueIndex indexKG;

    /**
     * 
     */
    public RelationsResolver(){
	if (!new File(Configuration.getDBPediaIndex()).exists()){
	    System.out.println("**** Init DBPedia ****");
	    if (!new File(Configuration.getIndexableDBPediaFile()).exists()){
		long start_time = System.currentTimeMillis();
		System.out.println("\t-> Normalizing DBPedia dataset ... ");
		DBPediaNormalizer.normalizeDBPedia(Configuration.getDBPediaDumpFile());
		long end_time = System.currentTimeMillis();
		System.out.println("\t-> done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	    }
	    long start_time = System.currentTimeMillis();
	    System.out.print("\t-> Writing DBPedia index ... ");
	    this.indexKG = new KeyValueIndex(Configuration.getIndexableDBPediaFile(), Configuration.getDBPediaIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    this.indexKG = new KeyValueIndex(Configuration.getDBPediaIndex());
    }

    /**
     * 
     * @param wikidSubject
     * @param wikidObject
     * @return
     */
    public Set<String> getRelations(String wikidSubject, String wikidObject) {
	Set<String> relations = new HashSet<String>();
	for (String relation : indexKG.retrieveValues(wikidSubject + "###" + wikidObject))
	    relations.add(relation);
	for (String relation : indexKG.retrieveValues(wikidObject + "###" + wikidSubject)){
	    if(!relations.contains(relation))
		relations.add(relation + "(-1)");
	}
	return relations;
    }

    /**
     * 
     * @param relation
     * @return
     */
    private void getInstances(String relation){
	for (String instance : indexKG.retrieveKeys(relation))
	    System.out.println(instance);
    }

    /**
     * 
     * @param subject
     * @param object
     */
    public void findRelations(String subject, String object){
	System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
	for (String relation : getRelations(subject, object))
	    System.out.println("\t" + relation);
    }


    /**
     * TEST MAIN.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	RelationsResolver res = new RelationsResolver();

	//String subject = "Tom_Maniatis";
	//String object = "Belfast";
	//res.findRelations(subject, object);

	String relation = "managerTitle";
	res.getInstances(relation);

    }

}