package net.sourceforge.eclipseccase.ucm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.eclipseccase.ClearCaseProvider;

/**
 * @author mikael petterson
 *
 */
public class RuntimeSubstitution {

	private static Map<String, String> replacements = new HashMap<String, String>();

	static {
		replacements
				.put("{stream}",
						"net.sourceforge.eclipseccase.ClearCaseProvider:getCurrentStream");
	}

	public static String replace(final String msg) {

		if (msg == null || "".equals(msg) || replacements == null
				|| replacements.isEmpty()) {
			return msg;
		}
		StringBuilder regexBuilder = new StringBuilder();
		Iterator<String> it = replacements.keySet().iterator();
		regexBuilder.append(Pattern.quote(it.next()));
		while (it.hasNext()) {
			regexBuilder.append('|').append(Pattern.quote(it.next()));
		}
		Matcher matcher = Pattern.compile(regexBuilder.toString()).matcher(msg);
		StringBuffer out = new StringBuffer(msg.length() + (msg.length() / 10));
		while (matcher.find()) {
			String toBeSubstituted = replacements.get(matcher.group());
			String replacement = getValue(toBeSubstituted);
			matcher.appendReplacement(out, replacement);
			// matcher.appendReplacement(out,
			// replacements.get(matcher.group()));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	public static String getValue(String expression) {

		String[] parts = expression.split(":");

		// Obtain the Class instance
		String result = "";
		try {
			// Obtain the Class
			// Obtain the Class instance
			Class<?> cls = Class.forName(parts[0]);

			// TODO Method[] methods = cls.getDeclaredMethods();

			// Get the method
			Method method = cls.getMethod(parts[1]);

			// Create the object that we want to invoke the methods on
			ClearCaseProvider provider = (ClearCaseProvider) cls.newInstance();
			// Call the method. Since none of them takes arguments we just
			// pass an empty array as second parameter.
			result = (String) method.invoke(provider, new Object[0]);

		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();

		} catch (InvocationTargetException ex) {
			ex.printStackTrace();

		} catch (IllegalAccessException ex) {
			ex.printStackTrace();

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();

		} catch (SecurityException ex) {
			ex.printStackTrace();

		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		}

		return result;
	}

}
