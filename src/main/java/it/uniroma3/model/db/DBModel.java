package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;

import it.uniroma3.extractor.triples.WikiMVL;
import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.model.DB;
/**
 * 
 * @author matteo
 *
 */
public class DBModel extends DB{

    public DBModel(String dbname){
	super(dbname);
    }

    public void createModelDB(){
	String dropLabeled = "DROP TABLE IF EXISTS labeled_triples";
	String createLabeled = "CREATE TABLE labeled_triples("
		+ "wikid text, "
		+ "phrase_original text, "
		+ "phrase_placeholder text, "
		+ "phrase_pre text, "
		+ "phrase_post text, "
		+ "subject text, "
		+ "wiki_subject text, "
		+ "type_subject text, "
		+ "object text, "
		+ "wiki_object text, "
		+ "type_object text, "
		+ "relation text)";
	String dropUnlabeled = "DROP TABLE IF EXISTS unlabeled_triples";
	String createUnlabeled = "CREATE TABLE unlabeled_triples("
		+ "wikid text, "
		+ "phrase_original text, "
		+ "phrase_placeholder text, "
		+ "phrase_pre text, "
		+ "phrase_post text, "
		+ "subject text, "
		+ "wiki_subject text, "
		+ "type_subject text, "
		+ "object text, "
		+ "wiki_object text, "
		+ "type_object text)";
	String dropOther = "DROP TABLE IF EXISTS other_triples";
	String createOther = "CREATE TABLE other_triples("
		+ "wikid text, "
		+ "phrase_original text, "
		+ "phrase_placeholder text, "
		+ "phrase_pre text, "
		+ "phrase_post text, "
		+ "subject text, "
		+ "wiki_subject text, "
		+ "type_subject text, "
		+ "object text, "
		+ "wiki_object text, "
		+ "type_object text, "
		+ "type text)";
	String dropMVLCollection = "DROP TABLE IF EXISTS mvl_collection";
	String createMVLCollection = "CREATE TABLE mvl_collection("
		+ "code text, "
		+ "section text, "
		+ "wikid text, "
		+ "list text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(dropUnlabeled);
	    stmt.executeUpdate(createUnlabeled);
	    stmt.executeUpdate(dropOther);
	    stmt.executeUpdate(createOther);
	    stmt.executeUpdate(dropMVLCollection);
	    stmt.executeUpdate(createMVLCollection);
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     */
    public void createNecessaryIndexes(){
	System.out.print("Writing indexes ... ");
	String indexModelRelationPhrase = "CREATE INDEX IF NOT EXISTS indexmodelrelationphrase "
		+ "ON labeled_triples(relation, phrase_placeholder)";
	String indexModelPhrase = "CREATE INDEX IF NOT EXISTS indexmodelphrase "
		+ "ON labeled_triples(phrase_placeholder)";
	String indexModelTypesPhrase = "CREATE INDEX IF NOT EXISTS indexmodeltypesphrase "
		+ "ON labeled_triples(type_subject, type_object, relation, phrase_placeholder)";
	String indexUnlabeledPhrase = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
		+ "ON unlabeled_triples(phrase_placeholder, type_subject, type_object)";
	String indexOther = "CREATE INDEX IF NOT EXISTS indexother "
		+ "ON other_triples(type)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(indexModelRelationPhrase);
	    stmt.executeUpdate(indexModelPhrase);
	    stmt.executeUpdate(indexModelTypesPhrase);
	    stmt.executeUpdate(indexOther);
	    stmt.executeUpdate(indexUnlabeledPhrase);

	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
	System.out.println("Done!");

    }

    /**
     * This is the schema of labeled_triples:
     * 
     * 		01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     * 		04- phrase_pre text
     * 		05- phrase_post text
     * 		06- subject text
     * 		07- wiki_subject text
     * 		08- type_subject text
     * 		09- object text
     * 		10- wiki_object text
     * 		11- type_object text
     * 		12- relation text
     * 
     * @param triple
     * @param relation
     */
    public void insertLabeledTriple(WikiTriple triple, String relation){
	String insert = "INSERT INTO labeled_triples VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhraseOriginal());
	    stmt.setString(3, triple.getPhrasePlaceholders());
	    stmt.setString(4, triple.getPre());
	    stmt.setString(5, triple.getPost());
	    stmt.setString(6, triple.getSubject());
	    stmt.setString(7, triple.getWikiSubject());
	    stmt.setString(8, triple.getSubjectType());
	    stmt.setString(9, triple.getObject());
	    stmt.setString(10, triple.getWikiObject());
	    stmt.setString(11, triple.getObjectType());
	    stmt.setString(12, relation);
	    stmt.execute();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * This is the schema of unlabeled_triples:
     * 
     * 		01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     *      	04- phrase_pre text
     * 		05- phrase_post text
     * 		06- subject text
     * 		07- wiki_subject text
     * 		08- type_subject text
     * 		09- object text
     * 		10- wiki_object text
     * 		11- type_object text
     * 		12- type text
     * 
     * @param triple
     */
    public void insertUnlabeledTriple(WikiTriple triple){
	String insert = "INSERT INTO unlabeled_triples VALUES(?,?,?,?,?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhraseOriginal());
	    stmt.setString(3, triple.getPhrasePlaceholders());
	    stmt.setString(4, triple.getPre());
	    stmt.setString(5, triple.getPost());
	    stmt.setString(6, triple.getSubject());
	    stmt.setString(7, triple.getWikiSubject());
	    stmt.setString(8, triple.getSubjectType());
	    stmt.setString(9, triple.getObject());
	    stmt.setString(10, triple.getWikiObject());
	    stmt.setString(11, triple.getObjectType());
	    stmt.execute();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * This is the schema of other_triples:
     * 
     * 		01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     *      	04- phrase_pre text
     * 		05- phrase_post text
     * 		06- subject text
     * 		07- wiki_subject text
     * 		08- type_subject text
     * 		09- object text
     * 		10- wiki_object text
     * 		11- type_object text
     * 		12- type text
     * 
     * @param triple
     */
    public void insertOtherTriple(WikiTriple triple){
	String insert = "INSERT INTO other_triples VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhraseOriginal());
	    stmt.setString(3, triple.getPhrasePlaceholders());
	    stmt.setString(4, triple.getPre());
	    stmt.setString(5, triple.getPost());
	    stmt.setString(6, triple.getSubject());
	    stmt.setString(7, triple.getWikiSubject());
	    stmt.setString(8, triple.getSubjectType());
	    stmt.setString(9, triple.getObject());
	    stmt.setString(10, triple.getWikiObject());
	    stmt.setString(11, triple.getObjectType());
	    stmt.setString(12, triple.getType().name());
	    stmt.execute();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }


    /**
     * 
     * @param list
     */
    public void insertMVList(WikiMVL list){
	String insert = "INSERT INTO mvl_collection VALUES(?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, list.getCode());
	    stmt.setString(2, list.getWikid());
	    stmt.setString(3, list.getSection());
	    stmt.setString(4, StringUtils.join(list.getListWikid(), ","));
	    stmt.execute();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     */
    public void closeConnection() {
	this.closeConnection();
    }

}
