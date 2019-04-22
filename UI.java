/* Luokka syötteen lukemiseen ja tulosteen tulostamiseen*/

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
			try
			{
				lueSyote();
			}
			catch(IllegalArgumentException e)
			{
				System.out.println(e.getMessage());
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
	private boolean kasitteleSyote(String s) throws IllegalArgumentException
	{
		switch(s)
		{
			case "kysely":
				System.out.println("Syötä SQL-kysely:");
				System.out.print("psql=>");
				String kysely = inputManager.readString();
				ht2019.tulostaKysely(con, kysely + "\n");
				break;
			case "lisää työkohde":
				ht2019.lisaaTyokohde(con);
				break;
			case "lisää asiakas"
				ht2019.lisaaUusiAsiakas(con);
				break;
			case "lisää urakkasopimus"
				ht2019.lisaaUrakkasopimusTietokantaan(con);
				break;
			case: "lisää tarvike"
				System.out.println("Syötä suoritteen tunnus: [numero 0-*]");
				int tunnus = inputManager.readInt();
				h2019.lisaaTarvikeSuoritteeseen(con, tunnus);
				break;
			case: "lisää suorite"
				ht.lisaaTuntityosuorite(con);
				break;
			case: "luo lasku"
				System.out.println("Syötä tuntityölaskun kohteen tunnus: ");
				int tunnus = inputManager.readInt();
				ht.muodostaTuntityolasku(con, tunnus, null, 1);
				break;
			case: "luo muistutuslaskut"
				// Tarkistetaan 
				break;
			case: "lopeta"
				System.out.println("Suljetaan ohjelma.");
				kaynnissa = false;
				break;
			default:
				throw new IllegalArgumentException("Syötettyä komentoa ei tunnistettu.");
				break;
		}
	}
	
	/* Metodi, joka tulostaa jokaisen sen tunnistaman komennon */
	private void tulostaOhjeet()
	{
		System.out.println("lisää työkohde\nlisää
		asiakas\nlisää urakkasopimus\nlisää tarvike\nlisää tuntityösuorite\nluo lasku\nluo muistutuslaskut\nkysely\nlopeta
	}
}