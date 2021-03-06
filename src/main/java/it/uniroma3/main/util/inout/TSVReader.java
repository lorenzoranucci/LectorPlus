package it.uniroma3.main.util.inout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import it.uniroma3.main.util.Pair;

public class TSVReader {

  /**
   * Reads an N-columns TSV file and put the first column in a set.
   *
   * @param path
   * @return
   */
  public static Set<String> getFirstColumn2Set(String path) {
    Set<String> entities = new HashSet<String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(path)));
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split("\t");
        entities.add(fields[0]);
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return entities;
  }

  /**
   * Reads an N-columns COMPRESSED (e.g. bz2) TSV file and put the whole lines in a set.
   *
   * @param path
   * @return
   */
  public static List<String> getLines2Set(String path) {
    List<String> entities = new ArrayList<String>();
    try {
      BufferedReader br = Compressed.getBufferedReaderForCompressedFile(path);
      String line;
      while ((line = br.readLine()) != null) {
        entities.add(line);
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return entities;
  }
  
  /**
   * Reads first K lines of an N-columns COMPRESSED (e.g. bz2) TSV file and put the whole lines in a set.
   *
   * @param path
   * @return
   */
  public static Set<String> getFirstKLines2Set(String path, int k) {
    int cont = 0;
    if (k == -1)
      k = Integer.MAX_VALUE;
    Set<String> entities = new HashSet<String>();
    try {
      BufferedReader br = Compressed.getBufferedReaderForCompressedFile(path);
      String line;
      while ((line = br.readLine()) != null) {
        cont ++;
        if (cont > k)
          break;
        entities.add(line);
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return entities;
  }

  /**
   * Reads a two-columns TSV file and put it in a map.
   * 
   * @param path
   * @return
   */
  public static Map<String, String> getLines2Map(String path) {
    Map<String, String> entities = new HashMap<String, String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(path)));
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split("\t");
        entities.put(fields[0], fields[1]);
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return entities;
  }
  
  /**
   * Reads a n-columns TSV file and count the lines.
   * 
   * @param path
   * @return
   */
  public static int countLinesTSVFile(String path) {
    int count=0;
    try {
      BufferedReader br = Compressed.getBufferedReaderForCompressedFile(path);
      String line;
      while ((line = br.readLine()) != null) {
        count+=1;
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Reads a two-columns TSV file and put it in a list of pairs.
   * 
   * @param path
   * @return
   */
  public static List<Pair<String, String>> getLines2Pairs(String path) {
    List<Pair<String, String>> pairs = new LinkedList<Pair<String, String>>();
    try {
      BufferedReader br = Compressed.getBufferedReaderForCompressedFile(path);
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split("\t");
        if (fields.length == 2)
          pairs.add(Pair.make(fields[0], fields[1]));
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return pairs;
  }


}
