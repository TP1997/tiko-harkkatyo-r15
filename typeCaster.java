import java.util.Date;
import java.text.*;
import java.sql.*;
public final class typeCaster{
	
	public static Integer toInt(String s) {
		try {
			int value=Integer.parseInt(s);
			return value;		
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
	public static Boolean toBoolean(String s) {
		try {
			boolean value=Boolean.parseBoolean(s);
			return value;		
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
	//Muutos String-tyypistä Date-tyypiksi
	public static Date toDate(String s) {
		try {
			Date date=new SimpleDateFormat("yyyy-dd-MM").parse(s);
			return date;
		}
		catch(Exception e) {
			return null;
		}
	}
	//Muutos Date-tyypistä java.sql.Date-tyypiksi
	public static java.sql.Date toSqlDate(Date d){
		try {
			java.sql.Date date=new java.sql.Date(d.getTime());
			return date;
		}
		catch(Exception e) {
			return null;
		}
	}
	
}
