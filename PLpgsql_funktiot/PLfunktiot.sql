--Funktio 1, Käytetään lisaaTuntityosuorite()-metodin yhteydessä. Funktio on luotu jo tietokantaan.
--PL/psql-funktio tuntitöiden lisäämiseksi suoritteeseen.
--Parametrit: sid=suoriteid, tyyppi_=tuntityön tyyppi, määrä_=työtuntien määrä
CREATE OR REPLACE FUNCTION tuntityölisäys(sid integer, tyyppi_ text, määrä_ integer)
RETURNS void AS $$
BEGIN
	--Jos avaimella (sid, tyyppi_)-määritelty rivi jo olemassa
	IF (select count(*) from suoritetuntityöt where tyyppi=tyyppi_ and suoriteid=sid)=1 then
		--Päivitetään kys. rivin työtunnit. (eli lisätään tunteihin määrä_-tuntia)
		update suoritetuntityöt set määrä=määrä::int + määrä_ where tyyppi=tyyppi_ and suoriteid=sid;
	ELSE
		--Uusi tuntityösuorite --> Lisätään uusi rivi normaalisti tauluun.
		insert into suoritetuntityöt values (sid, tyyppi_, määrä_);
	END IF;
END
$$ LANGUAGE plpgsql;



--Funktio 2
CREATE OR REPLACE FUNCTION tarvikelisäys(tid integer, sid integer, määrä_ integer)
RETURNS void AS $$
BEGIN
	IF (select count(*) from suoritetarvike where tarvikeid=tid and suoriteid=sid)=1 then
		update suoritetarvike set määrä=määrä::int + määrä_ where tarvikeid=tid and suoriteid=sid;
	ELSE
		insert into suoritetarvike values (tid, sid, määrä_);
	END IF;
END
$$ LANGUAGE plpgsql;

--Funktio 3
CREATE OR REPLACE FUNCTION maxValue(tbl text, col text, out res integer)
as $$
begin
	execute format('select max(%s) from %s', col, tbl)
	into res;
end
$$ LANGUAGE plpgsql;
--Funktio 4

