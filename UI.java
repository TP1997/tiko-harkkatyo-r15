/* Luokka syötteen lukemiseen ja tulosteen tulostamiseen*/

import java.util.*;
import java.io.*;
import java.sql.*;

public class UI
{	
	boolean kaynnissa;
	Connection con;
	
	/* Rakennin, joka julistaa käyttöliittymän olevan
	Käynnissä. */
	public UI(Connection c)
	{
		kaynnissa = true;
		con = c;
		
		System.out.println("Tmi Sähtkötärskyn tietokannanhallintaohjelma");
		System.out.println("Komennolla \"help\" saat listan ohjelman tunnistamista komennoista.");
	}
	
	// Ajometodi, kutsuu syötteenlukumetodia kunnes kaynnissa-attribuutti epätosi
	public void aja()
	{
		while(kaynnissa)
		{
			System.out.print("\nSyötä komento:");
			try
			{
				lueSyote();
			}
			catch(IllegalArgumentException e)
			{
				System.out.println(e.getMessage());
				System.out.println("Komennolla \"help\" saat listan ohjelman tunnistamista komennoista.");
			}
		}
	}
	
	/* Metodi, joka kutsuu inputManagerin readString-metodia
	ja kutsuu readStringin palauttamalla merkkijonolla syötteenkäsittelymetodia*/
	private void lueSyote() throws IllegalArgumentException
	{
		String syote = inputManager.readString();
		try
		{
			kasitteleSyote(syote);
		}
		catch(IllegalArgumentException e)
		{
			throw e;
		}
	}
	
	/* Metodi, joka päättelee syötteen perusteella mikä
	toiminto suoritetaan */
	private void kasitteleSyote(String s) throws IllegalArgumentException
	{
		String kysely = "";
		int tunnus = 0;
		switch(s)
		{
			case "päivitä hinnasto":
				ht2019.paivitaTarvikeHinnasto(con, "hinnasto.txt");
				break;
			case "kysely":
				System.out.println("Syötä SQL-kysely:");
				System.out.print("psql=>");
				kysely = inputManager.readString();
				ht2019.tulostaKysely(con, kysely + "\n");
				break;
			case "lisää työkohde":
				ht2019.lisaaTyokohde(con);
				break;
			case "lisää asiakas":
				ht2019.lisaaUusiAsiakas(con);
				break;
			case "lisää urakkasopimus":
				ht2019.lisaaUrakkasopimusTietokantaan(con);
				break;
			case "lisää tarvike":
				ht2019.lisaaTarvikeKohteeseen(con);
				break;
			case "lisää tuntityösuorite":
				ht2019.lisaaTuntityosuorite(con);
				break;
			case "muodosta tuntityölasku":
				ht2019.muodostaTuntityolasku(con);
				break;
			case "luo hinta-arvio":
				System.out.println("\nValitse työkohde:");
				ht2019.tulostaKysely(con, "select*from työkohde");
				tunnus = typeCaster.toInt(ht2019.valitse(con, "työkohde", "kohdeid", false));
				ht2019.muodostaHintaArvio(con, tunnus);
				break;
			case "help":
				tulostaOhjeet();
				break;
			case "luo muistutuslaskut":
				System.out.println("Haetaan erääntyneet laskut...");
				ArrayList<int[]> eraantyneidenTunnukset = ht2019.haeEraantyneetLaskut(con);
				if(eraantyneidenTunnukset == null)
				{
					System.out.println("Erääntyneitä laskuja ei löytynyt.");
				}
				else
				{
					int lkm = 0;
					for(int i = 0;i < eraantyneidenTunnukset.size();i++)
					{
						int[] lista = eraantyneidenTunnukset.get(i);
						if(ht2019.luoEraantynytlasku(con, lista[0], lista[1], lista[2]))
							lkm++;
					}
					if(lkm == 0)
						System.out.println("Erääntyneitä laskuja ei löytynyt");
					else
						System.out.println("Löydettiin " + eraantyneidenTunnukset.size() + " erääntynyttä laskua, muistutus- tai karhulaskut luotu");
					 
				}
				break;
			case "lopeta":
				System.out.println("Suljetaan ohjelma.");
				kaynnissa = false;
				break;
			default:
				// Jos komentoa ei tunnisteta, heitetään virheilmoitus
				throw new IllegalArgumentException("Syötettyä komentoa ei tunnistettu.");
		}
	}
	
	/* Metodi, joka tulostaa jokaisen sen tunnistaman komennon */
	private void tulostaOhjeet()
	{
		System.out.println("päivitä hinnasto\nlisää työkohde\nlisää asiakas\nlisää urakkasopimus\nlisää tarvike\nlisää tuntityösuorite\nluo hinta-arvio\nmuodosta tuntityölasku\nluo muistutuslaskut\nlopeta");
	}
}