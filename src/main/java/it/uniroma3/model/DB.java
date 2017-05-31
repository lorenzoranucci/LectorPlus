package it.uniroma3.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Obtain a connection to a specific DB, specified by its name.
 * 
 * @author matteo
 *
 */
public abstract class DB {
    
    private Connection connection;
    private String dbname;

    /**
     * 
     * @param dbname
     */
    public DB(String dbname){
	this.dbname = dbname;
	this.connection = obtainConnection();
    }
    
    /**
     * 
     * @return
     */
    public Connection obtainConnection(){
	String sDriverName = "org.sqlite.JDBC";
	Connection conn = null;
	try {
	    Class.forName(sDriverName);
	    String sJdbc = "jdbc:sqlite";
	    //String sDbUrl = sJdbc + ":" + this.dbname;
	    String sDbUrl = sJdbc + "::memory:";
	    conn = DriverManager.getConnection(sDbUrl);
	    Statement st = conn.createStatement();        
	    st.execute("PRAGMA synchronous=OFF");
	    st.execute("PRAGMA jorunal_mode=MEMORY");
	} catch (ClassNotFoundException | SQLException e) {
	    e.printStackTrace();
	}
	return conn;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the dbname
     */
    public String getDbname() {
        return dbname;
    }

}
