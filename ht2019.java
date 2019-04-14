import java.sql.*;
import java.util.*;
import java.util.HashMap;

public class ht2019{
	
	private static final String PROTOKOLLA = "jdbc:postgresql:";
	private static final String PALVELIN = "dbstud2.sis.uta.fi";
	private static final int PORTTI = 5432;
	private static final String TIETOKANTA = "tiko2019r15"; 
	private static final String KAYTTAJA = "";
	private static final String SALASANA = "";
	

	//Metodi satunnaisen kyselyn tulostamiseen
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
	//Metodi uuden asiakkaan lisäämiseksi tietokantaan.
	public void lisaaUusiAsiakas(Connection con){
		//Kysellään asiakkaan tiedot
		Scanner sc=new Scanner(System.in);
		Integer atunnus=uusiID(con, "asiakas", "asiakasid");
		System.out.print("Asiakkaan nimi:");
		String animi=sc.nextLine();
		System.out.print("Asiakkaan osoite:");
		String aosoite=sc.nextLine();
		
		if(atunnus!=null) {
			try {
				//Lisätään uusi asiakas "asiakas"-tauluun
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
		else
			System.out.println("Virheelliset tiedot.");
		sc.close();
		
	}
	//Metodi työkohteen lisäämiseksi asiakkaalle
	public static void lisaaTyokohde(Connection con){
		Scanner sc=new Scanner(System.in);
		//Tulostetaan kannassa olevien asiakkaiden tiedot
		tulostaKysely(con, "SELECT*FROM asiakas");
		//Kysellään työkohteeseen liittyvät tiedot
		System.out.print("Työkohteen omistajan tunnus:");
		Integer atunnus=typeCaster.toInt(sc.nextLine());		
		Integer tktunnus=uusiID(con, "työkohde", "kohdeid");
		System.out.print("Työkohteen nimi:");
		String tknimi=sc.nextLine();
		System.out.print("Työkohteen osoite:");
		String tkosoite=sc.nextLine();
		System.out.print("Kotitalousvähennys? true/false:");
		Boolean kvk=typeCaster.toBoolean(sc.nextLine());
		
		if(atunnus!=null && tktunnus!=null && kvk!=null) {
			try {
				//Lisätään uusi työkohde
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
		else
			System.out.println("Virheelliset tiedot.");
		sc.close();
		
	}
	public static Integer valitseTyokohde(Connection con) {
		Scanner sc=new Scanner(System.in);
		String kysely="SELECT a.nimi as asiakas, t.kohdeid, t.nimi as kohde, t.osoite "+
				"FROM asiakas as a, työkohde as t "+
				"where a.asiakasid=t.asiakasid";
		System.out.println("Työkohteiden tiedot:");
		tulostaKysely(con, kysely);
		System.out.print("Työkohteen tunnus:");
		Integer tkid=typeCaster.toInt(sc.nextLine());
		sc.close();
		return tkid;
		
		
	}
	//Metodi tuntiyöiden lisäämiseksi työkohteelle.
	public static void lisaaTuntityosuorite(Connection con) {
		Scanner sc=new Scanner(System.in);
		Integer tkID=valitseTyokohde(con);				
		
		System.out.print("Työn tyyppi (Suunnittelu/Työ/Aputyö):");
		String ttyyppi=sc.nextLine();								//ttyyppi=tuntityön tyyppi
		System.out.print("Työn määrä (tunteina):");
		Integer tunnit=typeCaster.toInt(sc.nextLine());				//Työtuntien määrä
		if(tkID!=null && tunnit!=null) {
			try {
				int suoriteID;
				//Suoritetaan kysely, jossa tarkastetaan, onko työkohteella vielä yhtään tuntityösuoritteita
				PreparedStatement pst=con.prepareStatement("SELECT suorite.suoriteid "+
												   "FROM suorite WHERE suorite.kohdeid=? and suorite.suoritetyyppi=true");
				pst.setInt(1, tkID);
				ResultSet rs=pst.executeQuery();
				//Jos aikaisempia tuntityösuoritteita löytyy...
				if(rs.next()) {
					//Haetaan aiemman kyselyn resultsetistä suoritteen tunnus.
					suoriteID=rs.getInt(1);
					//Suoritetaan päivitys suoritetuntityöt-tauluun. tuntityölisäys()-funktion toteutus määritelty PLfunktiot.sql tiedostossa
					CallableStatement cst=con.prepareCall("select tuntityölisäys(?,?,?)");
					cst.setInt(1, suoriteID);
					cst.setString(2, ttyyppi);
					cst.setInt(3, tunnit);
					cst.execute();
					cst.close();
				}
				//Jos aikaisempia tuntityösuoritteita ei ole...
				else {
					//Luodaan uusi suorite kys. työkohteelle.
					//Uusi suoriteID luodaan generoimalla satunnainen luku väliltä [100, 999]. Pitää tehdä parempi versio myöhemmin.
					suoriteID=uusiID(con, "suorite", "suoriteid");
					//Lisätään uusi tuntityösuoritteista kirjaa pitävä rivi työkohteelle "suorite"-tauluun
					pst=con.prepareStatement("INSERT INTO suorite VALUES (?,?,?)");
					pst.setInt(1, suoriteID);
					pst.setInt(2, tkID);
					pst.setBoolean(3, true);//true --> Kyseessä tuntityösuorite.
					pst.executeUpdate();
					
					//Lisätään uusi tuntityösuorite "suoritetuntityöt"-tauluun
					pst=con.prepareStatement("INSERT INTO suoritetuntityöt VALUES (?,?,?)");
					pst.setInt(1, suoriteID);
					pst.setString(2, ttyyppi);
					pst.setInt(3, tunnit);
					pst.executeUpdate();
				}
				
				rs.close();
				pst.close();
				
			}
			catch(SQLException exc){
				System.out.println("tapahtui virhe: "+exc.getMessage());
			}
		}
		else
			System.out.println("Virheelliset tiedot.");
		sc.close();
	}

	public static void lisaaTarvikeSuoritteeseen(Connection con, int suoriteid) {
		Scanner sc=new Scanner(System.in);	
		String kysely="SELECT tarvikeid, nimi, varastotilanne FROM tarvike WHERE varastotilanne>0";
		System.out.println("Tarvikkeet varastossa:");
		tulostaKysely(con, kysely);
		System.out.print("Tarvikkeen tunnus: ");
		Integer tarvikeid=typeCaster.toInt(sc.nextLine());
		System.out.print("Määrä: ");
		Integer maara=typeCaster.toInt(sc.nextLine());
		
		if(tarvikeid!=null && maara!=null && maara>0) {
			try {
				CallableStatement cst=con.prepareCall("update tarvike set varastotilanne=varastotilanne-? where tarvikeid=?");
				cst.setInt(1, maara);
				cst.setInt(2, tarvikeid);
				cst.execute();
				
				cst=con.prepareCall("select tarvikelisäys(?,?,?)");
				cst.setInt(1, tarvikeid);
				cst.setInt(2, suoriteid);
				cst.setInt(3, maara);
				cst.execute();
				
				cst.close();	
			}
			catch(SQLException exc) {
				System.out.println("tapahtui virhe: "+exc.getMessage());
			}
			
		}
		else
			System.out.println("Virheelliset tiedot.");
		sc.close();
		
		
	}
	
	public static Integer uusiID(Connection con, String taulu, String sarake) {
		try {
			PreparedStatement pst=con.prepareStatement("select maxValue(?,?)");
			pst.setString(1, taulu);
			pst.setString(2, sarake);
			ResultSet rs=pst.executeQuery();
			if(rs.next())
				return rs.getInt(1)+1;
			else
				return 100;
		}
		catch(SQLException exc){
			System.out.println("tapahtui virhe: "+exc.getMessage());
		}
		return null;
	}
	
	public static Connection avaaYhteys() {
		try {
			Connection con = DriverManager.getConnection(PROTOKOLLA + "//" + PALVELIN + ":" + PORTTI + "/" + TIETOKANTA, KAYTTAJA, SALASANA);
			System.out.println("yhteys avattu");
			return con;
		}
		catch(SQLException exc){
			System.out.println("tapahtui virhe: "+exc.getMessage());
			System.out.println("Suljetaan ohjelma");
			System.exit(0);
		}
		return null;
		
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
	
	//MUODOSTETAAN TUNTITYÖLASKU
	public static void muodostaTuntityolasku(Connection con, int kohdeid) {

	try {
		int asiakasid = -1;
		String asiakasNimi = "";
		String laskutusosoite = "";
		String kohdeOsoite = "";
		boolean kvkelpoinen = false;
		int suoriteid = -1;
		HashMap<String, Integer> tyonMaarat = new HashMap<>();
		HashMap<String, Double> tyonHinnat = new HashMap<>();
		ArrayList<String> tarvikkeet = new ArrayList<String>();
		String tuntityot[];
		double kvkelpoinenSumma = 0; //työnosuus
		final int tyonAlv = 24;
		double tarvikkeidenSumma = 0;

		//Hakee tiedot työkohte -taulusta kohdeid:n perusteella.
		PreparedStatement tyokohdePst = con.prepareStatement("SELECT asiakasid, osoite, kvkelpoinen FROM työkohde WHERE kohdeid = ?");
		tyokohdePst.setInt(1, kohdeid);
		ResultSet tyokohdeRs = tyokohdePst.executeQuery();
		while (tyokohdeRs.next()) {
			asiakasid = tyokohdeRs.getInt("asiakasid");
			kohdeOsoite = tyokohdeRs.getString("osoite");
			kvkelpoinen = tyokohdeRs.getBoolean("kvkelpoinen");
		}

		//Hakee tiedot asiakas -taulusta asiakasid:n perusteella
		PreparedStatement asiakasPst = con.prepareStatement("SELECT nimi, laskutusosoite FROM asiakas WHERE asiakasid = ?");
		asiakasPst.setInt(1, asiakasid);
		ResultSet asiakasRs = asiakasPst.executeQuery();
		while (asiakasRs.next()){
			asiakasNimi = asiakasRs.getString("nimi");
			laskutusosoite = asiakasRs.getString("laskutusosoite");
		}

		//Hakee suoriteid:n suorite -taulusta kohdeid:n perusteella. 
		PreparedStatement suoritePst = con.prepareStatement("SELECT suoriteid FROM suorite WHERE kohdeid = ? AND suoritetyyppi = true");
		suoritePst.setInt(1, kohdeid);
		ResultSet suoriteRs = suoritePst.executeQuery();
		while (suoriteRs.next()) {
			suoriteid = suoriteRs.getInt("suoriteid");
		}

		//Hakee työsuoritteeseen käytetyt tunnit
		PreparedStatement suoritetuntityotPst = con.prepareStatement("SELECT tyyppi, määrä FROM suoritetuntityöt WHERE suoriteid = ?");
		suoritetuntityotPst.setInt(1, suoriteid);
		ResultSet suoritetuntityotRs = suoritetuntityotPst.executeQuery();
		while (suoritetuntityotRs.next()) {
			String tyonTyyppi = suoritetuntityotRs.getString("tyyppi");
			int tyonMaara = suoritetuntityotRs.getInt("määrä");
			tyonMaarat.put(tyonTyyppi, tyonMaara);
		}
		
		//Hakee erilaisten töiden yksikköhinnat
		PreparedStatement tuntityotPst = con.prepareStatement("SELECT tyyppi, hinta FROM tuntityöt");
		ResultSet tuntityotRs = tuntityotPst.executeQuery();
		while (tuntityotRs.next()) {
			String tyonTyyppi = tuntityotRs.getString("tyyppi");
			double tyonHinta = tuntityotRs.getDouble("hinta");
			tyonHinnat.put(tyonTyyppi, tyonHinta);
		}

		//Lasketaan tuntitöiden hinnat
		tuntityot = new String[tyonMaarat.size()];
		int tt = 0;
		double summa;
		//| tyyppi | määrä h | yksikköhinta € | alv % | alv-osuus € | yhteensä € |
		for (String i : tyonMaarat.keySet()){
			summa = tyonMaarat.get(i) * tyonHinnat.get(i);
			tuntityot[tt] = ((i.replaceAll("\\s","")) +" | "+ Integer.toString(tyonMaarat.get(i)) +" | "+ Double.toString(tyonHinnat.get(i)) +" | "+ Integer.toString(tyonAlv) +" | "+ Double.toString(summa * (tyonAlv / 100.0)) +" | "+ Double.toString(summa));
			kvkelpoinenSumma += summa;
			tt++;
		}

		//Hakee työsuoritteeseen käytetyt tarvikkeet
		PreparedStatement tarvikePst = con.prepareStatement("SELECT suoritetarvike.määrä, tarvike.tarvikeid, tarvike.nimi, tarvike.yksikkö, tarvike.myyntihinta, tarvike.alv FROM suoritetarvike, tarvike WHERE suoritetarvike.suoriteid = ? AND suoritetarvike.tarvikeid = tarvike.tarvikeid");
		tarvikePst.setInt(1, suoriteid);
		ResultSet tarvikeRs = tarvikePst.executeQuery();
		//| tuotenro | kuvaus | määrä | yksikkö | yksikköhinta € | alv % | alv-osuus € | yhteensä € |
		while (tarvikeRs.next()) {
			tarvikkeet.add(Integer.toString(tarvikeRs.getInt("tarvikeid")) +" | "+ tarvikeRs.getString("nimi") +" | "+ Integer.toString(tarvikeRs.getInt("määrä")) +" | "+ tarvikeRs.getString("yksikkö") +" | "+ Double.toString(tarvikeRs.getDouble("myyntihinta")) +" | "+ Integer.toString(tarvikeRs.getInt("alv")) +" | "+ Double.toString(tarvikeRs.getInt("määrä") * tarvikeRs.getDouble("myyntihinta") * ((tarvikeRs.getInt("alv")) / 100.0)) +" | "+ Double.toString(tarvikeRs.getDouble("myyntihinta") * tarvikeRs.getInt("määrä")));
			
			tarvikkeidenSumma += tarvikeRs.getDouble("myyntihinta") * tarvikeRs.getInt("määrä");
		}
		
		//Lisää lasku -taulun päivitys.
		//Lisää tulostukset tekstitiedostoon?
		
		//Tulostelua
		System.out.println("");
		System.out.println("VASTAANOTTAJA");
		System.out.println(asiakasNimi);
		System.out.println(laskutusosoite);
		System.out.println("");
		
		System.out.println("TYÖKOHDE");
		System.out.println(kohdeOsoite);
		System.out.println("");
		
		System.out.println("KÄYTETYT TARVIKKEET");
		System.out.println("tuotenro | kuvaus | määrä | yksikkö | yksikköhinta € | alv % | alv-osuus € | yhteensä €");
		for (String s : tarvikkeet) {
			System.out.println(s);
		}
		System.out.println("Tarvikkeet yhteensä: " + Double.toString(tarvikkeidenSumma));
		System.out.println("");
		System.out.println("KÄYTETYT TYÖTUNNIT");
		System.out.println("tyyppi | määrä h | yksikköhinta € | alv % | alv-osuus € | yhteensä €");
		for (String a : tuntityot) {
			System.out.println(a);
		}
		if(kvkelpoinen){
			System.out.println("Kotitalousvähennykseen kelpaava osuus: " + Double.toString(kvkelpoinenSumma));
		} else {
			System.out.println("Työ yhteensä: " + Double.toString(kvkelpoinenSumma));
		}
		System.out.println("");
		System.out.println("Maksettavaa yhteensä: " + Double.toString(tarvikkeidenSumma + kvkelpoinenSumma));
		System.out.println("");
		
	} catch (SQLException e) {
		System.out.println("tapahtui virhe: " + e.getMessage());
	} catch (Exception ee) {
		System.out.println("tapahtui virhe: " + ee.getMessage());
	}
	}
	public static void main(String args[]) {
		Connection con = avaaYhteys();
		//Connection con2=avaaYhteys();
		//lisaaTyokohde(con);
		//lisaaTuntityosuorite(con);
		//int h=uusiID(con, "urakkasopimus", "urakkaid");
		//System.out.println(h);
		//lisaaTarvikeSuoritteeseen(con, 200);

		//muodostaTuntityolasku(con, 100);
		
		suljeYhteys(con);
	}
	
}
