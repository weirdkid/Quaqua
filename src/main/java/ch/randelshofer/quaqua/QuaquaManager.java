/*
 * @(#)QuaquaManager.java
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * The QuaquaManager provides bug fixes and enhancements for the Mac Look and
 * Feel and for the Aqua Look and Feel on Mac OS X.
 * <p>
 * <b>Usage for Java Applications:</b>
 * 
 * <pre>
 * UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
 * </pre>
 * <p>
 * <b>Usage for Java Applets:</b>
 * 
 * <pre>
 * UIManager.put("ClassLoader", getClass().getClassLoader());
 * UIManager.setLookAndFeel(QuaquaManager.getLookAndFeel());
 * </pre>
 * <p>
 * <b>System Properties for Java Applications:</b><br>
 * You can customize the Quaqua Look and Feel using the following system
 * properties:
 * <ul>
 * <li>{@code Quaqua.design=jaguar} Enforces Jaguar design.</li>
 * <li>{@code Quaqua.design=panther} Enforces Panther design.</li>
 * <li>{@code Quaqua.design=tiger} Enforces Tiger design.</li>
 * <li><code><b>Quaqua.design=auto</b></code> Chooses design automatically. This
 * is the default value.</li>
 * </ul>
 * <ul>
 * <li>{@code Quaqua.TabbedPane.design=jaguar} Enforces Jaguar design for tabbed
 * panes.</li>
 * <li>{@code Quaqua.TabbedPane.design=panther} Enforces Panther design for
 * tabbed panes.</li>
 * <li><code><b>Quaqua.TabbedPane.design=auto</b></code> Chooses design
 * automatically. This is the default value.</li>
 * </ul>
 * <ul>
 * <li>{@code Quaqua.FileChooser.autovalidate=false} FileChoosers do not refresh
 * their contents automatically. Users have to close and reopen a file chooser
 * to see changes in the file system.</li>
 * <li><code><b>Quaqua.FileChooser.autovalidate=true</b></code> FileChoosers
 * refresh their contents periodically. This is the default value.</li>
 * </ul>
 * <ul>
 * <li>{@code Quaqua.Debug.crossPlatform=true} Enforces cross platform support.
 * This is a hack, useful only for testing an application with the Quaqua Look
 * and Feel on non-Mac OS X platforms.</li>
 * <li><code><b>Quaqua.Debug.crossPlatform=false</b></code> Chooses native
 * support. This is the default value.</li>
 * </ul>
 * Example:
 * 
 * <pre>
 * System.setProperty("Quaqua.design", "panther");
 * System.setProperty("Quaqua.TabbedPane.design", "jaguar");
 * System.setProperty("Quaqua.FileChooser.autovalidate", "true");
 * </pre>
 * <p>
 * <b>System Properties for Java Applets:</b><br>
 * In a secure environment, you are not allowed to change system properties. Use
 * {@code QuaquaManager.setProperty} to specify (or override) system properties
 * that are used by Quaqua (that is, for all system properties listed above).
 * <p>
 * Example:
 * 
 * <pre>
 * QuaquaManager.setProperty("Quaqua.design", "panther");
 * QuaquaManager.setProperty("Quaqua.TabbedPane.design", "jaguar");
 * QuaquaManager.setProperty("Quaqua.FileChooser.autovalidate", "true");
 * </pre>
 * <p>
 * <b>Client Properties:</b><br>
 * You can customize some of the components by specifying client properties.
 * <ul>
 * <li><b>JTable</b>: {@code Quaqua.Table.style=striped} displays rows with
 * alternating colors.</li>
 * </ul>
 * <b>Specifying class loader (Java Applets):</b><br>
 * If your code runs as an Applet in a Java 1.3 VM, Swing attempts to load the
 * UI classes from the system class loader instead of from the class loader
 * which loads the applet classes. To have Swing load the UI classes using the
 * same class loader as your code, use the following code to set the Quaqua look
 * and feel on Swing's UIManager.
 * 
 * <pre>
 * UIManager.put("ClassLoader", getClass().getClassLoader());
 * UIManager.setLookAndFeel(QuaquaManager.getLookAndFeel());
 * </pre>
 *
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaManager {

	private static Properties properties;
	/**
	 * Set<String> of included Quaqua UI's. If this variable is null, all UI's of
	 * the QuaquaLookAndFeel are included.
	 */
	private static Set includedUIs;
	/**
	 * Set<String> of excluded Quaqua UI's. If this variable is null, all UI's of
	 * the QuaquaLookAndFeel are excluded.
	 */
	private static Set excludedUIs = Collections.EMPTY_SET;
	private final static String version;

	static {
		String v = null;
		try {
			InputStream s = QuaquaManager.class.getResourceAsStream("version.txt");
			if (s != null) {
				BufferedReader r = new BufferedReader(new InputStreamReader(s, "UTF8"));
				v = r.readLine();
				r.close();
			}
		} catch (IOException e) {
		}
		version = (v == null) ? "unknown" : v;
	}
	/**
	 * Mac OS X 10.0 Cheetah.
	 */
	public final static int CHEETAH = 0;
	/**
	 * Mac OS X 10.1 Puma.
	 */
	public final static int PUMA = 1;
	/**
	 * Mac OS X 10.2 Jaguar.
	 */
	public final static int JAGUAR = 2;
	/**
	 * Mac OS X 10.3 Panther.
	 */
	public final static int PANTHER = 3;
	/**
	 * Mac OS X 10.4 Tiger.
	 */
	public final static int TIGER = 4;
	/**
	 * Mac OS X 10.5 Leopard or Darwin 9.1.0.
	 */
	public final static int LEOPARD = 5;
	/**
	 * Mac OS X 10.6 Snow Leopard.
	 */
	public final static int SNOW_LEOPARD = 6;
	/**
	 * Mac OS X 10.7 Lion.
	 */
	public final static int LION = 7;
	/**
	 * Mac OS X 10.8 Mountain Lion.
	 */
	public final static int MOUNTAIN_LION = 8;
	/**
	 * Mac OS X 10.9 Mavericks.
	 */
	public final static int MAVERICKS = 9;
	/**
	 * Mac OS X 10.10 Yosemite.
	 */
	public final static int YOSEMITE = 10;
	/**
	 * Mac OS X 10.11 El Capitan.
	 */
	public final static int EL_CAPITAN = 11;
	/**
	 * Mac OS X 10.12 Sierra.
	 */
	public final static int SIERRA = 12;
	/**
	 * Mac OS X 10.13 High Sierra.
	 */
	public final static int HIGH_SIERRA = 13;
	/**
	 * Mac OS X 10.x Always points to next OSX X release.
	 */
	public final static int X = 100;
	/**
	 * Generic Linux.
	 */
	public final static int LINUX = -4;
	/**
	 * Darwin.
	 */
	public final static int DARWIN = -3;
	/**
	 * Windows.
	 */
	public final static int WINDOWS = -2;
	/**
	 * Unknown.
	 */
	public final static int UNKNOWN = -1;
	/**
	 * Current operating system.
	 */
	private static int OS;
	/**
	 * True if Mac OS X.
	 */
	private static boolean isOSX;
	/**
	 * Current design. May differ from the operating system, by setting a value in
	 * the "Quaqua.design" property.
	 */
	private static int design;

	static {
		updateDesignAndOS();
	}

	private static void updateDesignAndOS() {
		String osName = getProperty("os.name");
		String osVersion = getProperty("os.version");

		isOSX = osName.equals("Mac OS X");
		if (isOSX) {
			int p = osVersion.indexOf('.');
			p = osVersion.indexOf('.', p + 1);
			if (p != -1) {
				osVersion = osVersion.substring(0, p);
			}
			if (osVersion.equals("10.0")) {
				OS = CHEETAH;
			} else if (osVersion.equals("10.1")) {
				OS = PUMA;
			} else if (osVersion.equals("10.2")) {
				OS = JAGUAR;
			} else if (osVersion.equals("10.3")) {
				OS = PANTHER;
			} else if (osVersion.equals("10.4")) {
				OS = TIGER;
			} else if (osVersion.equals("10.5")) {
				OS = LEOPARD;
			} else if (osVersion.equals("10.6")) {
				OS = SNOW_LEOPARD;
			} else if (osVersion.equals("10.7")) {
				OS = LION;
			} else if (osVersion.equals("10.8")) {
				OS = MOUNTAIN_LION;
			} else if (osVersion.equals("10.9")) {
				OS = MAVERICKS;
			} else if (osVersion.equals("10.10")) {
				OS = YOSEMITE;
			} else if (osVersion.equals("10.11")) {
				OS = EL_CAPITAN;
			} else if (osVersion.equals("10.12")) {
				OS = SIERRA;
			} else if (osVersion.equals("10.13")) {
				OS = HIGH_SIERRA;
			} else if (osVersion.startsWith("10.")) {
				OS = X;
			} else {
				// Note: We must fall back to Snow Leopard here, because this
				// is the last OS X version for which we provide our own artwork.
				// For later OS X versions, we retrieve the artwork from
				// the system using native API's.
				OS = SNOW_LEOPARD;
			}
		} else if (osName.startsWith("Darwin")) {
			OS = DARWIN;
		} else if (osName.startsWith("Linux")) {
			OS = LINUX;
		} else if (osName.startsWith("Windows")) {
			OS = WINDOWS;
		} else {
			OS = UNKNOWN;
		}

		String osDesign = getProperty("Quaqua.design", "auto").toLowerCase();
		if (osDesign.equals("cheetah")) {
			design = JAGUAR;
		} else if (osDesign.equals("puma")) {
			design = JAGUAR;
		} else if (osDesign.equals("jaguar")) {
			design = JAGUAR;
		} else if (osDesign.equals("panther")) {
			design = PANTHER;
		} else if (osDesign.equals("tiger")) {
			design = TIGER;
		} else if (osDesign.equals("leopard")) {
			design = LEOPARD;
		} else if (osDesign.equals("snowleopard")) {
			design = SNOW_LEOPARD;
		} else if (osDesign.equals("lion")) {
			design = LION;
		} else if (osDesign.equals("mountainlion")) {
			design = MOUNTAIN_LION;
		} else if (osDesign.equals("mavericks")) {
			design = MAVERICKS;
		} else if (osDesign.equals("yosemite")) {
			design = YOSEMITE;
		} else if (osDesign.equals("elcapitan")) {
			design = EL_CAPITAN;
		} else if (osDesign.equals("sierra")) {
			design = SIERRA;
		} else if (osDesign.equals("highsierra")) {
			design = HIGH_SIERRA;
		} else {
			switch (OS) {
			case CHEETAH:
				design = JAGUAR;
				break;
			case PUMA:
				design = JAGUAR;
				break;
			case JAGUAR:
				design = JAGUAR;
				break;
			case PANTHER:
				design = PANTHER;
				break;
			case TIGER:
				design = TIGER;
				break;
			case LEOPARD:
				design = LEOPARD;
				break;
			case SNOW_LEOPARD:
				design = SNOW_LEOPARD;
				break;
			case LION:
				design = LION;
				break;
			case MOUNTAIN_LION:
				design = MOUNTAIN_LION;
				break;
			case MAVERICKS:
				design = MAVERICKS;
				break;
			case YOSEMITE:
				design = YOSEMITE;
				break;
			case EL_CAPITAN:
				design = EL_CAPITAN;
				break;
			case SIERRA:
				design = SIERRA;
				break;
			case HIGH_SIERRA:
				design = HIGH_SIERRA;
				break;
			default:
				// Note: We must fall back to Snow Leopard here, because this
				// is the last OS X version for which we provide our own artwork.
				// For later OS X versions, we retrieve the artwork from
				// the system using native API's.
				design = SNOW_LEOPARD;
				break;
			}
		}
	}

	/**
	 * Prevent instance creation.
	 */
	private QuaquaManager() {
	}

	/**
	 * Returns the current operating system.
	 *
	 * @return one of the OS constants: CHEETAH..SNOW_LEOPARD, DARWIN, WINDOWS or
	 *         UNKNOWN.
	 */
	public static int getOS() {
		return OS;
	}

	/**
	 * Returns true if the current operating system is known to be Mac OS X.
	 */
	public static boolean isOSX() {
		return OS >= CHEETAH;
	}

	/**
	 * Returns the current design of Mac OS X.
	 *
	 * @return one of the OS constants: CHEETAH..TIGER or UNKNOWN.
	 */
	public static int getDesign() {
		return design;
	}

	/**
	 * Returns the class name of a Quaqua look and feel. The class name depends on
	 * the JVM, Quaqua is running on, and on the visual design of the operating
	 * system.
	 */
	public static String getLookAndFeelClassName() {

		return "ch.randelshofer.quaqua.subset.QuaquaLeopardFileChooserLAF";
		// if I use this one, the buttons are not rendered
		// return "ch.randelshofer.quaqua.subset.Quaqua16LionFileChooserLAF";

	}

	/**
	 * Returns a Quaqua look and feel, if workarounds for the system look and feel
	 * are available. Returns a UIManager.getSystemLookAndFeelClassName() instance
	 * if no workaround is available.
	 */
	public static LookAndFeel getLookAndFeel() {
		try {
			String lafName = getLookAndFeelClassName();
			return (LookAndFeel) Class.forName(lafName).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			InternalError ie = new InternalError(e.toString());
			throw ie;
		}
	}

	/**
	 * This method returns a locally specified property, if it has been set using
	 * method {@code setProperty}. If no local property has been found, a system
	 * property using method {@code java.lang.System.getProperty(String,String} is
	 * returned.
	 * <p>
	 * This method is used to specify properties for Quaqua, when, due to security
	 * reasons, system properties can not be used, e.g. in a secure Applet
	 * environment.
	 *
	 * @see #setProperty
	 */
	public static String getProperty(String key) {
		try {
			if (properties == null || !properties.containsKey(key)) {
				return System.getProperty(key);
			} else {
				return properties.getProperty(key);
			}
		} catch (SecurityException e) {
			return null;
		}
	}

	/**
	 * This method returns a locally specified property, if it has been set using
	 * method {@code setProperty}. If no local property has been found, a system
	 * property using method {@code java.lang.System.getProperty(String,String} is
	 * returned.
	 * <p>
	 * This method is used to specify properties for Quaqua, when, due to security
	 * reasons, system properties can not be used, e.g. in a secure Applet
	 * environment.
	 *
	 * @see #setProperty
	 */
	public static String getProperty(String key, String def) {
		try {
			if (properties == null || !properties.containsKey(key)) {
				return System.getProperty(key, def);
			} else {
				return properties.getProperty(key, def);
			}
		} catch (SecurityException e) {
			return def;
		}
	}

	/**
	 * This method returns a locally specified property, if it has been set using
	 * method {@code setProperty}. If no local property has been found, a system
	 * property using method {@code java.lang.System.getProperty(String,String} is
	 * returned.
	 * <p>
	 * This method is used to specify properties for Quaqua, when, due to security
	 * reasons, system properties can not be used, e.g. in a secure Applet
	 * environment.
	 *
	 * @see #setProperty
	 */
	public static int[] getProperty(String key, int[] def) {
		String value;
		try {
			if (properties == null || !properties.containsKey(key)) {
				value = System.getProperty(key, null);
			} else {
				value = properties.getProperty(key, null);
			}
		} catch (SecurityException e) {
			value = null;
		}
		if (value != null) {
			StringTokenizer tt = new StringTokenizer(value, ",");
			if (tt.countTokens() == def.length) {
				int[] result = new int[def.length];
				try {
					for (int i = 0; i < result.length; i++) {
						result[i] = Integer.decode(tt.nextToken()).intValue();
					}
					return result;
				} catch (NumberFormatException e) {
					// continue (we return def below)
				}
			}
		}
		return def;
	}

	/**
	 * Locally defines a property.
	 * <p>
	 * Use method {@code clearProperty} to clear a local property.
	 * <p>
	 * This method is used to specify properties for Quaqua, when, due to security
	 * reasons, system properties can not be used, e.g. in a secure Applet
	 * environment.
	 * <p>
	 *
	 * @see #getProperty
	 */
	public static String setProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();
		}
		return (String) properties.setProperty(key, value);
	}

	/**
	 * Removes a locally defined property.
	 * <p>
	 * This method is used to specify properties for Quaqua, when, due to security
	 * reasons, system properties can not be used, e.g. in a secure Applet
	 * environment.
	 *
	 * @see #setProperty
	 */
	public static void removeProperty(String key) {
		if (properties != null) {
			properties.remove(key);
		}
	}

	/**
	 * Returns the version string of Quaqua. The version string is a sequence of
	 * numbers separated by full stops, followed by a blank character and a release
	 * date in ISO-format. e.g. "3.6.1 2006-03-12"
	 */
	public static String getVersion() {
		return version;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
		} catch (Exception e) {
			// empty
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					JFrame f = new JFrame("Quaqua Look and Feel");
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					JLabel label = new JLabel("<html>" + "<p align=center><b>Quaqua Look and Feel " + version
							+ "</b><br><br>" + "Copyright 2003-2013 Werner Randelshofer<br>"
							+ "All Rights Reserved.<br>" + "<br>" + "This is a software library.<br>"
							+ "Please read the accompanying documentation<br>for additional information.");
					label.setBorder(new EmptyBorder(12, 20, 20, 20));
					f.getContentPane().add(label);
					f.pack();
					f.setVisible(true);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	/**
	 * Returns true, if Quaqua uses native code for some of its functionality.
	 */
	public static boolean isNativeCodeAvailable() {
		return false;
	}

	/**
	 * Include only UI delegates with the specified names. This method must be
	 * called, before setting the QuaquaLookAndFeel to the UIManager.
	 * <p>
	 * Usage:
	 * 
	 * <pre>
	 * HashSet includes = new HashSet();
	 * includes.add("Button");
	 * QuaquaManager.setIncludeUIs(includes);
	 * </pre>
	 *
	 * @param includes Set&lt;String&gt; Only include UI delegates, which are in
	 *                 this list. Specify null to include all UIs.
	 */
	public static void setIncludedUIs(Set includes) {
		includedUIs = includes;
	}

	/**
	 * Excludes UI delegates with the specified names. This method must be called,
	 * before setting the QuaquaLookAndFeel to the UIManager.
	 * <p>
	 * Usage:
	 * 
	 * <pre>
	 * HashSet excludes = new HashSet();
	 * excludes.add("TextField");
	 * QuaquaManager.setExcludeUIs(excludes);
	 * </pre>
	 *
	 * @param excludes Set&lt;String&gt; Exclude UI delegates, which are in this
	 *                 list. Specify null to exclude all UIs.
	 */
	public static void setExcludedUIs(Set excludes) {
		excludedUIs = excludes;
	}

	/**
	 * Gets the included UI delegates, or null, if all Quaqua UI delegates shall be
	 * included into the QuaquaLookAndFeel.
	 *
	 * @return Set&lt;String&gt;.
	 */
	public static Set getIncludedUIs() {
		return includedUIs;
	}

	/**
	 * Gets the excluded UI delegates, or null, if all Quaqua UI delegates shall be
	 * excluded from the QuaquaLookAndFeel.
	 *
	 * @return Set&lt;String&gt;.
	 */
	public static Set getExcludedUIs() {
		return excludedUIs;
	}
}
