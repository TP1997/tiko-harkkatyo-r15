import java.sql.*;
import java.util.*;

public class ht2019{
	
	private static final String PROTOKOLLA = "jdbc:postgresql:";
	private static final String PALVELIN = "dbstud2.sis.uta.fi";
	private static final int PORTTI = 5432;
	private static final String TIETOKANTA = "tiko2019r15"; 
	private static final String KAYTTAJA = "tp427552";
	private static final String SALASANA = "tuomas";
	
	public static void listaaAsiakkaat(Connection con){
		try {
			PreparedStatement pst=con.prepareStatement("SELECT * FROM asiakas");
			ResultSet adata=pst.executeQuery();
			System.out.println("Asiakastiedot: ");
			while(adata.next()){
				System.out.println(adata.getString(1)+"; "+adata.getString(2)+"; "+adata.getString(3));
			}
			pst.close();
			adata.close();
		}
		catch(SQLException exc) {
			System.out.println("tapahtui virhe: "+exc.getMessage());
		}
		
	}
	public static void tulostaKysely(Connection con, String kysely) {
		try {
			PreparedStatement pst=con.prepareStatement(kysely);
			ResultSet rs=pst.executeQuery();
			ResultSetMetaData meta=rs.getMetaData();
			int colCount=meta.getColumnCount();
			while(rs.next()){
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
	public void lisaaUusiAsiakas(Connection con){
		Scanner sc=new Scanner(System.in);
		System.out.print("Asiakkaan tunnus:");
		String tunnus=sc.next();
		System.out.print("Asiakkaan nimi:");
		String animi=sc.next();
		System.out.print("Asiakkaan osoite:");
		String aosoite=sc.next();
		
		Integer atunnus=typeCaster.toInt(tunnus);
		if(atunnus!=null) {
			try {
				PreparedStatement pst=con.prepareStatement("INSERT INTO asiakas(asiakasid, nimi, laskutusosoite) VALUES"
														  + "(?,?,?)");
				pst.setInt(1, atunnus);
				pst.setString(2, animi);
				pst.setString(3, aosoite);
				pst.executeUpdate();
				pst.close();
				System.out.println("Uusi asiakas lisätty.");
			}
			catch(SQLException exc) {
				System.out.println("tapahtui virhe: "+exc.getMessage());
			}
		}
		sc.close();
		
	}
	public static void lisaaTyokohde(Connection con){
		Scanner sc=new Scanner(System.in);
		listaaAsiakkaat(con);
		System.out.print("Työkohteen omistajan tunnus:");
		String tunnus1=sc.nextLine();
		System.out.print("Työkohteen tunnus:");
		String tunnus2=sc.nextLine();
		System.out.print("Työkohteen nimi:");
		String tknimi=sc.nextLine();
		System.out.print("Työkohteen osoite:");
		String tkosoite=sc.nextLine();
		System.out.print("Kotitalousvähennys? true/false:");
		String kvkelpoinen=sc.nextLine();
		
		Integer atunnus=typeCaster.toInt(tunnus1);
		Integer tktunnus=typeCaster.toInt(tunnus2);
		Boolean kvk=typeCaster.toBoolean(kvkelpoinen);
		if(atunnus!=null && tktunnus!=null && kvk!=null) {
			try {
				PreparedStatement pst=con.prepareStatement("INSERT INTO työkohde(kohdeid, asiakasid, nimi, osoite, kvkelpoinen) VALUES"
												   +"(?,?,?,?,?)");
				pst.setInt(1,  tktunnus);
				pst.setInt(2, atunnus);
				pst.setString(3, tknimi);
				pst.setString(4, tkosoite);
				pst.setBoolean(5, kvk);
				pst.executeUpdate();
				pst.close();
				System.out.println("Uusi työkohde lisätty.");
				
			}
			catch(SQLException exc) {
				System.out.println("tapahtui virhe: "+exc.getMessage());
			}
		}
		sc.close();
		
	}
	public static void lisaaTuntityosuorite(Connection con) {
		Scanner sc=new Scanner(System.in);
		String kysely="SELECT a.nimi as asiakas, t.kohdeid, t.nimi as kohde, t.osoite "+
				"FROM asiakas as a, työkohde as t "+
				"where a.asiakasid=t.asiakasid";
		System.out.println("Työkohteiden tiedot:");
		tulostaKysely(con, kysely);
		
		System.out.print("Valitse työkohteen tunnus:");
		Integer tktunnus=typeCaster.toInt(sc.nextLine());
		if(tktunnus!=null) {
			
		}
		else
			System.out.print("Tunnus ei kelpaa, poistutaan...");
	}
	
	public static Connection avaaYhteys() {
		try {
			Connection con = DriverManager.getConnection(PROTOKOLLA + "//" + PALVELIN + ":" + PORTTI + "/" + TIETOKANTA, KAYTTAJA, SALASANA);
			System.out.println("yhteys avattu");
			return con;
		}
		catch(SQLException exc){
			System.out.println("tapahtui virhe: "+exc.getMessage());
			return null;
		}
		
	}
	public static void suljeYhteys(Connection con) {
		if(con!=null) {
			try{
				System.out.println("suljetaan yhteys");
				con.close();
			}
			catch(SQLException exc){
				System.out.println("Virhe yhteyden sulkemisessa");
			}
		}
	}
	public static void main(String args[]) {
		Connection con = avaaYhteys();
		
		//lisaaTyokohde(con);
		lisaaTuntityosuorite(con);
		
		
		suljeYhteys(con);
	}
	
}
