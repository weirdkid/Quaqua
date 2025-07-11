/* Werner Randelshofer 2006-01-08 
 * Replaced Java 1.1 collections by Java 1.2 collections.
 */
/* XMLElement.java
 *
 * $Revision: 1.4 $
 * $Date: 2002/03/24 10:27:59 $
 * $Name: RELEASE_2_2_1 $
 *
 * This file is part of NanoXML 2 Lite.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *****************************************************************************/

package ch.randelshofer.quaqua.ext.nanoxml;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * XMLElement is a representation of an XML object. The object is able to parse
 * XML code.
 * <P>
 * <DL>
 * <DT><B>Parsing XML Data</B></DT>
 * <DD>You can parse XML data using the following code:
 * 
 * <pre>
 * XMLElement xml = new XMLElement();
 * FileReader reader = new FileReader("filename.xml");
 * xml.parseFromReader(reader);
 * </pre>
 * 
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Attributes</B></DT>
 * <DD>You can enumerate the attributes of an element using the method
 * {@link #enumerateAttributeNames() enumerateAttributeNames}. The attribute
 * values can be retrieved using the method
 * {@link #getStringAttribute(java.lang.String) getStringAttribute}. The
 * following example shows how to list the attributes of an element:
 * 
 * <pre>
 * XMLElement element = ...;
 * Iterator iter = element.getAttributeNames();
 * while (iter.hasNext()) {
 * &nbsp;&nbsp;&nbsp;&nbsp;String key = (String) iter.next();
 * &nbsp;&nbsp;&nbsp;&nbsp;String value = element.getStringAttribute(key);
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(key + " = " + value);
 * }
 * </pre>
 * 
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Child Elements</B></DT>
 * <DD>You can enumerate the children of an element using
 * {@link #iterateChildren() iterateChildren}. The number of child iterator can
 * be retrieved using {@link #countChildren() countChildren}.</DD>
 * </DL>
 * <DL>
 * <DT><B>Elements Containing Character Data</B></DT>
 * <DD>If an iterator contains character data, like in the following example:
 * 
 * <pre>
 * &lt;title&gt;The Title&lt;/title&gt;
 * </pre>
 * 
 * you can retrieve that data using the method {@link #getContent() getContent}.
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Subclassing XMLElement</B></DT>
 * <DD>When subclassing XMLElement, you need to override the method
 * {@link #createAnotherElement() createAnotherElement} which has to return a
 * new copy of the receiver.</DD>
 * </DL>
 * <P>
 *
 * @see XMLParseException
 *
 *
 *
 * @author Marc De Scheemaecker
 *         &lt;<A href="mailto:cyberelf@mac.com">cyberelf@mac.com</A>&gt;
 * @version 2005-06-18 Werner Randelshofer: Adapted for Java 2 Collections API.
 *          <br>
 *          $Name: RELEASE_2_2_1 $, $Revision: 1.4 $
 */
public class XMLElement {

	/**
	 * Serialization serial version ID.
	 */
	static final long serialVersionUID = 6685035139346394777L;

	/**
	 * Major version of NanoXML. Classes with the same major and minor version are
	 * binary compatible. Classes with the same major version are source compatible.
	 * If the major version is different, you may need to modify the client source
	 * code.
	 *
	 * @see XMLElement#NANOXML_MINOR_VERSION
	 */
	public static final int NANOXML_MAJOR_VERSION = 2;

	/**
	 * Minor version of NanoXML. Classes with the same major and minor version are
	 * binary compatible. Classes with the same major version are source compatible.
	 * If the major version is different, you may need to modify the client source
	 * code.
	 *
	 * @see XMLElement#NANOXML_MAJOR_VERSION
	 */
	public static final int NANOXML_MINOR_VERSION = 2;

	/**
	 * The attributes given to the element.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field can be empty.
	 * <li>The field is never {@code null}.
	 * <li>The keySet().iterator and the values are strings.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private HashMap<String, String> attributes;

	/**
	 * Child iterator of the element.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field can be empty.
	 * <li>The field is never {@code null}.
	 * <li>The iterator are instances of {@code XMLElement} or a subclass of
	 * {@code XMLElement}.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private ArrayList<XMLElement> children;

	/**
	 * The name of the element.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is {@code null} iff the element is not initialized by either
	 * parse or setName.
	 * <li>If the field is not {@code null}, it's not empty.
	 * <li>If the field is not {@code null}, it contains a valid XML identifier.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private String name;

	/**
	 * The #PCDATA content of the object.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is {@code null} iff the element is not a #PCDATA element.
	 * <li>The field can be any string, including the empty string.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private String contents;

	/**
	 * Conversion table for &amp;...; entities. The keySet().iterator are the entity
	 * names without the &amp; and ; delimiters.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is never {@code null}.
	 * <li>The field always contains the following associations:
	 * "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;",
	 * "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
	 * "amp"&nbsp;=&gt;&nbsp;"&amp;"
	 * <li>The keySet().iterator are strings
	 * <li>The values are char arrays
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private HashMap<Object, Object> entities;

	/**
	 * The line number where the element starts.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code lineNr &gt= 0}
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private int lineNr;

	/**
	 * {@code true} if the case of the element and attribute names are case
	 * insensitive.
	 */
	private boolean ignoreCase;

	/**
	 * {@code true} if the leading and trailing whitespace of #PCDATA sections have
	 * to be ignored.
	 */
	private boolean ignoreWhitespace;

	/**
	 * Character read too much. This character provides push-back functionality to
	 * the input reader without having to use a PushbackReader. If there is no such
	 * character, this field is '\0'.
	 */
	private char charReadTooMuch;

	/**
	 * The reader provided by the caller of the parse method.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is not {@code null} while the parse method is running.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private Reader reader;

	/**
	 * The current line number in the source content.
	 *
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>parserLineNr &gt; 0 while the parse method is running.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private int parserLineNr;

	/**
	 * Creates and initializes a new XML element. Calling the construction is
	 * equivalent to:
	 * <ul>
	 * <li>{@code new XMLElement(new HashMap(), false, true)
	 * }</li>
	 * </ul>
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>countChildren() =&amp; 0
	 * <li>iterateChildren() =&amp; empty enumeration
	 * <li>enumeratePropertyNames() =&amp; empty enumeration
	 * <li>getChildren() =&amp; empty vector
	 * <li>getContent() =&amp; ""
	 * <li>getLineNr() =&amp; 0
	 * <li>getName() =&amp; null
	 * </ul>
	 * </dd>
	 * </dl>
	 *
	 * @see XMLElement#XMLElement(java.util.HashMap) XMLElement(HashMap)
	 * @see XMLElement#XMLElement(boolean)
	 * @see XMLElement#XMLElement(java.util.HashMap,boolean) XMLElement(HashMap,
	 *      boolean)
	 */
	public XMLElement() {
		this(new HashMap<>(), false, true, true);
	}

	/**
	 * Creates and initializes a new XML element. Calling the construction is
	 * equivalent to:
	 * <ul>
	 * <li>{@code new XMLElement(entities, false, true)
	 * }</li>
	 * </ul>
	 *
	 * @param entities The entity conversion table.
	 *
	 *                 <dl>
	 *                 <dt><b>Preconditions:</b></dt>
	 *                 <dd>
	 *                 <ul>
	 *                 <li>{@code entities != null}
	 *                 </ul>
	 *                 </dd>
	 *                 </dl>
	 *
	 *                 <dl>
	 *                 <dt><b>Postconditions:</b></dt>
	 *                 <dd>
	 *                 <ul>
	 *                 <li>countChildren() =&amp; 0
	 *                 <li>iterateChildren() =&amp; empty enumeration
	 *                 <li>enumeratePropertyNames() =&amp; empty enumeration
	 *                 <li>getChildren() =&amp; empty vector
	 *                 <li>getContent() =&amp; ""
	 *                 <li>getLineNr() =&amp; 0
	 *                 <li>getName() =&amp; null
	 *                 </ul>
	 *                 </dd>
	 *                 </dl>
	 *
	 * @see XMLElement#XMLElement()
	 * @see XMLElement#XMLElement(boolean)
	 * @see XMLElement#XMLElement(java.util.HashMap,boolean) XMLElement(HashMap,
	 *      boolean)
	 */
	public XMLElement(HashMap<Object, Object> entities) {
		this(entities, false, true, true);
	}

	/**
	 * Creates and initializes a new XML element. Calling the construction is
	 * equivalent to:
	 * <ul>
	 * <li>{@code new XMLElement(new HashMap(), skipLeadingWhitespace, true)
	 * }</li>
	 * </ul>
	 *
	 * @param skipLeadingWhitespace {@code true} if leading and trailing whitespace
	 *                              in PCDATA content has to be removed.
	 *
	 *                              <dl>
	 *                              <dt><b>Postconditions:</b></dt>
	 *                              <dd>
	 *                              <ul>
	 *                              <li>countChildren() =&amp; 0
	 *                              <li>iterateChildren() =&amp; empty enumeration
	 *                              <li>enumeratePropertyNames() =&amp; empty
	 *                              enumeration
	 *                              <li>getChildren() =&amp; empty vector
	 *                              <li>getContent() =&amp; ""
	 *                              <li>getLineNr() =&amp; 0
	 *                              <li>getName() =&amp; null
	 *                              </ul>
	 *                              </dd>
	 *                              </dl>
	 *
	 * @see XMLElement#XMLElement()
	 * @see XMLElement#XMLElement(java.util.HashMap) XMLElement(HashMap)
	 * @see XMLElement#XMLElement(java.util.HashMap,boolean) XMLElement(HashMap,
	 *      boolean)
	 */
	public XMLElement(boolean skipLeadingWhitespace) {
		this(new HashMap<>(), skipLeadingWhitespace, true, true);
	}

	/**
	 * Creates and initializes a new XML element. Calling the construction is
	 * equivalent to:
	 * <ul>
	 * <li>{@code new XMLElement(entities, skipLeadingWhitespace, true)
	 * }</li>
	 * </ul>
	 *
	 * @param entities              The entity conversion table.
	 * @param skipLeadingWhitespace {@code true} if leading and trailing whitespace
	 *                              in PCDATA content has to be removed.
	 *
	 *                              <dl>
	 *                              <dt><b>Preconditions:</b></dt>
	 *                              <dd>
	 *                              <ul>
	 *                              <li>{@code entities != null}
	 *                              </ul>
	 *                              </dd>
	 *                              </dl>
	 *
	 *                              <dl>
	 *                              <dt><b>Postconditions:</b></dt>
	 *                              <dd>
	 *                              <ul>
	 *                              <li>countChildren() =&amp; 0
	 *                              <li>iterateChildren() =&amp; empty enumeration
	 *                              <li>enumeratePropertyNames() =&amp; empty
	 *                              enumeration
	 *                              <li>getChildren() =&amp; empty vector
	 *                              <li>getContent() =&amp; ""
	 *                              <li>getLineNr() =&amp; 0
	 *                              <li>getName() =&amp; null
	 *                              </ul>
	 *                              </dd>
	 *                              </dl>
	 *
	 * @see XMLElement#XMLElement()
	 * @see XMLElement#XMLElement(boolean)
	 * @see XMLElement#XMLElement(java.util.HashMap) XMLElement(HashMap)
	 */
	public XMLElement(HashMap<Object, Object> entities, boolean skipLeadingWhitespace) {
		this(entities, skipLeadingWhitespace, true, true);
	}

	/**
	 * Creates and initializes a new XML element.
	 *
	 * @param entities              The entity conversion table.
	 * @param skipLeadingWhitespace {@code true} if leading and trailing whitespace
	 *                              in PCDATA content has to be removed.
	 * @param ignoreCase            {@code true} if the case of element and
	 *                              attribute names have to be ignored.
	 *
	 *                              <dl>
	 *                              <dt><b>Preconditions:</b></dt>
	 *                              <dd>
	 *                              <ul>
	 *                              <li>{@code entities != null}
	 *                              </ul>
	 *                              </dd>
	 *                              </dl>
	 *
	 *                              <dl>
	 *                              <dt><b>Postconditions:</b></dt>
	 *                              <dd>
	 *                              <ul>
	 *                              <li>countChildren() =&amp; 0
	 *                              <li>iterateChildren() =&amp; empty enumeration
	 *                              <li>enumeratePropertyNames() =&amp; empty
	 *                              enumeration
	 *                              <li>getChildren() =&amp; empty vector
	 *                              <li>getContent() =&amp; ""
	 *                              <li>getLineNr() =&amp; 0
	 *                              <li>getName() =&amp; null
	 *                              </ul>
	 *                              </dd>
	 *                              </dl>
	 *
	 * @see XMLElement#XMLElement()
	 * @see XMLElement#XMLElement(boolean)
	 * @see XMLElement#XMLElement(java.util.HashMap) XMLElement(HashMap)
	 * @see XMLElement#XMLElement(java.util.HashMap,boolean) XMLElement(HashMap,
	 *      boolean)
	 */
	public XMLElement(HashMap<Object, Object> entities, boolean skipLeadingWhitespace, boolean ignoreCase) {
		this(entities, skipLeadingWhitespace, true, ignoreCase);
	}

	/**
	 * Creates and initializes a new XML element.
	 * <P>
	 * This constructor should <I>only</I> be called from
	 * {@link #createAnotherElement() createAnotherElement} to create child
	 * iterator.
	 *
	 * @param entities                 The entity conversion table.
	 * @param skipLeadingWhitespace    {@code true} if leading and trailing
	 *                                 whitespace in PCDATA content has to be
	 *                                 removed.
	 * @param fillBasicConversionTable {@code true} if the basic entities need to be
	 *                                 added to the entity list.
	 * @param ignoreCase               {@code true} if the case of element and
	 *                                 attribute names have to be ignored.
	 *
	 *                                 <dl>
	 *                                 <dt><b>Preconditions:</b></dt>
	 *                                 <dd>
	 *                                 <ul>
	 *                                 <li>{@code entities != null}
	 *                                 <li>if
	 *                                 {@code fillBasicConversionTable == false}
	 *                                 then {@code entities} contains at least the
	 *                                 following entries: {@code amp}, {@code lt},
	 *                                 {@code gt}, {@code apos} and {@code quot}
	 *                                 </ul>
	 *                                 </dd>
	 *                                 </dl>
	 *
	 *                                 <dl>
	 *                                 <dt><b>Postconditions:</b></dt>
	 *                                 <dd>
	 *                                 <ul>
	 *                                 <li>countChildren() =&amp; 0
	 *                                 <li>iterateChildren() =&amp; empty
	 *                                 enumeration
	 *                                 <li>enumeratePropertyNames() =&amp; empty
	 *                                 enumeration
	 *                                 <li>getChildren() =&amp; empty vector
	 *                                 <li>getContent() =&amp; ""
	 *                                 <li>getLineNr() =&amp; 0
	 *                                 <li>getName() =&amp; null
	 *                                 </ul>
	 *                                 </dd>
	 *                                 </dl>
	 *
	 * @see XMLElement#createAnotherElement()
	 */
	protected XMLElement(HashMap<Object, Object> entities, boolean skipLeadingWhitespace,
			boolean fillBasicConversionTable, boolean ignoreCase) {
		this.ignoreWhitespace = skipLeadingWhitespace;
		this.ignoreCase = ignoreCase;
		this.name = null;
		this.contents = "";
		this.attributes = new HashMap<>();
		this.children = new ArrayList<>();
		this.entities = entities;
		this.lineNr = 0;
		Iterator<Object> iter = this.entities.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object value = this.entities.get(key);
			if (value instanceof String) {
				value = ((String) value).toCharArray();
				this.entities.put(key, value);
			}
		}
		if (fillBasicConversionTable) {
			this.entities.put("amp", new char[] { '&' });
			this.entities.put("quot", new char[] { '"' });
			this.entities.put("apos", new char[] { '\'' });
			this.entities.put("lt", new char[] { '<' });
			this.entities.put("gt", new char[] { '>' });
		}
	}

	/**
	 * Adds a child element.
	 *
	 * @param child The child element to add.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code child != null}
	 *              <li>{@code child.getName() != null}
	 *              <li>{@code child} does not have a parent element
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 *              <dl>
	 *              <dt><b>Postconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>countChildren() =&gt; old.countChildren() + 1
	 *              <li>iterateChildren() =&gt; old.iterateChildren() + child
	 *              <li>getChildren() =&gt; old.iterateChildren() + child
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 * @see XMLElement#countChildren()
	 * @see XMLElement#iterateChildren()
	 * @see XMLElement#getChildren()
	 * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
	 */
	public void addChild(XMLElement child) {
		this.children.add(child);
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param name  The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code name != null}
	 *              <li>{@code name} is a valid XML identifier
	 *              <li>{@code value != null}
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 *              <dl>
	 *              <dt><b>Postconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>enumerateAttributeNames() =&gt;
	 *              old.enumerateAttributeNames() + name
	 *              <li>getAttribute(name) =&gt; value
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, Object)
	 * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
	 *      getStringAttribute(String, String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getStringAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public void setAttribute(String name, Object value) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		this.attributes.put(name, value.toString());
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param name  The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 * @deprecated Use {@link #setAttribute(java.lang.String, java.lang.Object)
	 *             setAttribute} instead.
	 */
	@Deprecated
	public void addProperty(String name, Object value) {
		this.setAttribute(name, value);
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param name  The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code name != null}
	 *              <li>{@code name} is a valid XML identifier
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 *              <dl>
	 *              <dt><b>Postconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>enumerateAttributeNames() =&gt;
	 *              old.enumerateAttributeNames() + name
	 *              <li>getIntAttribute(name) =&gt; value
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getIntAttribute(java.lang.String) getIntAttribute(String)
	 * @see XMLElement#getIntAttribute(java.lang.String, int)
	 *      getIntAttribute(String, int)
	 * @see XMLElement#getIntAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getIntAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public void setIntAttribute(String name, int value) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		this.attributes.put(name, Integer.toString(value));
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param key   The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 * @deprecated Use {@link #setIntAttribute(java.lang.String, int)
	 *             setIntAttribute} instead.
	 */
	@Deprecated
	public void addProperty(String key, int value) {
		this.setIntAttribute(key, value);
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param name  The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code name != null}
	 *              <li>{@code name} is a valid XML identifier
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 *              <dl>
	 *              <dt><b>Postconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>enumerateAttributeNames() =&gt;
	 *              old.enumerateAttributeNames() + name
	 *              <li>getDoubleAttribute(name) =&gt; value
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getDoubleAttribute(java.lang.String)
	 *      getDoubleAttribute(String)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, double)
	 *      getDoubleAttribute(String, double)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getDoubleAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public void setDoubleAttribute(String name, double value) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		this.attributes.put(name, Double.toString(value));
	}

	/**
	 * Adds or modifies an attribute.
	 *
	 * @param name  The name of the attribute.
	 * @param value The value of the attribute.
	 *
	 * @deprecated Use {@link #setDoubleAttribute(java.lang.String, double)
	 *             setDoubleAttribute} instead.
	 */
	@Deprecated
	public void addProperty(String name, double value) {
		this.setDoubleAttribute(name, value);
	}

	/**
	 * Returns the number of child iterator of the element.
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code result &gt;= 0}
	 * </ul>
	 * </dd>
	 * </dl>
	 *
	 * @see XMLElement#addChild(XMLElement) addChild(XMLElement)
	 * @see XMLElement#iterateChildren()
	 * @see XMLElement#getChildren()
	 * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
	 */
	public int countChildren() {
		return this.children.size();
	}

	/**
	 * Enumerates the attribute names.
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code result != null}
	 * </ul>
	 * </dd>
	 * </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, String)
	 * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
	 *      getStringAttribute(String, String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getStringAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getIntAttribute(java.lang.String) getIntAttribute(String)
	 * @see XMLElement#getIntAttribute(java.lang.String, int)
	 *      getIntAttribute(String, int)
	 * @see XMLElement#getIntAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getIntAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getDoubleAttribute(java.lang.String)
	 *      getDoubleAttribute(String)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, double)
	 *      getDoubleAttribute(String, double)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getDoubleAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getBooleanAttribute(java.lang.String, java.lang.String,
	 *      java.lang.String, boolean) getBooleanAttribute(String, String, String,
	 *      boolean)
	 */
	public Iterator<String> enumerateAttributeNames() {
		return this.attributes.keySet().iterator();
	}

	/**
	 * Enumerates the attribute names.
	 *
	 * @deprecated Use {@link #enumerateAttributeNames() enumerateAttributeNames}
	 *             instead.
	 */
	@Deprecated
	public Iterator<String> enumeratePropertyNames() {
		return this.enumerateAttributeNames();
	}

	/**
	 * Enumerates the child iterator.
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code result != null}
	 * </ul>
	 * </dd>
	 * </dl>
	 *
	 * @see XMLElement#addChild(XMLElement) addChild(XMLElement)
	 * @see XMLElement#countChildren()
	 * @see XMLElement#getChildren()
	 * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
	 */
	public Iterator<XMLElement> iterateChildren() {
		return this.children.iterator();
	}

	/**
	 * Returns the child iterator as a ArrayList. It is safe to modify this
	 * ArrayList.
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code result != null}
	 * </ul>
	 * </dd>
	 * </dl>
	 *
	 * @see XMLElement#addChild(XMLElement) addChild(XMLElement)
	 * @see XMLElement#countChildren()
	 * @see XMLElement#iterateChildren()
	 * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
	 */
	public ArrayList<XMLElement> getChildren() {
		ArrayList<XMLElement> clonedList = new ArrayList<>();
		if (this.children != null) {
			clonedList.addAll(this.children);
		}
		return clonedList;
	}

	/**
	 * Returns the PCDATA content of the object. If there is no such content,
	 * <CODE>null</CODE> is returned.
	 *
	 * @see XMLElement#setContent(java.lang.String) setContent(String)
	 */
	public String getContent() {
		return this.contents;
	}

	/**
	 * Returns the line nr in the source data on which the element is found. This
	 * method returns {@code 0} there is no associated source data.
	 *
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code result &gt;= 0}
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	public int getLineNr() {
		return this.lineNr;
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code null} is returned.
	 *
	 * @param name The name of the attribute.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, Object)
	 * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public Object getAttribute(String name) {
		return this.getAttribute(name, null);
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code defaultValue} is returned.
	 *
	 * @param name         The name of the attribute.
	 * @param defaultValue Key to use if the attribute is missing.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>{@code name != null}
	 *                     <li>{@code name} is a valid XML identifier
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public Object getAttribute(String name, Object defaultValue) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		Object value = this.attributes.get(name);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Returns an attribute by looking up a key in a hashtable. If the attribute
	 * doesn't exist, the value corresponding to defaultKey is returned.
	 * <P>
	 * As an example, if valueSet contains the mapping {@code "one" =&gt;
	 * "1"} and the element contains the attribute {@code attr="one"}, then
	 * {@code getAttribute("attr", mapping, defaultKey, false)} returns {@code "1"}.
	 *
	 * @param name          The name of the attribute.
	 * @param valueSet      HashMap mapping keySet().iterator to values.
	 * @param defaultKey    Key to use if the attribute is missing.
	 * @param allowLiterals {@code true} if literals are valid.
	 *
	 *                      <dl>
	 *                      <dt><b>Preconditions:</b></dt>
	 *                      <dd>
	 *                      <ul>
	 *                      <li>{@code name != null}
	 *                      <li>{@code name} is a valid XML identifier
	 *                      <li>{@code valueSet} != null
	 *                      <li>the keySet().iterator of {@code valueSet} are
	 *                      strings
	 *                      </ul>
	 *                      </dd>
	 *                      </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, Object)
	 */
	public Object getAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey,
			boolean allowLiterals) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		Object key = this.attributes.get(name);
		Object result;
		if (key == null) {
			key = defaultKey;
		}
		result = valueSet.get(key);
		if (result == null) {
			if (allowLiterals) {
				result = key;
			} else {
				throw this.invalidValue(name, (String) key);
			}
		}
		return result;
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code null} is returned.
	 *
	 * @param name The name of the attribute.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
	 *      getStringAttribute(String, String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getStringAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public String getStringAttribute(String name) {
		return this.getStringAttribute(name, null);
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code defaultValue} is returned.
	 *
	 * @param name         The name of the attribute.
	 * @param defaultValue Key to use if the attribute is missing.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>{@code name != null}
	 *                     <li>{@code name} is a valid XML identifier
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getStringAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public String getStringAttribute(String name, String defaultValue) {
		return (String) this.getAttribute(name, defaultValue);
	}

	/**
	 * Returns an attribute by looking up a key in a hashtable. If the attribute
	 * doesn't exist, the value corresponding to defaultKey is returned.
	 * <P>
	 * As an example, if valueSet contains the mapping {@code "one" =&gt;
	 * "1"} and the element contains the attribute {@code attr="one"}, then
	 * {@code getAttribute("attr", mapping, defaultKey, false)} returns {@code "1"}.
	 *
	 * @param name          The name of the attribute.
	 * @param valueSet      HashMap mapping keySet().iterator to values.
	 * @param defaultKey    Key to use if the attribute is missing.
	 * @param allowLiterals {@code true} if literals are valid.
	 *
	 *                      <dl>
	 *                      <dt><b>Preconditions:</b></dt>
	 *                      <dd>
	 *                      <ul>
	 *                      <li>{@code name != null}
	 *                      <li>{@code name} is a valid XML identifier
	 *                      <li>{@code valueSet} != null
	 *                      <li>the keySet().iterator of {@code valueSet} are
	 *                      strings
	 *                      <li>the values of {@code valueSet} are strings
	 *                      </ul>
	 *                      </dd>
	 *                      </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
	 *      getStringAttribute(String, String)
	 */
	public String getStringAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey,
			boolean allowLiterals) {
		return (String) this.getAttribute(name, valueSet, defaultKey, allowLiterals);
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code 0} is returned.
	 *
	 * @param name The name of the attribute.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getIntAttribute(java.lang.String, int)
	 *      getIntAttribute(String, int)
	 * @see XMLElement#getIntAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getIntAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public int getIntAttribute(String name) {
		return this.getIntAttribute(name, 0);
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code defaultValue} is returned.
	 *
	 * @param name         The name of the attribute.
	 * @param defaultValue Key to use if the attribute is missing.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>{@code name != null}
	 *                     <li>{@code name} is a valid XML identifier
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 *
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getIntAttribute(java.lang.String) getIntAttribute(String)
	 * @see XMLElement#getIntAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getIntAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public int getIntAttribute(String name, int defaultValue) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		String value = this.attributes.get(name);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw this.invalidValue(name, value);
			}
		}
	}

	/**
	 * Returns an attribute by looking up a key in a hashtable. If the attribute
	 * doesn't exist, the value corresponding to defaultKey is returned.
	 * <P>
	 * As an example, if valueSet contains the mapping {@code "one" =&amp; 1} and
	 * the element contains the attribute {@code attr="one"}, then
	 * {@code getIntAttribute("attr", mapping, defaultKey, false)} returns
	 * {@code 1}.
	 *
	 * @param name                The name of the attribute.
	 * @param valueSet            HashMap mapping keySet().iterator to values.
	 * @param defaultKey          Key to use if the attribute is missing.
	 * @param allowLiteralNumbers {@code true} if literal numbers are valid.
	 *
	 *                            <dl>
	 *                            <dt><b>Preconditions:</b></dt>
	 *                            <dd>
	 *                            <ul>
	 *                            <li>{@code name != null}
	 *                            <li>{@code name} is a valid XML identifier
	 *                            <li>{@code valueSet} != null
	 *                            <li>the keySet().iterator of {@code valueSet} are
	 *                            strings
	 *                            <li>the values of {@code valueSet} are Integer
	 *                            objects
	 *                            <li>{@code defaultKey} is either {@code null}, a
	 *                            key in {@code valueSet} or an integer.
	 *                            </ul>
	 *                            </dd>
	 *                            </dl>
	 *
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getIntAttribute(java.lang.String) getIntAttribute(String)
	 * @see XMLElement#getIntAttribute(java.lang.String, int)
	 *      getIntAttribute(String, int)
	 */
	public int getIntAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey,
			boolean allowLiteralNumbers) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		Object key = this.attributes.get(name);
		Integer result;
		if (key == null) {
			key = defaultKey;
		}
		try {
			result = (Integer) valueSet.get(key);
		} catch (ClassCastException e) {
			throw this.invalidValueSet(name);
		}
		if (result == null) {
			if (!allowLiteralNumbers) {
				throw this.invalidValue(name, (String) key);
			}
			try {
				result = Integer.valueOf((String) key);
			} catch (NumberFormatException e) {
				throw this.invalidValue(name, (String) key);
			}
		}
		return result.intValue();
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code 0.0} is returned.
	 *
	 * @param name The name of the attribute.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getDoubleAttribute(java.lang.String, double)
	 *      getDoubleAttribute(String, double)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getDoubleAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public double getDoubleAttribute(String name) {
		return this.getDoubleAttribute(name, 0.);
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code defaultValue} is returned.
	 *
	 * @param name         The name of the attribute.
	 * @param defaultValue Key to use if the attribute is missing.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>{@code name != null}
	 *                     <li>{@code name} is a valid XML identifier
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getDoubleAttribute(java.lang.String)
	 *      getDoubleAttribute(String)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getDoubleAttribute(String, HashMap, String,
	 *      boolean)
	 */
	public double getDoubleAttribute(String name, double defaultValue) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		String value = this.attributes.get(name);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Double.valueOf(value).doubleValue();
			} catch (NumberFormatException e) {
				throw this.invalidValue(name, value);
			}
		}
	}

	/**
	 * Returns an attribute by looking up a key in a hashtable. If the attribute
	 * doesn't exist, the value corresponding to defaultKey is returned.
	 * <P>
	 * As an example, if valueSet contains the mapping {@code "one" =&gt;
	 * 1.0} and the element contains the attribute {@code attr="one"}, then
	 * {@code getDoubleAttribute("attr", mapping, defaultKey, false)} returns
	 * {@code 1.0}.
	 *
	 * @param name                The name of the attribute.
	 * @param valueSet            HashMap mapping keySet().iterator to values.
	 * @param defaultKey          Key to use if the attribute is missing.
	 * @param allowLiteralNumbers {@code true} if literal numbers are valid.
	 *
	 *                            <dl>
	 *                            <dt><b>Preconditions:</b></dt>
	 *                            <dd>
	 *                            <ul>
	 *                            <li>{@code name != null}
	 *                            <li>{@code name} is a valid XML identifier
	 *                            <li>{@code valueSet != null}
	 *                            <li>the keySet().iterator of {@code valueSet} are
	 *                            strings
	 *                            <li>the values of {@code valueSet} are Double
	 *                            objects
	 *                            <li>{@code defaultKey} is either {@code null}, a
	 *                            key in {@code valueSet} or a double.
	 *                            </ul>
	 *                            </dd>
	 *                            </dl>
	 *
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#getDoubleAttribute(java.lang.String)
	 *      getDoubleAttribute(String)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, double)
	 *      getDoubleAttribute(String, double)
	 */
	public double getDoubleAttribute(String name, HashMap<Object, Object> valueSet, String defaultKey,
			boolean allowLiteralNumbers) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		Object key = this.attributes.get(name);
		Double result;
		if (key == null) {
			key = defaultKey;
		}
		try {
			result = (Double) valueSet.get(key);
		} catch (ClassCastException e) {
			throw this.invalidValueSet(name);
		}
		if (result == null) {
			if (!allowLiteralNumbers) {
				throw this.invalidValue(name, (String) key);
			}
			try {
				result = Double.valueOf((String) key);
			} catch (NumberFormatException e) {
				throw this.invalidValue(name, (String) key);
			}
		}
		return result.doubleValue();
	}

	/**
	 * Returns an attribute of the element. If the attribute doesn't exist,
	 * {@code defaultValue} is returned. If the value of the attribute is equal to
	 * {@code trueValue}, {@code true} is returned. If the value of the attribute is
	 * equal to {@code falseValue}, {@code false} is returned. If the value doesn't
	 * match {@code trueValue} or {@code falseValue}, an exception is thrown.
	 *
	 * @param name         The name of the attribute.
	 * @param trueValue    The value associated with {@code true}.
	 * @param falseValue   The value associated with {@code true}.
	 * @param defaultValue Value to use if the attribute is missing.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>{@code name != null}
	 *                     <li>{@code name} is a valid XML identifier
	 *                     <li>{@code trueValue} and {@code falseValue} are
	 *                     different strings.
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 *
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#removeAttribute(java.lang.String) removeAttribute(String)
	 * @see XMLElement#enumerateAttributeNames()
	 */
	public boolean getBooleanAttribute(String name, String trueValue, String falseValue, boolean defaultValue) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		Object value = this.attributes.get(name);
		if (value == null) {
			return defaultValue;
		} else if (value.equals(trueValue)) {
			return true;
		} else if (value.equals(falseValue)) {
			return false;
		} else {
			throw this.invalidValue(name, (String) value);
		}
	}

	/**
	 * Returns the name of the element.
	 *
	 * @see XMLElement#setName(java.lang.String) setName(String)
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Reads one XML element from a java.io.Reader and parses it.
	 *
	 * @param reader The reader from which to retrieve the XML data.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code reader != null}
	 *               <li>{@code reader} is not closed
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>the state of the receiver is updated to reflect the XML
	 *               element parsed from the reader
	 *               <li>the reader points to the first character following the last
	 *               '&gt;' character of the XML element
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws java.io.IOException If an error occured while reading the input.
	 * @throws XMLParseException   If an error occured while parsing the read data.
	 */
	public void parseFromReader(Reader reader) throws IOException, XMLParseException {
		this.parseFromReader(reader, /* startingLineNr */ 1);
	}

	/**
	 * Reads one XML element from a java.io.Reader and parses it.
	 *
	 * @param reader         The reader from which to retrieve the XML data.
	 * @param startingLineNr The line number of the first line in the data.
	 *
	 *                       <dl>
	 *                       <dt><b>Preconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>{@code reader != null}
	 *                       <li>{@code reader} is not closed
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 *                       <dl>
	 *                       <dt><b>Postconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>the state of the receiver is updated to reflect the
	 *                       XML element parsed from the reader
	 *                       <li>the reader points to the first character following
	 *                       the last '&gt;' character of the XML element
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 * @throws java.io.IOException If an error occured while reading the input.
	 * @throws XMLParseException   If an error occured while parsing the read data.
	 */
	public void parseFromReader(Reader reader, int startingLineNr) throws IOException, XMLParseException {
		this.name = null;
		this.contents = "";
		this.attributes = new HashMap<>();
		this.children = new ArrayList<>();
		this.charReadTooMuch = '\0';
		this.reader = reader;
		this.parserLineNr = startingLineNr;

		for (;;) {
			char ch = this.scanWhitespace();

			if (ch != '<') {
				throw this.expectedInput("<");
			}

			ch = this.readChar();

			if ((ch == '!') || (ch == '?')) {
				this.skipSpecialTag(0);
			} else {
				this.unreadChar(ch);
				this.scanElement(this);
				return;
			}
		}
	}

	/**
	 * Reads one XML element from a String and parses it.
	 *
	 * @param string The String from which to retrieve the XML data.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code string != null}
	 *               <li>{@code string.length() &gt; 0}
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>the state of the receiver is updated to reflect the XML
	 *               element parsed from the reader
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseString(String string) throws XMLParseException {
		try {
			this.parseFromReader(new StringReader(string), /* startingLineNr */ 1);
		} catch (IOException e) {
			// Java exception handling suxx
		}
	}

	/**
	 * Reads one XML element from a String and parses it.
	 *
	 * @param string The String from which to retrieve the XML data.
	 * @param offset The first character in {@code string} to scan.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code string != null}
	 *               <li>{@code offset &lt; string.length()}
	 *               <li>{@code offset &gt;= 0}
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>the state of the receiver is updated to reflect the XML
	 *               element parsed from the reader
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseString(String string, int offset) throws XMLParseException {
		this.parseString(string.substring(offset));
	}

	/**
	 * Reads one XML element from a String and parses it.
	 *
	 * @param string The String from which to retrieve the XML data.
	 * @param offset The first character in {@code string} to scan.
	 * @param end    The character where to stop scanning. This character is not
	 *               scanned.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code string != null}
	 *               <li>{@code end &lt;= string.length()}
	 *               <li>{@code offset &lt; end}
	 *               <li>{@code offset &gt;= 0}
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>the state of the receiver is updated to reflect the XML
	 *               element parsed from the reader
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseString(String string, int offset, int end) throws XMLParseException {
		this.parseString(string.substring(offset, end));
	}

	/**
	 * Reads one XML element from a String and parses it.
	 *
	 * @param string         The String from which to retrieve the XML data.
	 * @param offset         The first character in {@code string} to scan.
	 * @param end            The character where to stop scanning. This character is
	 *                       not scanned.
	 * @param startingLineNr The line number of the first line in the data.
	 *
	 *                       <dl>
	 *                       <dt><b>Preconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>{@code string != null}
	 *                       <li>{@code end &lt;= string.length()}
	 *                       <li>{@code offset &lt; end}
	 *                       <li>{@code offset &gt;= 0}
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 *                       <dl>
	 *                       <dt><b>Postconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>the state of the receiver is updated to reflect the
	 *                       XML element parsed from the reader
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseString(String string, int offset, int end, int startingLineNr) throws XMLParseException {
		string = string.substring(offset, end);
		try {
			this.parseFromReader(new StringReader(string), startingLineNr);
		} catch (IOException e) {
			// Java exception handling suxx
		}
	}

	/**
	 * Reads one XML element from a char array and parses it.
	 *
	 * @param input  The char array from which to retrieve the XML data.
	 * @param offset The first character in {@code string} to scan.
	 * @param end    The character where to stop scanning. This character is not
	 *               scanned.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code input != null}
	 *               <li>{@code end &lt;= input.length}
	 *               <li>{@code offset &lt; end}
	 *               <li>{@code offset &gt;= 0}
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>the state of the receiver is updated to reflect the XML
	 *               element parsed from the reader
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseCharArray(char[] input, int offset, int end) throws XMLParseException {
		this.parseCharArray(input, offset, end, /* startingLineNr */ 1);
	}

	/**
	 * Reads one XML element from a char array and parses it.
	 *
	 * @param input          The char array from which to retrieve the XML data.
	 * @param offset         The first character in {@code string} to scan.
	 * @param end            The character where to stop scanning. This character is
	 *                       not scanned.
	 * @param startingLineNr The line number of the first line in the data.
	 *
	 *                       <dl>
	 *                       <dt><b>Preconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>{@code input != null}
	 *                       <li>{@code end &lt;= input.length}
	 *                       <li>{@code offset &lt; end}
	 *                       <li>{@code offset &gt;= 0}
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 *                       <dl>
	 *                       <dt><b>Postconditions:</b></dt>
	 *                       <dd>
	 *                       <ul>
	 *                       <li>the state of the receiver is updated to reflect the
	 *                       XML element parsed from the reader
	 *                       </ul>
	 *                       </dd>
	 *                       </dl>
	 *
	 * @throws XMLParseException If an error occured while parsing the string.
	 */
	public void parseCharArray(char[] input, int offset, int end, int startingLineNr) throws XMLParseException {
		try {
			Reader reader = new CharArrayReader(input, offset, end);
			this.parseFromReader(reader, startingLineNr);
		} catch (IOException e) {
			// This exception will never happen.
		}
	}

	/**
	 * Removes a child element.
	 *
	 * @param child The child element to remove.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code child != null}
	 *              <li>{@code child} is a child element of the receiver
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 *              <dl>
	 *              <dt><b>Postconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>countChildren() =&amp; old.countChildren() - 1
	 *              <li>iterateChildren() =&amp; old.iterateChildren() - child
	 *              <li>getChildren() =&amp; old.iterateChildren() - child
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 *
	 * @see XMLElement#addChild(XMLElement) addChild(XMLElement)
	 * @see XMLElement#countChildren()
	 * @see XMLElement#iterateChildren()
	 * @see XMLElement#getChildren()
	 */
	public void removeChild(XMLElement child) {
		this.children.remove(child);
	}

	/**
	 * Removes an attribute.
	 *
	 * @param name The name of the attribute.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 *             <dl>
	 *             <dt><b>Postconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>enumerateAttributeNames() =&amp;
	 *             old.enumerateAttributeNames() - name
	 *             <li>getAttribute(name) =&amp; {@code null}
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#enumerateAttributeNames()
	 * @see XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see XMLElement#setAttribute(java.lang.String, java.lang.Object)
	 *      setAttribute(String, Object)
	 * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
	 * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, Object)
	 * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
	 *      getStringAttribute(String, String)
	 * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getStringAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getIntAttribute(java.lang.String) getIntAttribute(String)
	 * @see XMLElement#getIntAttribute(java.lang.String, int)
	 *      getIntAttribute(String, int)
	 * @see XMLElement#getIntAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getIntAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getDoubleAttribute(java.lang.String)
	 *      getDoubleAttribute(String)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, double)
	 *      getDoubleAttribute(String, double)
	 * @see XMLElement#getDoubleAttribute(java.lang.String, java.util.HashMap,
	 *      java.lang.String, boolean) getDoubleAttribute(String, HashMap, String,
	 *      boolean)
	 * @see XMLElement#getBooleanAttribute(java.lang.String, java.lang.String,
	 *      java.lang.String, boolean) getBooleanAttribute(String, String, String,
	 *      boolean)
	 */
	public void removeAttribute(String name) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		this.attributes.remove(name);
	}

	/**
	 * Removes an attribute.
	 *
	 * @param name The name of the attribute.
	 *
	 * @deprecated Use {@link #removeAttribute(java.lang.String) removeAttribute}
	 *             instead.
	 */
	@Deprecated
	public void removeProperty(String name) {
		this.removeAttribute(name);
	}

	/**
	 * Removes an attribute.
	 *
	 * @param name The name of the attribute.
	 *
	 * @deprecated Use {@link #removeAttribute(java.lang.String) removeAttribute}
	 *             instead.
	 */
	@Deprecated
	public void removeChild(String name) {
		this.removeAttribute(name);
	}

	/**
	 * Creates a new similar XML element.
	 * <P>
	 * You should override this method when subclassing XMLElement.
	 */
	public XMLElement createAnotherElement() {
		return new XMLElement(this.entities, this.ignoreWhitespace, false, this.ignoreCase);
	}

	/**
	 * Changes the content string.
	 *
	 * @param content The new content string.
	 */
	public void setContent(String content) {
		this.contents = content;
	}

	/**
	 * Changes the name of the element.
	 *
	 * @param name The new name.
	 *
	 * @deprecated Use {@link #setName(java.lang.String) setName} instead.
	 */
	@Deprecated
	public void setTagName(String name) {
		this.setName(name);
	}

	/**
	 * Changes the name of the element.
	 *
	 * @param name The new name.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name} is a valid XML identifier
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 *
	 * @see XMLElement#getName()
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Writes the XML element to a string.
	 *
	 * @see XMLElement#write(java.io.Writer) write(Writer)
	 */
	@Override
	public String toString() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out);
			this.write(writer);
			writer.flush();
			return new String(out.toByteArray());
		} catch (IOException e) {
			// Java exception handling suxx
			return super.toString();
		}
	}

	/**
	 * Writes the XML element to a writer.
	 *
	 * @param writer The writer to write the XML data to.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code writer != null}
	 *               <li>{@code writer} is not closed
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 * @throws java.io.IOException If the data could not be written to the writer.
	 *
	 * @see XMLElement#toString()
	 */
	public void write(Writer writer) throws IOException {
		if (this.name == null) {
			XMLElement.writeEncoded(writer, this.contents);
			return;
		}
		writer.write('<');
		writer.write(this.name);
		if (!this.attributes.isEmpty()) {
			Iterator<String> iter = this.attributes.keySet().iterator();
			while (iter.hasNext()) {
				writer.write(' ');
				String key = iter.next();
				String value = this.attributes.get(key);
				writer.write(key);
				writer.write('=');
				writer.write('"');
				XMLElement.writeEncoded(writer, value);
				writer.write('"');
			}
		}
		if ((this.contents != null) && (this.contents.length() > 0)) {
			writer.write('>');
			XMLElement.writeEncoded(writer, this.contents);
			writer.write('<');
			writer.write('/');
			writer.write(this.name);
			writer.write('>');
		} else if (this.children.isEmpty()) {
			writer.write('/');
			writer.write('>');
		} else {
			writer.write('>');
			Iterator<XMLElement> iter = this.iterateChildren();
			while (iter.hasNext()) {
				XMLElement child = iter.next();
				child.write(writer);
			}
			writer.write('<');
			writer.write('/');
			writer.write(this.name);
			writer.write('>');
		}
	}

	/**
	 * Writes a string encoded to a writer.
	 *
	 * @param writer The writer to write the XML data to.
	 * @param str    The string to write encoded.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code writer != null}
	 *               <li>{@code writer} is not closed
	 *               <li>{@code str != null}
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 */
	protected static void writeEncoded(Writer writer, String str) throws IOException {
		for (int i = 0; i < str.length(); i += 1) {
			char ch = str.charAt(i);
			switch (ch) {
			case '<':
				writer.write('&');
				writer.write('l');
				writer.write('t');
				writer.write(';');
				break;
			case '>':
				writer.write('&');
				writer.write('g');
				writer.write('t');
				writer.write(';');
				break;
			case '&':
				writer.write('&');
				writer.write('a');
				writer.write('m');
				writer.write('p');
				writer.write(';');
				break;
			case '"':
				writer.write('&');
				writer.write('q');
				writer.write('u');
				writer.write('o');
				writer.write('t');
				writer.write(';');
				break;
			case '\'':
				writer.write('&');
				writer.write('a');
				writer.write('p');
				writer.write('o');
				writer.write('s');
				writer.write(';');
				break;
			default:
				int unicode = ch;
				if ((unicode < 32) || (unicode > 126)) {
					writer.write('&');
					writer.write('#');
					writer.write('x');
					writer.write(Integer.toString(unicode, 16));
					writer.write(';');
				} else {
					writer.write(ch);
				}
			}
		}
	}

	/**
	 * Scans an identifier from the current reader. The scanned identifier is
	 * appended to {@code result}.
	 *
	 * @param result The buffer in which the scanned identifier will be put.
	 *
	 *               <dl>
	 *               <dt><b>Preconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>{@code result != null}
	 *               <li>The next character read from the reader is a valid first
	 *               character of an XML identifier.
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 *
	 *               <dl>
	 *               <dt><b>Postconditions:</b></dt>
	 *               <dd>
	 *               <ul>
	 *               <li>The next character read from the reader won't be an
	 *               identifier character.
	 *               </ul>
	 *               </dd>
	 *               </dl>
	 */
	protected void scanIdentifier(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z')) && ((ch < '0') || (ch > '9')) && (ch != '_')
					&& (ch != '.') && (ch != ':') && (ch != '-') && (ch <= '\u007E')) {
				this.unreadChar(ch);
				return;
			}
			result.append(ch);
		}
	}

	/**
	 * This method scans an identifier from the current reader.
	 *
	 * @return the next character following the whitespace.
	 */
	protected char scanWhitespace() throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	/**
	 * This method scans an identifier from the current reader. The scanned
	 * whitespace is appended to {@code result}.
	 *
	 * @return the next character following the whitespace.
	 *
	 *         <dl>
	 *         <dt><b>Preconditions:</b></dt>
	 *         <dd>
	 *         <ul>
	 *         <li>{@code result != null}
	 *         </ul>
	 *         </dd>
	 *         </dl>
	 */
	protected char scanWhitespace(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
				result.append(ch);
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	/**
	 * This method scans a delimited string from the current reader. The scanned
	 * string without delimiters is appended to {@code string}.
	 *
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code string != null}
	 * <li>the next char read is the string delimiter
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	protected void scanString(StringBuffer string) throws IOException {
		char delimiter = this.readChar();
		if ((delimiter != '\'') && (delimiter != '"')) {
			throw this.expectedInput("' or \"");
		}
		for (;;) {
			char ch = this.readChar();
			if (ch == delimiter) {
				return;
			} else if (ch == '&') {
				this.resolveEntity(string);
			} else {
				string.append(ch);
			}
		}
	}

	/**
	 * Scans a #PCDATA element. CDATA sections and entities are resolved. The next
	 * &lt; char is skipped. The scanned data is appended to {@code data}.
	 *
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code data != null}
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	protected void scanPCData(StringBuffer data) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (ch == '<') {
				ch = this.readChar();
				if (ch == '!') {
					this.checkCDATA(data);
				} else {
					this.unreadChar(ch);
					return;
				}
			} else if (ch == '&') {
				this.resolveEntity(data);
			} else {
				data.append(ch);
			}
		}
	}

	/**
	 * Scans a special tag and if the tag is a CDATA section, append its content to
	 * {@code buf}.
	 *
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>{@code buf != null}
	 * <li>The first &lt; has already been read.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	protected boolean checkCDATA(StringBuffer buf) throws IOException {
		char ch = this.readChar();
		if (ch != '[') {
			this.unreadChar(ch);
			this.skipSpecialTag(0);
			return false;
		} else if (!this.checkLiteral("CDATA[")) {
			this.skipSpecialTag(1); // one [ has already been read
			return false;
		} else {
			int delimiterCharsSkipped = 0;
			while (delimiterCharsSkipped < 3) {
				ch = this.readChar();
				switch (ch) {
				case ']':
					if (delimiterCharsSkipped < 2) {
						delimiterCharsSkipped += 1;
					} else {
						buf.append(']');
						buf.append(']');
						delimiterCharsSkipped = 0;
					}
					break;
				case '>':
					if (delimiterCharsSkipped < 2) {
						for (int i = 0; i < delimiterCharsSkipped; i++) {
							buf.append(']');
						}
						delimiterCharsSkipped = 0;
						buf.append('>');
					} else {
						delimiterCharsSkipped = 3;
					}
					break;
				default:
					for (int i = 0; i < delimiterCharsSkipped; i += 1) {
						buf.append(']');
					}
					buf.append(ch);
					delimiterCharsSkipped = 0;
				}
			}
			return true;
		}
	}

	/**
	 * Skips a comment.
	 *
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The first &lt;!-- has already been read.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	protected void skipComment() throws IOException {
		int dashesToRead = 2;
		while (dashesToRead > 0) {
			char ch = this.readChar();
			if (ch == '-') {
				dashesToRead -= 1;
			} else {
				dashesToRead = 2;
			}
		}
		if (this.readChar() != '>') {
			throw this.expectedInput(">");
		}
	}

	/**
	 * Skips a special tag or comment.
	 *
	 * @param bracketLevel The number of open square brackets ([) that have already
	 *                     been read.
	 *
	 *                     <dl>
	 *                     <dt><b>Preconditions:</b></dt>
	 *                     <dd>
	 *                     <ul>
	 *                     <li>The first &lt;! has already been read.
	 *                     <li>{@code bracketLevel >= 0}
	 *                     </ul>
	 *                     </dd>
	 *                     </dl>
	 */
	protected void skipSpecialTag(int bracketLevel) throws IOException {
		int tagLevel = 1; // <
		char stringDelimiter = '\0';
		if (bracketLevel == 0) {
			char ch = this.readChar();
			if (ch == '[') {
				bracketLevel += 1;
			} else if (ch == '-') {
				ch = this.readChar();
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				} else if (ch == '-') {
					this.skipComment();
					return;
				}
			}
		}
		while (tagLevel > 0) {
			char ch = this.readChar();
			if (stringDelimiter == '\0') {
				if ((ch == '"') || (ch == '\'')) {
					stringDelimiter = ch;
				} else if (bracketLevel <= 0) {
					if (ch == '<') {
						tagLevel += 1;
					} else if (ch == '>') {
						tagLevel -= 1;
					}
				}
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				}
			} else {
				if (ch == stringDelimiter) {
					stringDelimiter = '\0';
				}
			}
		}
	}

	/**
	 * Scans the data for literal text. Scanning stops when a character does not
	 * match or after the complete text has been checked, whichever comes first.
	 *
	 * @param literal the literal to check.
	 *
	 *                <dl>
	 *                <dt><b>Preconditions:</b></dt>
	 *                <dd>
	 *                <ul>
	 *                <li>{@code literal != null}
	 *                </ul>
	 *                </dd>
	 *                </dl>
	 */
	protected boolean checkLiteral(String literal) throws IOException {
		int length = literal.length();
		for (int i = 0; i < length; i += 1) {
			if (this.readChar() != literal.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads a character from a reader.
	 */
	protected char readChar() throws IOException {
		if (this.charReadTooMuch != '\0') {
			char ch = this.charReadTooMuch;
			this.charReadTooMuch = '\0';
			return ch;
		} else {
			int i = this.reader.read();
			if (i < 0) {
				throw this.unexpectedEndOfData();
			} else if (i == 10) {
				this.parserLineNr += 1;
				return '\n';
			} else {
				return (char) i;
			}
		}
	}

	/**
	 * Scans an XML element.
	 *
	 * @param elt The element that will contain the result.
	 *
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The first &lt; has already been read.
	 *            <li>{@code elt != null}
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	protected void scanElement(XMLElement elt) throws IOException {
		StringBuffer buf = new StringBuffer();
		this.scanIdentifier(buf);
		String name = buf.toString();
		elt.setName(name);
		char ch = this.scanWhitespace();
		while ((ch != '>') && (ch != '/')) {
			buf.setLength(0);
			this.unreadChar(ch);
			this.scanIdentifier(buf);
			String key = buf.toString();
			ch = this.scanWhitespace();
			if (ch != '=') {
				throw this.expectedInput("=");
			}
			this.unreadChar(this.scanWhitespace());
			buf.setLength(0);
			this.scanString(buf);
			elt.setAttribute(key, buf);
			ch = this.scanWhitespace();
		}
		if (ch == '/') {
			ch = this.readChar();
			if (ch != '>') {
				throw this.expectedInput(">");
			}
			return;
		}
		buf.setLength(0);
		ch = this.scanWhitespace(buf);
		if (ch != '<') {
			this.unreadChar(ch);
			this.scanPCData(buf);
		} else {
			for (;;) {
				ch = this.readChar();
				if (ch == '!') {
					if (this.checkCDATA(buf)) {
						this.scanPCData(buf);
						break;
					} else {
						ch = this.scanWhitespace(buf);
						if (ch != '<') {
							this.unreadChar(ch);
							this.scanPCData(buf);
							break;
						}
					}
				} else {
					if ((ch != '/') || this.ignoreWhitespace) {
						buf.setLength(0);
					}
					if (ch == '/') {
						this.unreadChar(ch);
					}
					break;
				}
			}
		}
		if (buf.length() == 0) {
			while (ch != '/') {
				if (ch == '!') {
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					this.skipComment();
				} else {
					this.unreadChar(ch);
					XMLElement child = this.createAnotherElement();
					this.scanElement(child);
					elt.addChild(child);
				}
				ch = this.scanWhitespace();
				if (ch != '<') {
					throw this.expectedInput("<");
				}
				ch = this.readChar();
			}
			this.unreadChar(ch);
		} else {
			if (this.ignoreWhitespace) {
				elt.setContent(buf.toString().trim());
			} else {
				elt.setContent(buf.toString());
			}
		}
		ch = this.readChar();
		if (ch != '/') {
			throw this.expectedInput("/");
		}
		this.unreadChar(this.scanWhitespace());
		if (!this.checkLiteral(name)) {
			throw this.expectedInput(name);
		}
		if (this.scanWhitespace() != '>') {
			throw this.expectedInput(">");
		}
	}

	/**
	 * Resolves an entity. The name of the entity is read from the reader. The value
	 * of the entity is appended to {@code buf}.
	 *
	 * @param buf Where to put the entity value.
	 *
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The first &amp; has already been read.
	 *            <li>{@code buf != null}
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	protected void resolveEntity(StringBuffer buf) throws IOException {
		char ch = '\0';
		StringBuffer keyBuf = new StringBuffer();
		for (;;) {
			ch = this.readChar();
			if (ch == ';') {
				break;
			}
			keyBuf.append(ch);
		}
		String key = keyBuf.toString();
		if (key.charAt(0) == '#') {
			try {
				if (key.charAt(1) == 'x') {
					ch = (char) Integer.parseInt(key.substring(2), 16);
				} else {
					ch = (char) Integer.parseInt(key.substring(1), 10);
				}
			} catch (NumberFormatException e) {
				throw this.unknownEntity(key);
			}
			buf.append(ch);
		} else {
			char[] value = (char[]) this.entities.get(key);
			if (value == null) {
				throw this.unknownEntity(key);
			}
			buf.append(value);
		}
	}

	/**
	 * Pushes a character back to the read-back buffer.
	 *
	 * @param ch The character to push back.
	 *
	 *           <dl>
	 *           <dt><b>Preconditions:</b></dt>
	 *           <dd>
	 *           <ul>
	 *           <li>The read-back buffer is empty.
	 *           <li>{@code ch != '\0'}
	 *           </ul>
	 *           </dd>
	 *           </dl>
	 */
	protected void unreadChar(char ch) {
		this.charReadTooMuch = ch;
	}

	/**
	 * Creates a parse exception for when an invalid valueset is given to a method.
	 *
	 * @param name The name of the entity.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 */
	protected XMLParseException invalidValueSet(String name) {
		String msg = "Invalid value set (entity name = \"" + name + "\")";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Creates a parse exception for when an invalid value is given to a method.
	 *
	 * @param name  The name of the entity.
	 * @param value The value of the entity.
	 *
	 *              <dl>
	 *              <dt><b>Preconditions:</b></dt>
	 *              <dd>
	 *              <ul>
	 *              <li>{@code name != null}
	 *              <li>{@code value != null}
	 *              </ul>
	 *              </dd>
	 *              </dl>
	 */
	protected XMLParseException invalidValue(String name, String value) {
		String msg = "Attribute \"" + name + "\" does not contain a valid " + "value (\"" + value + "\")";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Creates a parse exception for when the end of the data input has been
	 * reached.
	 */
	protected XMLParseException unexpectedEndOfData() {
		String msg = "Unexpected end of data reached";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Creates a parse exception for when a syntax error occured.
	 *
	 * @param context The context in which the error occured.
	 *
	 *                <dl>
	 *                <dt><b>Preconditions:</b></dt>
	 *                <dd>
	 *                <ul>
	 *                <li>{@code context != null}
	 *                <li>{@code context.length() &gt; 0}
	 *                </ul>
	 *                </dd>
	 *                </dl>
	 */
	protected XMLParseException syntaxError(String context) {
		String msg = "Syntax error while parsing " + context;
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Creates a parse exception for when the next character read is not the
	 * character that was expected.
	 *
	 * @param charSet The set of characters (in human readable form) that was
	 *                expected.
	 *
	 *                <dl>
	 *                <dt><b>Preconditions:</b></dt>
	 *                <dd>
	 *                <ul>
	 *                <li>{@code charSet != null}
	 *                <li>{@code charSet.length() &gt; 0}
	 *                </ul>
	 *                </dd>
	 *                </dl>
	 */
	protected XMLParseException expectedInput(String charSet) {
		String msg = "Expected: " + charSet;
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Creates a parse exception for when an entity could not be resolved.
	 *
	 * @param name The name of the entity.
	 *
	 *             <dl>
	 *             <dt><b>Preconditions:</b></dt>
	 *             <dd>
	 *             <ul>
	 *             <li>{@code name != null}
	 *             <li>{@code name.length() &gt; 0}
	 *             </ul>
	 *             </dd>
	 *             </dl>
	 */
	protected XMLParseException unknownEntity(String name) {
		String msg = "Unknown or invalid entity: &" + name + ";";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

}
