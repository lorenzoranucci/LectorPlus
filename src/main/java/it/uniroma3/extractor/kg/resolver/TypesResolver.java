package it.uniroma3.extractor.kg.resolver;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.kg.normalizer.TypesNormalizer;
import it.uniroma3.extractor.kg.tgpatterns.Ontology;
import it.uniroma3.extractor.kg.tgpatterns.TGPattern;
import it.uniroma3.extractor.util.KeyValueIndex;
/**
 * 
 * @author matteo
 *
 */
public class TypesResolver {

    private static KeyValueIndex indexOriginal;
    private static KeyValueIndex indexAirpedia;
    private static KeyValueIndex indexSDTyped;
    private static KeyValueIndex indexLHD;
    private static KeyValueIndex indexDBTax;

    private static Ontology ontology;

    /**
     * 
     */
    public TypesResolver(){
	// we need the ontology to find subTypes
	ontology = new Ontology();

	if (!new File(Configuration.getTypesIndex()).exists()){
	    if (!new File(Configuration.getIndexableDBPediaNormalizedTypesFile()).exists()){
		TypesNormalizer.normalizeTypesFile();
	    }
	    System.out.print("Creating [main types] index ...");
	    long start_time = System.currentTimeMillis();
	    indexOriginal = new KeyValueIndex(Configuration.getIndexableDBPediaNormalizedTypesFile(), Configuration.getTypesIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    indexOriginal = new KeyValueIndex(Configuration.getTypesIndex());

	if (!new File(Configuration.getAirpediaIndex()).exists()){
	    if (!new File(Configuration.getIndexableDBPediaAirpediaFile()).exists()){
		TypesNormalizer.normalizeTypesFile();
	    }
	    System.out.print("Creating [airpedia] index ...");
	    long start_time = System.currentTimeMillis();
	    indexAirpedia = new KeyValueIndex(Configuration.getIndexableDBPediaAirpediaFile(), Configuration.getAirpediaIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
	}
	else// we already have the index
	    indexAirpedia = new KeyValueIndex(Configuration.getAirpediaIndex());

	/**
	 * for the english version we rely on three more types datasets 
	 * but the usage is conditioned to some rules: {@link #assignTypes(String wikid)}.
	 */
	if (Lector.getLang().equals(Lang.en)){
	    if (!new File(Configuration.getSDTypesIndex()).exists()){
		if (!new File(Configuration.getIndexableDBPediaSDTypedFile()).exists()){
		    TypesNormalizer.normalizeTypesFile();
		}
		System.out.print("Creating [sdtyped] index ...");
		long start_time = System.currentTimeMillis();
		indexSDTyped = new KeyValueIndex(Configuration.getIndexableDBPediaSDTypedFile(), Configuration.getSDTypesIndex());
		long end_time = System.currentTimeMillis();
		System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
	    }
	    else// we already have the index
		indexSDTyped = new KeyValueIndex(Configuration.getSDTypesIndex());

	    if (!new File(Configuration.getLHDTypesIndex()).exists()){
		if (!new File(Configuration.getIndexableDBPediaLHDFile()).exists()){
		    TypesNormalizer.normalizeTypesFile();
		}
		System.out.print("Creating [lhd] index ...");
		long start_time = System.currentTimeMillis();
		indexLHD = new KeyValueIndex(Configuration.getIndexableDBPediaLHDFile(), Configuration.getLHDTypesIndex());
		long end_time = System.currentTimeMillis();
		System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
	    }
	    else// we already have the index
		indexLHD = new KeyValueIndex(Configuration.getLHDTypesIndex());

	    if (!new File(Configuration.getDBTaxTypesIndex()).exists()){
		if (!new File(Configuration.getIndexableDBPediaDBTaxFile()).exists()){
		    TypesNormalizer.normalizeTypesFile();
		}
		System.out.print("Creating [dbtax] index ...");
		long start_time = System.currentTimeMillis();
		indexDBTax = new KeyValueIndex(Configuration.getIndexableDBPediaDBTaxFile(), Configuration.getDBTaxTypesIndex());
		long end_time = System.currentTimeMillis();
		System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
	    }
	    else// we already have the index
		indexDBTax = new KeyValueIndex(Configuration.getDBTaxTypesIndex());
	}
    }

    /**
     * @return the indexOriginal
     */
    public KeyValueIndex getIndexOriginal() {
	return indexOriginal;
    }

    /**
     * @return the indexAirpedia
     */
    public KeyValueIndex getIndexAirpedia() {
	return indexAirpedia;
    }

    /**
     * @return the indexSDTyped
     */
    public KeyValueIndex getIndexSDTyped() {
	return indexSDTyped;
    }

    /**
     * @return the indexLHD
     */
    public KeyValueIndex getIndexLHD() {
	return indexLHD;
    }

    /**
     * @return the indexDBTax
     */
    public KeyValueIndex getIndexDBTax() {
	return indexDBTax;
    }

    /**
     * Returns all the types that we can find for the entity in the given index.
     * 
     * @param wikid
     * @return
     */
    public List<String> getTypes(String wikid, KeyValueIndex index){
	List<String> types = index.retrieveValues(wikid).stream().collect(Collectors.toList());
	if (types.isEmpty())
	    types.add("none");
	return types;
    }

    /**
     * 
     * @param entity
     * @param specificIndex
     * @return
     */
    public List<TGPattern> getTGpattern(String entity, KeyValueIndex specificIndex) {
	List<TGPattern> patt = new LinkedList<TGPattern>();
	for (String t : getTypes(entity, specificIndex)){
	    TGPattern tgp = ontology.getTGPattern(t);
	    if (tgp != null)
		patt.add(tgp);
	}
	return patt;
    }

    /**
     * 
     * 
     * @param dbpediaEntity
     * @return
     */
    public TGPattern getTGpattern(String dbpediaEntity) {
	return ontology.getTGPattern(assignTypes(dbpediaEntity));
    }

    /**
     * 
     * @param types
     * @return
     */
    private String selectDeepest(List<String> types){
	String deepType = types.stream().max(Comparator.comparingInt(s -> ontology.depthNode(s))).get();
	return "[" + deepType + "]";
    }


    /**
     * Here we express the rules that are used to assign a type to an input entity.
     * Not all the dictionaries of types that we used have the same reliability!
     * 
     * @param wikid
     * @return
     */
    public String assignTypes(String wikid){
	String type = selectDeepest(getTypes(wikid, indexOriginal));
	if (Lector.getLang().equals("en")){
	    if (type.equals("[none]"))
		type = selectDeepest(getTypes(wikid, indexSDTyped));
	}
	if (type.equals("[none]"))
	    type = selectDeepest(getTypes(wikid, indexAirpedia));

	if (Lector.getLang().equals("en")){
	    if (type.equals("[none]"))
		type = selectDeepest(getTypes(wikid, indexLHD));
	}
	return type;
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));

	TypesResolver t = new TypesResolver();

	String entity = "2010_in_Strikeforce";
	System.out.println("USED --> " + t.assignTypes(entity));

	System.out.println("\nTypes in orginal mapping: ");
	t.getTypes(entity, t.getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	t.getTypes(entity, t.getIndexAirpedia()).forEach(System.out::println);

	System.out.println("\nTypes in DBTax: ");
	t.getTypes(entity, t.getIndexDBTax()).forEach(System.out::println);

	System.out.println("\nTypes in LHD: ");
	t.getTypes(entity, t.getIndexLHD()).forEach(System.out::println);

	System.out.println("\nTypes in SDTyped: ");
	t.getTypes(entity, t.getIndexSDTyped()).forEach(System.out::println);
    }

}
