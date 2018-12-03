import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckSKU {
	/**
	 * check if the SKU is valid
	 * @param SKU product SKU
	 */
	static public boolean isSKU(String s) throws NumberFormatException{
		if (s.length() == 0 || s == null) return false;
		 Pattern p = Pattern.compile("^[A-Z-]{2}[\\-][0-9]{6}[\\-][A-Z0-9]{2}$");
		 Matcher matcher = p.matcher(s);
		 if (matcher.matches()) {
			 return true;
		 } else {
			 throw new NumberFormatException("wrong format");
		 }
	 }
}
