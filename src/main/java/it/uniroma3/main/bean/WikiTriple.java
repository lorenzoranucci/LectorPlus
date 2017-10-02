package it.uniroma3.main.bean;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.config.Lector;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
/**
 * 
 * @author matteo
 *
 */
public class WikiTriple {
    
    public enum TType {
   	MVL,
   	JOINABLE,
   	JOINABLE_NOTYPE_BOTH,
   	JOINABLE_NOTYPE_SBJ,
   	JOINABLE_NOTYPE_OBJ,
   	DROP
       }

    private String wikid;
    private String section;
    private String subject;
    private String object;
    private String wikiSubject;
    private String wikiObject;
    private String subjectType;
    private String objectType;
    private String pre;
    private String phrase_original;
    private String phrase_placeholders;
    private String post;
    private String wholeSentence;
    private TType type;

    /**
     * This constructor is used when we retrieve the triple from the DB.
     * 
     * @param wikid
     * @param section
     * @param wholeSentence
     * @param phrase_original
     * @param phrase_placeholders
     * @param pre
     * @param post
     * @param subject
     * @param object
     * @param subjectType
     * @param objectType
     * @param articleType
     */
    public WikiTriple(String wikid, String section, String wholeSentence, String phrase_original, String phrase_placeholders, String pre, String post, 
	    String subject,  String object, String subjectType, String objectType, String tripleType){
	this.wikid = wikid;
	this.section = section;
	this.wholeSentence = wholeSentence;
	this.subject = subject;
	this.wikiSubject = getWikipediaName(subject);
	this.subjectType = subjectType;
	this.phrase_original = phrase_original;
	this.phrase_placeholders = phrase_placeholders;
	this.pre = pre;
	this.post = post;
	this.object = object;
	this.wikiObject = getWikipediaName(object);
	this.objectType = objectType;
	this.type = TType.valueOf(tripleType);
    }

    /**
     * This constructor is used when we harvest the triple from the text.
     * 
     * @param wikid
     * @param section
     * @param wholeSentence
     * @param pre
     * @param subject
     * @param phrase_original
     * @param phrase_placeholders
     * @param object
     * @param post
     */
    public WikiTriple(String wikid, String section, String wholeSentence, String pre, String subject, 
	    String phrase_original, String phrase_placeholders, String object, String post){
	this.wikid = wikid;
	this.section = section;
	this.wholeSentence = wholeSentence;
	this.subject = subject;
	this.wikiSubject = getWikipediaName(subject);
	this.subjectType = getEntityType(subject);
	this.phrase_original = phrase_original;
	this.phrase_placeholders = phrase_placeholders;
	this.object = object;
	this.wikiObject = getWikipediaName(object);
	this.objectType = getEntityType(object);
	this.pre = pre;
	this.post = post;
	assignType();
    }
    

    /**
     * Assign the type based on the entities involved.
     */
    private void assignType(){
	if (isWikiEntity(this.subject) && isMVLEntity(this.object)){
	    this.type = TType.MVL;
	    return;
	}
	
	if (isWikiEntity(this.subject) && isWikiEntity(this.object)){
	    if (!getSubjectType().equals("[none]") && !getObjectType().equals("[none]")){
		this.type = TType.JOINABLE;
		return;
	    }
	    
	    if (getSubjectType().equals("[none]") && getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_BOTH;
		return;
	    }
	    
	    if (!getSubjectType().equals("[none]") && getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_OBJ;
		return;
	    }
	    
	    if (getSubjectType().equals("[none]") && !getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_SBJ;
		return;
	    }
	    
	}
	this.type = TType.DROP;
    }


    /**
     * Reg-ex that defines primary (PE) o secondary 
     * entities (SE) that have a wikid and a specific type.
     * 
     * e.g. <PE-TITLE<Real_Madrid>>
     * 
     * @return 
     */
    private boolean isWikiEntity(String entity) {
	boolean isJoinable = false;
	Pattern joinableEntity = Pattern.compile("^" + MarkupParser.WIKID_REGEX + "$");
	Matcher m = joinableEntity.matcher(entity);
	if(m.matches()){
	    isJoinable = true;
	}
	return isJoinable;    
    }

    /**
     * Reg-ex that defines a multi-values list (MVL) entity.
     * 
     * @return 
     */
    private boolean isMVLEntity(String entity) {
	boolean isMVL = false;
	Pattern MVLEntity = Pattern.compile("^<<MVL>><([^>]*?)><([^>]*?)>>$");
	Matcher m = MVLEntity.matcher(entity);
	if(m.matches()){
	    isMVL = true;
	}
	return isMVL;    
    }

    /**
     * This method queries the KG to find possible relations between the entities.
     * We use the method only if a triple is JOINABLE.
     * 
     * @return
     */
    public Set<String> getLabels() {
	return Lector.getDBPedia().getRelations(wikiSubject, wikiObject);
    }


    /**
     * This method queries the Types Assigner to find the type for the entities.
     * It assigns [none] in case of no type.
     * 
     * @return
     */
    public String getEntityType(String entity) {
	String type = "[none]";
	// make sure it makes sense to qyery the type
	if (isWikiEntity(entity) && !isMVLEntity(entity)){
	    type = Lector.getDBPedia().getType(getWikipediaName(entity));
	}
	return type;
    }
    
    /**
     * This method extracts Wikipedia Id (i.e. wikid) from the annotated entities.
     * 
     * @param entity
     * @return
     */
    private String getWikipediaName(String entity){
	String dbpediaEntity = null;
	Pattern ENTITY = Pattern.compile(MarkupParser.WIKID_REGEX);
	Matcher m = ENTITY.matcher(entity);
	if(m.find()){
	    dbpediaEntity = m.group(2);
	}
	return dbpediaEntity;
    }

    /**
     * @return the subjectType
     */
    public String getSubjectType() {
	return subjectType;
    }

    /**
     * @return the objectType
     */
    public String getObjectType() {
	return objectType;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
	return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
	this.subject = subject;
    }

    /**
     * @return the object
     */
    public String getObject() {
	return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(String object) {
	this.object = object;
    }

    /**
     * @return the phrase
     */
    public String getPhraseOriginal() {
	return phrase_original;
    }

    /**
     * @param phrase the phrase to set
     */
    public void setPhraseOriginal(String phrase_original) {
	this.phrase_original = phrase_original;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((object == null) ? 0 : object.hashCode());
	result = prime * result + ((phrase_original == null) ? 0 : phrase_original.hashCode());
	result = prime * result + ((subject == null) ? 0 : subject.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	WikiTriple other = (WikiTriple) obj;
	if (object == null) {
	    if (other.object != null)
		return false;
	} else if (!object.equals(other.object))
	    return false;
	if (phrase_original == null) {
	    if (other.phrase_original != null)
		return false;
	} else if (!phrase_original.equals(other.phrase_original))
	    return false;
	if (subject == null) {
	    if (other.subject != null)
		return false;
	} else if (!subject.equals(other.subject))
	    return false;
	return true;
    }

    /**
     * 
     */
    public String toString(){
	return this.subject + this.subjectType + "\t" + this.phrase_placeholders + "\t" + this.object + this.objectType;
    }

    /**
     * @return the pre
     */
    public String getPre() {
	return pre;
    }

    /**
     * @return the post
     */
    public String getPost() {
	return post;
    }


    /**
     * @return the type
     */
    public TType getType() {
	return type;
    }

    /**
     * 
     */
    public void setType(TType type){
	this.type = type;
    }

    /**
     * @return the wikid
     */
    public String getWikid() {
	return wikid;
    }

    /**
     * @return the wikiSubject
     */
    public String getWikiSubject() {
        return wikiSubject;
    }

    /**
     * @return the wikiObject
     */
    public String getWikiObject() {
        return wikiObject;
    }

    /**
     * @return the phrasePlaceholders
     */
    public String getPhrasePlaceholders() {
        return phrase_placeholders;
    }
    
    /**
     * 
     * @return
     */
    public String getInvertedSubject(){
	return this.wikiObject;
    }
    
    /**
     * 
     * @return
     */
    public String getInvertedLectorSubject(){
	return this.object;
    }
    
    /**
     * 
     * @return
     */
    public String getInvertedObject(){
	return this.wikiSubject;
    }
    
    /**
     * 
     * @return
     */
    public String getInvertedLectorObject(){
	return this.subject;
    }

    /**
     * @return the wholeSentence
     */
    public String getWholeSentence() {
        return wholeSentence;
    }

    /**
     * @return the section
     */
    public String getSection() {
        return section;
    }

}
