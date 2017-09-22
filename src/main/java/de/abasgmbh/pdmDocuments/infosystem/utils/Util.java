package de.abasgmbh.pdmDocuments.infosystem.utils;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import de.abas.eks.jfop.remote.EKS;



public class Util {

	private final static String MESSAGE_BASE = "de.abasgmbh.pdmDocuments.infosystem.messages";
	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" }, { new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" }, { new String("ß"), "ss" } };
	private static Locale locale = Locale.ENGLISH;

	private static Locale getLocale() {
		try {
			return EKS.getFOPSessionContext().getOperatingLangLocale();
		} catch (final NullPointerException e) {
			return locale;
		}
	}

	public static String getMessage(String key) {
		final ResourceBundle rb = ResourceBundle.getBundle(MESSAGE_BASE, getLocale());
		return rb.getString(key);
	}

	public static String getMessage(String key, Object... params) {
		final ResourceBundle rb = ResourceBundle.getBundle(MESSAGE_BASE, getLocale());
		String rbname = rb.getBaseBundleName();
		Enumeration<String> rbvalues = rb.getKeys();
		return MessageFormat.format(rb.getString(key), params);
	}

	public static String getTimestamp() {
		Long mili = Instant.now().toEpochMilli();
		return mili.toString();
		
		
	}

	
	public static String replaceUmlaute(String orig) {
	    String result = orig;

	    for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
	        result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
	    }

	    return result;
	}
	
	
}
