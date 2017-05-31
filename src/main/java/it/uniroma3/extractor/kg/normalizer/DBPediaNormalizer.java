package it.uniroma3.extractor.kg.normalizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Triple;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.util.reader.RDFReader;
import it.uniroma3.extractor.util.reader.RDFReader.Encoding;
/**
 * This module reads a DBPedia .ttl dump and normalize it writing a tsv file.
 * The file is composed only by triples that have a subject and object entity,
 * in which there is a property from the DBPedia ontology. 
 * 
 * Also, it performs a double iteration over the dump in order to normalize triples
 * containing blank nodes (e.g. CareerStation, Raul__1).
 * 
 * @author matteo
 *
 */
public class DBPediaNormalizer{

    /**
     * External call of the normalizer.
     */
    public static void normalizeDBPedia(String dumpFile){
	try {
	    normalizeMappingBasedDBPediaDump(dumpFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * It iterated two times over the rdf dump in order to normalize
     * functional nodes. For example, the occupations of people are expressed 
     * with functional nodes such as: http://dbpedia.org/page/Allan_Dwan__1.
     * 
     * The method removes those nodes and normalize them replacing with the relative
     * object (if it is a entity) or eliminating the triple if it is a literal.
     * 
     * @return
     * @throws IOException 
     */
    private static void normalizeMappingBasedDBPediaDump(String dumpFile) throws IOException{
	int cont = 0;
	String subject;
	String object;
	String pred;

	// FIRST iteration: save second parts
	RDFReader reader = new RDFReader(dumpFile, Encoding.bzip2);
	Map<String, List<String>> subject2secondparts = new HashMap<String, List<String>>();
	Iterator<Triple> iter = reader.readTTLFile();
	
	while(iter.hasNext()){
	    cont++;
	    if (cont % 6000000 == 0)
		System.out.println("\t-> First iteration:\t" + cont);
	    Triple t = iter.next();

	    // check if it is an interesting triple, otherwise skip
	    if (isInterestingTriple(t)){
		subject = getResourceName(t.getSubject().toString());
		object = getResourceName(t.getObject().toString());
		pred = getPredicateName(t.getPredicate().toString());
		// only check if the subject contains __, otherwise skip
		if (isIntermediateNode(subject)){
		    if (!subject2secondparts.containsKey(subject))
			subject2secondparts.put(subject, new LinkedList<String>());
		    subject2secondparts.get(subject).add(object +"\t"+ pred);
		}
	    }
	}
	reader.closeReader();

	// SECOND iteration: print clean triples in the following file
	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Configuration.getIndexableDBPediaFile())));
	reader = new RDFReader(Configuration.getDBPediaDumpFile(), Encoding.bzip2);
	cont = 0;
	iter = reader.readTTLFile();
	while(iter.hasNext()){
	    cont++;
	    if (cont % 6000000 == 0)
		System.out.println("\t-> Second iteration:\t" + cont);
	    Triple t = iter.next();
	    
	    // both subject and object have to be DBPedia resources (e.g. not common http addresses)
	    if (isInterestingTriple(t)){
		subject = getResourceName(t.getSubject().toString());
		object = getResourceName(t.getObject().toString());
		pred = getPredicateName(t.getPredicate().toString());

		if (!isIntermediateNode(subject)){
		    if (isIntermediateNode(object)){
			if (subject2secondparts.containsKey(object))
			    for (String secPart : subject2secondparts.get(object))
				bw.write(subject + "###" + secPart + "\n");
		    }else
			bw.write(subject + "###" + object + "\t" + pred + "\n");
		}
	    }
	}
	bw.close();
	reader.closeReader();
    }

    /**
     * It is an interesting triple if:
     * 
     * (1) the subject is a dbpedia resource
     * (2) the object is a dbpedia resource
     * (3) the predicate is in dbpedia ontology
     * 
     * @param t
     * @return
     */
    private static boolean isInterestingTriple(Triple t){
	boolean okSubject = isDBPediaResource(t.getSubject().toString());
	boolean okObject = isDBPediaResource(t.getObject().toString());
	boolean okPred = isInDBPediaOntology(t.getPredicate().toString());
	return okSubject && okObject && okPred;
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static String getResourceName(String uri){
	String namespace = null;
	if(Lector.getLangCode().equals("en"))
	    namespace = "http://dbpedia.org/resource/";
	if(Lector.getLangCode().equals("es"))
	    namespace = "http://es.dbpedia.org/resource/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static String getPredicateName(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static boolean isDBPediaResource(String uri){
	String namespace = null;
	if(Lector.getLangCode().equals("en"))
	    namespace = "http://dbpedia.org/resource/";
	if(Lector.getLangCode().equals("es"))
	    namespace = "http://es.dbpedia.org/resource/";
	return uri.contains(namespace);
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static boolean isIntermediateNode(String uri){
	return uri.contains("__");
    }

    /**
     * 
     * @param uri
     * @return
     */
    protected static boolean isInDBPediaOntology(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.contains(namespace);
    }

}
