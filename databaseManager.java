import java.sql.*;
import java.util.*;

//Luokka tietokantakyselyjen hallintaan. Keskeneräinen
public class databaseManager{
	
	private final String PROTOKOLLA = "jdbc:postgresql:";
	private final String PALVELIN = "dbstud2.sis.uta.fi";
	private final int PORTTI = 5432;
	private final String TIETOKANTA = ""; 
	private final String KAYTTAJA = "";
	private final String SALASANA = "";
	Connection con;
	
	public databaseManager() {
		try {
			Connection con = DriverManager.getConnection(PROTOKOLLA + "//" + PALVELIN + ":" + PORTTI + "/" + TIETOKANTA, KAYTTAJA, SALASANA);
			System.out.println("yhteys avattu");
		}
		catch(SQLException exc) {
			System.out.println("tapahtui virhe: "+exc.getMessage());
		}
	}
	public void close() {
		if(con!=null) {
			try {
				System.out.println("suljetaan yhteys");
				con.close();
			}
			catch(SQLException exc){
				System.out.println("Virhe yhteyden sulkemisessa");
			}
		}
	}
	public void printQuery(String query) {
		try {
			PreparedStatement pst=con.prepareStatement(query);
			ResultSet rs=pst.executeQuery();
			ResultSetMetaData meta=rs.getMetaData();
			int colCount=meta.getColumnCount();
			while(rs.next()) {
				for(int i=1; i<=colCount; i++) {
					System.out.print(rs.getString(i)+"; ");
				}
				System.out.println();
			}
			pst.close();
			rs.close();
			
		}
		catch(SQLException exc) {
			System.out.println("tapahtui virhe: "+exc.getMessage());
		}
	}
	
}
