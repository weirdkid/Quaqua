/*
 * @(#)MimeTypeParseException.java 
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.datatransfer;

/**
 * A class to encapsulate MimeType parsing related exceptions
 * <p>
 * Implementation taken from java.awt.datatransfer.TypeParseException.java 1.10
 * 01/12/03
 *
 * @serial exclude
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MimeTypeParseException extends Exception {

	// use serialVersionUID from JDK 1.2.2 for interoperability
	private static final long serialVersionUID = -5604407764691570741L;

	/**
	 * Constructs a MimeTypeParseException with no specified detail message.
	 */
	public MimeTypeParseException() {
		super();
	}

	/**
	 * Constructs a MimeTypeParseException with the specified detail message.
	 *
	 * @param s the detail message.
	 */
	public MimeTypeParseException(String s) {
		super(s);
	}
} // class MimeTypeParseException
