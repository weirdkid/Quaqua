/*
 * @(#)MimeTypeParameterList.java 
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.datatransfer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An object that encapsulates the parameter list of a MimeType as defined in
 * RFC 2045 and 2046.
 * <p>
 * Implementation taken from java.awt.datatransfer.MimeTypeParameterList.java
 * 1.12 01/12/03
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
class MimeTypeParameterList implements Cloneable {

	/**
	 * Default constructor.
	 */
	public MimeTypeParameterList() {
		parameters = new Hashtable();
	}

	public MimeTypeParameterList(String rawdata) throws MimeTypeParseException {
		parameters = new Hashtable();

		// now parse rawdata
		parse(rawdata);
	}

	@Override
	public int hashCode() {
		int code = Integer.MAX_VALUE / 45; // "random" value for empty lists
		String paramName = null;
		Enumeration enm = this.getNames();

		while (enm.hasMoreElements()) {
			paramName = (String) enm.nextElement();
			code += paramName.hashCode();
			code += this.get(paramName).hashCode();
		}

		return code;
	} // hashCode()

	/**
	 * Two parameter lists are considered equal if they have exactly the same set of
	 * parameter names and associated values. The order of the parameters is not
	 * considered.
	 */
	@Override
	public boolean equals(Object thatObject) {
		if (!(thatObject instanceof MimeTypeParameterList)) {
			return false;
		}
		MimeTypeParameterList that = (MimeTypeParameterList) thatObject;
		if (this.size() != that.size()) {
			return false;
		}
		String name = null;
		String thisValue = null;
		String thatValue = null;
		Set entries = parameters.entrySet();
		Iterator iterator = entries.iterator();
		Map.Entry entry = null;
		while (iterator.hasNext()) {
			entry = (Map.Entry) iterator.next();
			name = (String) entry.getKey();
			thisValue = (String) entry.getValue();
			thatValue = (String) that.parameters.get(name);
			if ((thisValue == null) || (thatValue == null)) {
				// both null -> equal, only one null -> not equal
				if (thisValue != thatValue) {
					return false;
				}
			} else if (!thisValue.equals(thatValue)) {
				return false;
			}
		} // while iterator

		return true;
	} // equals()

	/**
	 * A routine for parsing the parameter list out of a String.
	 */
	protected void parse(String rawdata) throws MimeTypeParseException {
		int length = rawdata.length();
		if (length > 0) {
			int currentIndex = skipWhiteSpace(rawdata, 0);
			int lastIndex = 0;

			if (currentIndex < length) {
				char currentChar = rawdata.charAt(currentIndex);
				while ((currentIndex < length) && (currentChar == ';')) {
					String name;
					String value;
					boolean foundit;

					// eat the ';'
					++currentIndex;

					// now parse the parameter name

					// skip whitespace
					currentIndex = skipWhiteSpace(rawdata, currentIndex);

					if (currentIndex < length) {
						// find the end of the token char run
						lastIndex = currentIndex;
						currentChar = rawdata.charAt(currentIndex);
						while ((currentIndex < length) && isTokenChar(currentChar)) {
							++currentIndex;
							currentChar = rawdata.charAt(currentIndex);
						}
						name = rawdata.substring(lastIndex, currentIndex).toLowerCase();

						// now parse the '=' that separates the name from the value

						// skip whitespace
						currentIndex = skipWhiteSpace(rawdata, currentIndex);

						if ((currentIndex < length) && (rawdata.charAt(currentIndex) == '=')) {
							// eat it and parse the parameter value
							++currentIndex;

							// skip whitespace
							currentIndex = skipWhiteSpace(rawdata, currentIndex);

							if (currentIndex < length) {
								// now find out whether or not we have a quoted value
								currentChar = rawdata.charAt(currentIndex);
								if (currentChar == '"') {
									// yup it's quoted so eat it and capture the quoted string
									++currentIndex;
									lastIndex = currentIndex;

									if (currentIndex < length) {
										// find the next unescqped quote
										foundit = false;
										while ((currentIndex < length) && !foundit) {
											currentChar = rawdata.charAt(currentIndex);
											if (currentChar == '\\') {
												// found an escape sequence so pass this and the next character
												currentIndex += 2;
											} else if (currentChar == '"') {
												// foundit!
												foundit = true;
											} else {
												++currentIndex;
											}
										}
										if (currentChar == '"') {
											value = unquote(rawdata.substring(lastIndex, currentIndex));
											// eat the quote
											++currentIndex;
										} else {
											throw new MimeTypeParseException(
													"Encountered unterminated quoted parameter value.");
										}
									} else {
										throw new MimeTypeParseException(
												"Encountered unterminated quoted parameter value.");
									}
								} else if (isTokenChar(currentChar)) {
									// nope it's an ordinary token so it ends with a non-token char
									lastIndex = currentIndex;
									foundit = false;
									while ((currentIndex < length) && !foundit) {
										currentChar = rawdata.charAt(currentIndex);

										if (isTokenChar(currentChar)) {
											++currentIndex;
										} else {
											foundit = true;
										}
									}
									value = rawdata.substring(lastIndex, currentIndex);
								} else {
									// it ain't a value
									throw new MimeTypeParseException(
											"Unexpected character encountered at index " + currentIndex);
								}

								// now put the data into the hashtable
								parameters.put(name, value);
							} else {
								throw new MimeTypeParseException("Couldn't find a value for parameter named " + name);
							}
						} else {
							throw new MimeTypeParseException(
									"Couldn't find the '=' that separates a parameter name from its value.");
						}
					} else {
						throw new MimeTypeParseException("Couldn't find parameter name");
					}

					// setup the next iteration
					currentIndex = skipWhiteSpace(rawdata, currentIndex);
					if (currentIndex < length) {
						currentChar = rawdata.charAt(currentIndex);
					}
				}
				if (currentIndex < length) {
					throw new MimeTypeParseException("More characters encountered in input than expected.");
				}
			}
		}
	}

	/**
	 * return the number of name-value pairs in this list.
	 */
	public int size() {
		return parameters.size();
	}

	/**
	 * Determine whether or not this list is empty.
	 */
	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	/**
	 * Retrieve the value associated with the given name, or null if there is no
	 * current association.
	 */
	public String get(String name) {
		return (String) parameters.get(name.trim().toLowerCase());
	}

	/**
	 * Set the value to be associated with the given name, replacing any previous
	 * association.
	 */
	public void set(String name, String value) {
		parameters.put(name.trim().toLowerCase(), value);
	}

	/**
	 * Remove any value associated with the given name.
	 */
	public void remove(String name) {
		parameters.remove(name.trim().toLowerCase());
	}

	/**
	 * Retrieve an enumeration of all the names in this list.
	 */
	public Enumeration getNames() {
		return parameters.keys();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.ensureCapacity(parameters.size() * 16); // heuristic: 8 characters per field

		Enumeration keys = parameters.keys();
		while (keys.hasMoreElements()) {
			buffer.append("; ");

			String key = (String) keys.nextElement();
			buffer.append(key);
			buffer.append('=');
			buffer.append(quote((String) parameters.get(key)));
		}

		return buffer.toString();
	}

	/**
	 * @return a clone of this object
	 */

	@Override
	public Object clone() {
		MimeTypeParameterList newObj = null;
		try {
			newObj = (MimeTypeParameterList) super.clone();
		} catch (CloneNotSupportedException cannotHappen) {
		}
		newObj.parameters = (Hashtable) parameters.clone();
		return newObj;
	}

	private Hashtable parameters;

	// below here be scary parsing related things

	/**
	 * Determine whether or not a given character belongs to a legal token.
	 */
	private static boolean isTokenChar(char c) {
		return ((c > 040) && (c < 0177)) && (TSPECIALS.indexOf(c) < 0);
	}

	/**
	 * return the index of the first non white space character in rawdata at or
	 * after index i.
	 */
	private static int skipWhiteSpace(String rawdata, int i) {
		int length = rawdata.length();
		if (i < length) {
			char c = rawdata.charAt(i);
			while ((i < length) && Character.isWhitespace(c)) {
				++i;
				c = rawdata.charAt(i);
			}
		}

		return i;
	}

	/**
	 * A routine that knows how and when to quote and escape the given value.
	 */
	private static String quote(String value) {
		boolean needsQuotes = false;

		// check to see if we actually have to quote this thing
		int length = value.length();
		for (int i = 0; (i < length) && !needsQuotes; ++i) {
			needsQuotes = !isTokenChar(value.charAt(i));
		}

		if (needsQuotes) {
			StringBuffer buffer = new StringBuffer();
			buffer.ensureCapacity((int) (length * 1.5));

			// add the initial quote
			buffer.append('"');

			// add the properly escaped text
			for (int i = 0; i < length; ++i) {
				char c = value.charAt(i);
				if ((c == '\\') || (c == '"')) {
					buffer.append('\\');
				}
				buffer.append(c);
			}

			// add the closing quote
			buffer.append('"');

			return buffer.toString();
		} else {
			return value;
		}
	}

	/**
	 * A routine that knows how to strip the quotes and escape sequences from the
	 * given value.
	 */
	private static String unquote(String value) {
		int valueLength = value.length();
		StringBuffer buffer = new StringBuffer();
		buffer.ensureCapacity(valueLength);

		boolean escaped = false;
		for (int i = 0; i < valueLength; ++i) {
			char currentChar = value.charAt(i);
			if (!escaped && (currentChar != '\\')) {
				buffer.append(currentChar);
			} else if (escaped) {
				buffer.append(currentChar);
				escaped = false;
			} else {
				escaped = true;
			}
		}

		return buffer.toString();
	}

	/**
	 * A string that holds all the special chars.
	 */
	private static final String TSPECIALS = "()<>@,;:\\\"/[]?=";

}
