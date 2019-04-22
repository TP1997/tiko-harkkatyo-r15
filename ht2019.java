//Tietokantaohjelmointi 2019 - harjoitustyö - ryhmä 15

import java.sql.*;
import java.util.*;
import java.util.HashMap;
import java.util.Date;
import java.io.*;
import java.util.stream.*;


public class ht2019{
	
	private static final String PROTOKOLLA = "jdbc:postgresql:";
	private static final String PALVELIN = "dbstud2.sis.uta.fi";
	private static final int PORTTI = 5432;
	private static final String TIETOKANTA = "tiko2019r15"; 
	private static final String KAYTTAJA = "tp427552";
	private static final String SALASANA = "tuomas";
	
	private static final String tilinumero = "FI42 5000 1510 0000 23";	

	//Metodi satunnaisen kyselyn tulostamiseen
	public static void tulostaKysely(Connection con, String kysely) {
		try {
			//Haetaan sarakkeiden maksimileveydet
			PreparedStatement pst=con.prepareStatement(kysely, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs=pst.executeQuery();
			ResultSetMetaData meta=rs.getMetaData();
			int colCount=meta.getColumnCount();
			int[] colWidth=new int[colCount];
			for(int n=0; n<colCount; n++) {
				colWidth[n]=meta.getColumnName(n+1).length();
			}
			while(rs.next()) {
				for(int n=1; n<=colCount; n++) {
					int length=rs.getString(n).length();
					colWidth[n-1]=(length>colWidth[n-1])? length : colWidth[n-1];
				}
			}
			rs.beforeFirst();
			//Tulostetaan taulun sarakkeet
			int totLength=0;
			for(int n=0; n<colCount; n++) {
				System.out.print(String.format("%"+(-colWidth[n])+"s"+" | ", meta.getColumnName(n+1)));
				totLength+=colWidth[n]+3;
			}
			System.out.println();
			Stream.generate(()->"-").limit(totLength-1).forEach(System.out::print);
			//Tulostetaan taulun sisältö
			while(rs.next()) {
				System.out.println();
				for(int n=0; n<colCount; n++) {
					System.out.print(String.format("%"+(-colWidth[n])+"s"+" | " ,rs.getString(n+1)));
				}
			}
			System.out.println();
			Stream.generate(()->"-").limit(totLength-1).forEach(System.out::print);
			System.out.println();
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
		Integer atunnus=uusiID(con, "asiakas", "asiakasid");
		System.out.print("Asiakkaan nimi:");
		String animi=inputManager.readString();
		System.out.print("Asiakkaan osoite:");
		String aosoite=inputManager.readString();
		
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
		
	}
	//Metodi työkohteen lisäämiseksi asiakkaalle
	public static void lisaaTyokohde(Connection con){
		//Tulostetaan kannassa olevien asiakkaiden tiedot
		tulostaKysely(con, "SELECT*FROM asiakas");
		//Kysellään työkohteeseen liittyvät tiedot
		System.out.print("Työkohteen omistajan tunnus:");
		Integer atunnus=inputManager.readInt();		
		Integer tktunnus=uusiID(con, "työkohde", "kohdeid");
		System.out.print("Työkohteen nimi:");
		String tknimi=inputManager.readString();
		System.out.print("Työkohteen osoite:");
		String tkosoite=inputManager.readString();
		System.out.print("Kotitalousvähennys? true/false:");
		Boolean kvk=inputManager.readBoolean();
		
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
		
	}
	public static Integer valitseTyokohde(Connection con) {
		String kysely="SELECT a.nimi as asiakas, t.kohdeid, t.nimi as kohde, t.osoite "+
				"FROM asiakas as a, työkohde as t "+
				"where a.asiakasid=t.asiakasid";
		System.out.println("Työkohteiden tiedot:");
		tulostaKysely(con, kysely);
		System.out.print("Työkohteen tunnus:");
		Integer tkid=inputManager.readInt();
		return tkid;
		
		
	}
	public static Integer luoUusiSuorite(Connection con, Integer tyokohdeID, boolean tyyppi) {
		try {
			Integer suoriteID=uusiID(con, "suorite", "suoriteid");
			
			PreparedStatement pst=con.prepareStatement("INSERT INTO suorite VALUES(?,?,?)");
			pst.setInt(1, suoriteID);
			pst.setInt(2, tyokohdeID);
			pst.setBoolean(3, tyyppi);
			pst.executeUpdate();
			
			return suoriteID;
		}
		catch(SQLException exc) {
			System.out.println("tapahtui virhe: "+exc.getMessage());
			return null;
		}
	}
	//Metodi tuntiyöiden lisäämiseksi työkohteelle.
	public static void lisaaTuntityosuorite(Connection con) {
		Integer tkID=valitseTyokohde(con);				
		
		System.out.print("Työn tyyppi (Suunnittelu/Työ/Aputyö):");
		String ttyyppi=inputManager.readString();							//ttyyppi=tuntityön tyyppi
		System.out.print("Työn määrä (tunteina):");
		Integer tunnit=inputManager.readInt();				//Työtuntien määrä
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
	}

	public static void lisaaTarvikeSuoritteeseen(Connection con, int suoriteid) {
		String kysely="SELECT tarvikeid, nimi, varastotilanne FROM tarvike WHERE varastotilanne>0";
		System.out.println("Tarvikkeet varastossa:");
		tulostaKysely(con, kysely);
		System.out.print("Tarvikkeen tunnus: ");
		Integer tarvikeid=inputManager.readInt();
		System.out.print("Määrä: ");
		Integer maara=inputManager.readInt();
		
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
	/******************** T3 *********************************************************/
	public static void luoLasku(Connection con, Integer edeltajaid, Integer suoriteid, Integer monesko) {
		//Lasku lähetetään, kun se ollaan luotu
		Date lahetyspvm = new Date();
		System.out.println("Anna eräpäivä yyyy-dd-mm):");
		Date erapvm=inputManager.readDate();
		Integer laskuid=uusiID(con, "lasku", "laskuid");
		java.sql.Date lahetyspvmsql=typeCaster.toSqlDate(lahetyspvm);
		java.sql.Date erapvmsql=typeCaster.toSqlDate(erapvm);
		if(erapvm.getTime()>lahetyspvm.getTime() && lahetyspvmsql!=null && erapvmsql!=null) {
			try {
				CallableStatement cst=con.prepareCall("insert into lasku values(?,?,?,?,?,?,?)");
				cst.setInt(1, laskuid);
				cst.setInt(2, suoriteid);
				cst.setDate(3, lahetyspvmsql);
				cst.setDate(4, erapvmsql);
				cst.setBoolean(5, false);
				cst.setInt(6, monesko);
				if(edeltajaid!=null)cst.setInt(7, edeltajaid);
				else cst.setNull(7, java.sql.Types.INTEGER);
				cst.execute();
				
				cst.close();
				System.out.println("Lasku lisätty");
			}
			catch(SQLException exc) {
				System.out.println("tapahtui virhe: "+exc.getMessage());
			}
		}
		else
			System.out.println("Virheelliset tiedot.");
		
	}
	public static void lisaaUrakkasopimusTietokantaan(Connection con) {

		try {
			float kokonaissumma=0;
			HashMap<String, Integer> tyomaarat=new HashMap<String, Integer>();
			PreparedStatement pst=con.prepareStatement("SELECT tyyppi, hinta FROM tuntityöt");
			ResultSet rs=pst.executeQuery();
			System.out.println("Valitse urakkaan kuuluvat työt");
			while(rs.next()) {
				String tyotyyppi=rs.getString(1);
				float tuntihinta=rs.getFloat(2);
				System.out.println(tyotyyppi);
				System.out.println("Tuntien määrä: ");
				Integer tunnit=inputManager.readInt();
				if(tunnit>0) {
					tyomaarat.put(tyotyyppi, tunnit);
					kokonaissumma+=tunnit*tuntihinta;
				}
			}

			HashMap<Integer, Integer> tarvikkeet=new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> varastotilanne=new HashMap<Integer, Integer>();
			HashMap<Integer, Float> hinnat=new HashMap<Integer, Float>();
			pst=con.prepareStatement("SELECT tarvikeid, varastotilanne, myyntihinta FROM tarvike WHERE varastotilanne>0");
			rs=pst.executeQuery();
			while(rs.next()) {
				Integer tarvikeid=rs.getInt(1);
				Integer vtilanne=rs.getInt(2);
				Float myyntihinta=rs.getFloat(3);
				tarvikkeet.put(tarvikeid, 0);
				varastotilanne.put(tarvikeid, vtilanne);
				hinnat.put(tarvikeid, myyntihinta);
			}
			
			System.out.println("Valitse urakkaan kuuluvat tarvikkeet");
			tulostaKysely(con, "select tarvikeid, nimi, varastotilanne from tarvike");
			boolean exit=false;
			System.out.println("Lopeta syöttämällä -1");
			do {
				System.out.println("Tarvikkeen tunnus: ");
				Integer tunnus=inputManager.readInt();
				exit=(tunnus==-1)?true : false;
				if(!exit && tarvikkeet.containsKey(tunnus)) {
					System.out.println("Tarvikkeiden lukumärä: ");
					Integer maara=inputManager.readInt();
					if((varastotilanne.get(tunnus)-maara)>=0) {
						tarvikkeet.replace(tunnus, tarvikkeet.get(tunnus)+maara);
						varastotilanne.replace(tunnus, varastotilanne.get(tunnus)-maara);
						kokonaissumma+=maara*hinnat.get(tunnus);
					}
					else System.out.println("Varastomääräiian pieni.");
				}
				else System.out.println("Virheellinen tuotetunnus.");
			}while(!exit);
			
			System.out.println("Urakan kokonaishinta: "+kokonaissumma+"e");
			System.out.println("Laskuerien lukumäärä: ");
			Integer laskueraLkm;
			do {
				exit=true;
				laskueraLkm=inputManager.readInt();
				if((kokonaissumma/laskueraLkm)<20) {
					System.out.print("Laskuerän suuruus liian pieni, "+(kokonaissumma/laskueraLkm)+"e. Minimi 20e");
					exit=false;
				}
			}while(!exit);
			
			//Luodaan uusi suorite urakalle
			Integer tyokohdeID=valitseTyokohde(con);
			Integer suoriteID=luoUusiSuorite(con, tyokohdeID, false);
			if(suoriteID!=null) {

				//Luodaan uusi urakkasopimus
				Integer urakkaID=uusiID(con, "urakkasopimus", "urakkaid");
				CallableStatement cst=con.prepareCall("INSERT INTO urakkasopimus VALUES(?,?,?,?,?)");
				cst.setInt(1, urakkaID);cst.setInt(2, suoriteID);cst.setFloat(3, kokonaissumma/laskueraLkm);
				cst.setInt(4, laskueraLkm);cst.setInt(5, laskueraLkm);
				cst.execute();
				//Lisätään urakkaan liittyvät työt
				for(String key : tyomaarat.keySet()) {
					int tunnit=tyomaarat.get(key);
					if(tunnit>0) {
						cst=con.prepareCall("INSERT INTO urakkatyöt VALUES(?,?,?)");
						cst.setInt(1, urakkaID);cst.setString(2, key);cst.setInt(3, tunnit);
						cst.execute();
					}
				}
				//Lisätään urakkaan liittyvät tarvikkeet ja päivitetään varastotilanne
				for(Integer tarvikeid : tarvikkeet.keySet()) {
					int maara=tarvikkeet.get(tarvikeid);
					int uusiVarastotilanne=varastotilanne.get(tarvikeid);
					if(maara>0) {
						cst=con.prepareCall("INSERT INTO suoritetarvike VALUES(?,?,?)");
						cst.setInt(1, tarvikeid);cst.setInt(2, suoriteID);cst.setInt(3, maara);
						cst.execute();
					
						cst=con.prepareCall("UPDATE tarvike SET varastotilanne=? WHERE tarvikeid=?");
						cst.setInt(1,uusiVarastotilanne);cst.setInt(2,tarvikeid);
						cst.execute();
					}
				}
				System.out.print("Uusi urakkasopimus lisätty kantaan");
				rs.close();
				pst.close();
				cst.close();
			}
			
			
		}
		catch(SQLException exc) {
			System.out.println("tapahtui virhe: "+exc.getMessage());
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
	
	/**
	 * (Raportti 2 - vaadittava toiminnallisuus)
     * Kerää tuntityölaskuun tarvittavat tiedot tietokannasta ja luo niistä laskun tekstitiedostoon.
     *
     * @param con yhteys tietokantaan.
     * @param kohdeid sen työkohteen id tunnus, josta halutaan muodostaa tuntityölasku.
     * @param edeltavaLasku laskua edeltävän laskun id
     * @param moneskoLasku ilmaisee monesko lasku on kyseessä: 1=ensimmäistä kertaa lähetettävä, 2=muistutuslasku...
     */
    public static void muodostaTuntityolasku(Connection con, int kohdeid, Integer edeltavaLasku, int moneskoLasku) {
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
            String erapvm = null;
            int laskuid = -1;

            //Hakee tiedot työkohte -taulusta kohdeid:n perusteella.
            PreparedStatement tyokohdePst = con.prepareStatement("SELECT asiakasid, osoite, kvkelpoinen FROM työkohde WHERE kohdeid = ?");
            tyokohdePst.setInt(1, kohdeid);
            ResultSet tyokohdeRs = tyokohdePst.executeQuery();
            while (tyokohdeRs.next()) {
                asiakasid = tyokohdeRs.getInt("asiakasid");
                kohdeOsoite = tyokohdeRs.getString("osoite");
                kvkelpoinen = tyokohdeRs.getBoolean("kvkelpoinen");
            }
            tyokohdePst.close();
            tyokohdeRs.close();

            //Hakee tiedot asiakas -taulusta asiakasid:n perusteella
            PreparedStatement asiakasPst = con.prepareStatement("SELECT nimi, laskutusosoite FROM asiakas WHERE asiakasid = ?");
            asiakasPst.setInt(1, asiakasid);
            ResultSet asiakasRs = asiakasPst.executeQuery();
            while (asiakasRs.next()) {
                asiakasNimi = asiakasRs.getString("nimi");
                laskutusosoite = asiakasRs.getString("laskutusosoite");
            }
            asiakasPst.close();
            asiakasRs.close();

            //Hakee suoriteid:n suorite -taulusta kohdeid:n perusteella. 
            PreparedStatement suoritePst = con.prepareStatement("SELECT suoriteid FROM suorite WHERE kohdeid = ? AND suoritetyyppi = true");
            suoritePst.setInt(1, kohdeid);
            ResultSet suoriteRs = suoritePst.executeQuery();
            while (suoriteRs.next()) {
                suoriteid = suoriteRs.getInt("suoriteid");
            }
            suoritePst.close();
            suoriteRs.close();

            //Hakee työsuoritteeseen käytetyt tunnit
            PreparedStatement suoritetuntityotPst = con.prepareStatement("SELECT tyyppi, määrä FROM suoritetuntityöt WHERE suoriteid = ?");
            suoritetuntityotPst.setInt(1, suoriteid);
            ResultSet suoritetuntityotRs = suoritetuntityotPst.executeQuery();
            while (suoritetuntityotRs.next()) {
                String tyonTyyppi = suoritetuntityotRs.getString("tyyppi");
                int tyonMaara = suoritetuntityotRs.getInt("määrä");
                tyonMaarat.put(tyonTyyppi, tyonMaara);
            }
            suoritetuntityotPst.close();
            suoritetuntityotRs.close();

            //Hakee erilaisten töiden yksikköhinnat
            PreparedStatement tuntityotPst = con.prepareStatement("SELECT tyyppi, hinta FROM tuntityöt");
            ResultSet tuntityotRs = tuntityotPst.executeQuery();
            while (tuntityotRs.next()) {
                String tyonTyyppi = tuntityotRs.getString("tyyppi");
                double tyonHinta = tuntityotRs.getDouble("hinta");
                tyonHinnat.put(tyonTyyppi, tyonHinta);
            }
            tuntityotPst.close();
            tuntityotRs.close();

            //Lasketaan tuntitöiden hinnat
            tuntityot = new String[tyonMaarat.size()];
            int tt = 0;
            double summa;
            //| tyyppi | määrä h | yksikköhinta € | alv % | alv-osuus € | yhteensä € |
            for (String i : tyonMaarat.keySet()) {
                summa = tyonMaarat.get(i) * tyonHinnat.get(i);
                tuntityot[tt] = ((i.replaceAll("\\s", "")) + " | " + Integer.toString(tyonMaarat.get(i)) + " | " + Double.toString(tyonHinnat.get(i)) + " | " + Integer.toString(tyonAlv) + " | " + Double.toString(summa * (tyonAlv / 100.0)) + " | " + Double.toString(summa));
                kvkelpoinenSumma += summa;
                tt++;
            }

            //Hakee työsuoritteeseen käytetyt tarvikkeet
            PreparedStatement tarvikePst = con.prepareStatement("SELECT suoritetarvike.määrä, tarvike.tarvikeid, tarvike.nimi, tarvike.yksikkö, tarvike.myyntihinta, tarvike.alv FROM suoritetarvike, tarvike WHERE suoritetarvike.suoriteid = ? AND suoritetarvike.tarvikeid = tarvike.tarvikeid");
            tarvikePst.setInt(1, suoriteid);
            ResultSet tarvikeRs = tarvikePst.executeQuery();
            //| tuotenro | kuvaus | määrä | yksikkö | yksikköhinta € | alv % | alv-osuus € | yhteensä € |
            while (tarvikeRs.next()) {
                tarvikkeet.add(Integer.toString(tarvikeRs.getInt("tarvikeid")) + " | " + tarvikeRs.getString("nimi") + " | " + Integer.toString(tarvikeRs.getInt("määrä")) + " | " + tarvikeRs.getString("yksikkö") + " | " + Double.toString(tarvikeRs.getDouble("myyntihinta")) + " | " + Integer.toString(tarvikeRs.getInt("alv")) + " | " + Double.toString(Math.round((tarvikeRs.getInt("määrä") * tarvikeRs.getDouble("myyntihinta") * ((tarvikeRs.getInt("alv")) / 100.0)) * 100.0)/100.0) + " | " + Double.toString(tarvikeRs.getDouble("myyntihinta") * tarvikeRs.getInt("määrä")));

                tarvikkeidenSumma += tarvikeRs.getDouble("myyntihinta") * tarvikeRs.getInt("määrä");
            }
            tarvikePst.close();
            tarvikeRs.close();

            //Lasku -taulun päivitys.
            try{
				luoLasku(con, edeltavaLasku, suoriteid, moneskoLasku);
			} catch (Exception e){
				System.out.println("Lasku oli jo ilmeisesti kannassa: "+e);
			}
            
            //Haetaan lasku -taulusta tarvittavat tiedot laskuun.
            PreparedStatement erapvmPst = con.prepareStatement("SELECT eräpvm, laskuid FROM lasku WHERE suoriteid = ? AND moneskolasku = ?");
            erapvmPst.setInt(1, suoriteid);
            erapvmPst.setInt(2, moneskoLasku);
            ResultSet erapvmRs = erapvmPst.executeQuery();
            while(erapvmRs.next()){
				erapvm = erapvmRs.getString("eräpvm");
				laskuid = erapvmRs.getInt("laskuid");
			}
			erapvmPst.close();
			erapvmRs.close();	

            //Luodaan lasku txt tiedostoon
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("lasku"+Integer.toString(laskuid)+".txt"));
                
                if(moneskoLasku == 2)writer.write("MUISTUTUSLASKU\nEdellisen laskun viite: "+Integer.toString(edeltavaLasku)+"\n");
                else writer.write("LASKU\n");
                writer.write("\nLaskuviite: " + Integer.toString(laskuid));
                writer.write("\n\nLÄHETTÄJÄ\nTmi Sähkötärsky\n\n");
                writer.write("VASTAANOTTAJA\n" + asiakasNimi + "\n" + laskutusosoite + "\n\n");
                writer.write("TYÖKOHDE\n" + kohdeOsoite);
                writer.write("\n\nKÄYTETYT TARVIKKEET\n");
                writer.write("tuoteid | kuvaus | määrä | yksikkö | yksikköhinta € | alv % | alv-osuus € | yhteensä €");
                for (String s : tarvikkeet) {
                    writer.write("\n" + s);
                }
                writer.write("\nTarvikkeet yhteensä: " + Double.toString(tarvikkeidenSumma) + "€\n\n");
                writer.write("KÄYTETYT TYÖTUNNIT\n");
                writer.write("tyyppi | määrä h | yksikköhinta € | alv % | alv-osuus € | yhteensä €");
                for (String s : tuntityot) {
                    writer.write("\n" + s);
                }
                writer.write("\nTyö yhteensä: " + Double.toString(kvkelpoinenSumma) + "€");
                if (kvkelpoinen) {
                    writer.write("\n\nKotitalousvähennykseen kelpaava osuus: " + Double.toString(kvkelpoinenSumma) + "€");
                } else {
                    writer.write("\n\nKotitalousvähennykseen kelpaava osuus: 0.00€");
                }
                double laskutusLisa = 0;
                if(moneskoLasku > 1){
					laskutusLisa = (moneskoLasku - 1) * 5.00;
					writer.write("\nLaskutuslisä: 5.00€");
				}
                writer.write("\n\nMaksettavaa yhteensä: " + Double.toString(tarvikkeidenSumma + kvkelpoinenSumma + laskutusLisa) + "€");
                writer.write("\n\nEräpäivä: "+erapvm);
                writer.write("\n\nTilinumero: " + tilinumero);
                writer.close();
                System.out.println("Lasku"+Integer.toString(laskuid)+".txt luotiin onnistuneesti.");
            } catch (IOException e) {
                System.out.println("Tapahtui virhe: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Tapahtui virhe: " + e.getMessage());
        } catch (Exception ee) {
            System.out.println("Tapahtui virhe: " + ee.getMessage());
        }
    }
    
    /**
     * (Raportti 1 - vaaddittava toiminnallisuus)
     * Muodostaa hinta-arvion kohteesta x siihen aiemmin lisättyjen tarvikkeiden ja tuntitöiden pohjalta.
     * 
     * @param con yhteys tietokantaan.
     * @param kohdeid sen työkohteen id tunnus, josta halutaan muodostaa hinta-arvio.
     */
    public static void muodostaHintaArvio (Connection con, int kohdeid){
		try{
			int suoriteid = -1;
			double tarvikeSumma = 0.00;
			double tuntiSumma = 0.00;
			String kohdeOsoite = "";
			HashMap<String, Integer> tyonMaarat = new HashMap<>();
			HashMap<String, Double> tyonHinnat = new HashMap<>();
			
			//Hakee suoriteid:n suorite -taulusta kohdeid:n perusteella. 
			PreparedStatement suoritePst = con.prepareStatement("SELECT suoriteid FROM suorite WHERE kohdeid = ? AND suoritetyyppi = true");
			suoritePst.setInt(1, kohdeid);
			ResultSet suoriteRs = suoritePst.executeQuery();
			while (suoriteRs.next()) {
				suoriteid = suoriteRs.getInt("suoriteid");
			}
			suoritePst.close();
			suoriteRs.close();
			
			//Hakee työsuoritteeseen käytetyt tarvikkeet
			PreparedStatement tarvikePst = con.prepareStatement("SELECT suoritetarvike.määrä,  tarvike.myyntihinta FROM suoritetarvike, tarvike WHERE suoritetarvike.suoriteid = ? AND suoritetarvike.tarvikeid = tarvike.tarvikeid");
			tarvikePst.setInt(1, suoriteid);
			ResultSet tarvikeRs = tarvikePst.executeQuery();
			while (tarvikeRs.next()){
				tarvikeSumma += Math.round((tarvikeRs.getInt("määrä") * tarvikeRs.getDouble("myyntihinta"))*100.0)/100.0;
			}
			tarvikePst.close();
			tarvikeRs.close();
			
			//Hakee työsuoritteeseen käytetyt tunnit
			PreparedStatement suoritetuntityotPst = con.prepareStatement("SELECT tyyppi, määrä FROM suoritetuntityöt WHERE suoriteid = ?");
			suoritetuntityotPst.setInt(1, suoriteid);
			ResultSet suoritetuntityotRs = suoritetuntityotPst.executeQuery();
			while (suoritetuntityotRs.next()) {
				String tyonTyyppi = suoritetuntityotRs.getString("tyyppi");
				int tyonMaara = suoritetuntityotRs.getInt("määrä");
				tyonMaarat.put(tyonTyyppi, tyonMaara);
			}
			suoritetuntityotPst.close();
			suoritetuntityotRs.close();

			//Hakee erilaisten töiden yksikköhinnat
			PreparedStatement tuntityotPst = con.prepareStatement("SELECT tyyppi, hinta FROM tuntityöt");
			ResultSet tuntityotRs = tuntityotPst.executeQuery();
			while (tuntityotRs.next()) {
				String tyonTyyppi = tuntityotRs.getString("tyyppi");
				double tyonHinta = tuntityotRs.getDouble("hinta");
				tyonHinnat.put(tyonTyyppi, tyonHinta);
			}
			tuntityotPst.close();
			tuntityotRs.close();
			
			//Lasketaan työstä koituvat kulut
			for (String i : tyonMaarat.keySet()) {
				tuntiSumma += Math.round((tyonMaarat.get(i) * tyonHinnat.get(i))*100.0)/100.0;
			}
			
			//Hakee tiedot työkohte -taulusta kohdeid:n perusteella.
			PreparedStatement tyokohdePst = con.prepareStatement("SELECT osoite FROM työkohde WHERE kohdeid = ?");
			tyokohdePst.setInt(1, kohdeid);
			ResultSet tyokohdeRs = tyokohdePst.executeQuery();
			while (tyokohdeRs.next()) {
				kohdeOsoite = tyokohdeRs.getString("osoite");
			}
			tyokohdePst.close();
			tyokohdeRs.close();
			
			//Tulostetaan hinta-arvio
			System.out.println("Hinta-arvio kohteeseen: " + kohdeOsoite);
			System.out.println("Kulut tarvikkeista: " + Double.toString(tarvikeSumma) + "€");
			System.out.println("Kulut työtunneista: " + Double.toString(tuntiSumma) + "€");
			System.out.println("Yhteensä: " + Double.toString(tuntiSumma + tarvikeSumma) + "€");
		}catch (SQLException e){
			System.out.println("Tapahtui virhe: " + e);
		}catch (Exception e){
			System.out.println("Tapahtui virhe: " + e);
		}
	}

	public static void main(String args[]) {
		Connection con = avaaYhteys();
		// Luodaan käyttöliittymäolio
		UI kayttoliittyma = new UI(con);
		// Kutsutaan käyttöliittymän ajometodia
		kayttoliittyma.aja();
		//Connection con2=avaaYhteys();
		//lisaaTyokohde(con);
		//lisaaTuntityosuorite(con);
		//int h=uusiID(con, "urakkasopimus", "urakkaid");
		//System.out.println(h);
		//lisaaTarvikeSuoritteeseen(con, 200);
		
		//muodostaTuntityolasku(con, 100, null, 1);
		//muodostaTuntityolasku(con, 100, 1, 2);
		tulostaKysely(con, "select*from tarvike");
		//muodostaHintaArvio(con, 100);
		suljeYhteys(con);
	}
	
}
