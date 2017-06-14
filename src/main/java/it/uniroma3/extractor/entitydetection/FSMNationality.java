package it.uniroma3.extractor.entitydetection;

import java.util.Map;
import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.util.io.TSVReader;
/**
 * This is not a real FSM but is what we use to detect a nationality in the first sentence of the article.
 * 
 * @author matteo
 *
 */
public class FSMNationality {
    private Map<String, String> nationalities;
    
    /**
     * 
     */
    public FSMNationality(){
	this.nationalities = TSVReader.getLines2Map(Configuration.getNationalitiesList());
    }

    /**
     * 
     * @return
     */
    public String detectNationality(String sentence){
	String nationality = null;
	for (String token : sentence.split(" ")){
	    token = token.toLowerCase();
	    if(this.nationalities.containsKey(token)){
		nationality = nationalities.get(token);
	    }
	}
	return nationality;
    }
    
}
