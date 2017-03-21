package it.uniroma3.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.uniroma3.model.WikiArticle;

public class JSONReader {

    private BufferedReader br;
    private Gson gson;

    /**
     * 
     * @param file
     */
    public JSONReader(String file){
	try {
	    this.br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
	    this.gson = new Gson();

	} catch (FileNotFoundException | UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Returns the next chunck of WikiArticles.
     * 
     * @return
     */
    public List<WikiArticle> nextChunk(int chunk){
	List<WikiArticle> s = new ArrayList<WikiArticle>(chunk);
	try {
	    String article;
	    while((article=br.readLine())!=null && chunk > 0){
		try{
		    s.add(gson.fromJson(article, WikiArticle.class));
		} catch (JsonSyntaxException e) {
		    e.printStackTrace();
		    System.out.println(article);
		}
		chunk--;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return s;
    }

    /**
     * 
     */
    public void closeBuffer(){
	try {
	    this.br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
