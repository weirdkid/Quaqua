/*
 * @(#)OSXClipboardTransferable.java
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * {@code OSXClipboardTransferable} provides read access to the Mac OS X system
 * clipboard.
 * <p>
 * All data flavors returned by this object have the mime type
 * {@code application/octet-stream; type=...}.
 * <p>
 * The following code snippet shows how to determine the native data type a
 * flavor:
 * 
 * <pre>
 * import java.net.URLDecoder;
 *
 * String nativeDataType = URLDecoder.decode(dataFlavor.getParameter("type"),"UTF-8");
 * </pre>
 * <p>
 * The system clipboard data is retrieved using the Cocoa class <a href=
 * "http://developer.apple.com/documentation/Cocoa/Reference/ApplicationKit/Classes/NSPasteboard_Class/Reference/Reference.html#//apple_ref/occ/instm/NSPasteboard/dataForType:"
 * >NSPasteboard</a>.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class OSXClipboardTransferable implements Transferable {

	/**
	 * Returns true if native code is available. This method also loads the native
	 * code.
	 */
	public static boolean isNativeCodeAvailable() {

		return false;
	}

	/**
	 * An array of String objects containing the types of data declared for the
	 * current contents of the clipboard. The returned types are listed in the order
	 * they were declared.
	 */
	private native static String[] nativeGetTypes();

	/**
	 * Returns the data for the specified type.
	 *
	 * @param dataType The type of data you want to read from the pasteboard. This
	 *                 value should be one of the types returned by #getTypes.
	 */
	private native static byte[] nativeGetDataForType(String dataType);

	/**
	 * Returns the version of the native code library. If the version does not match
	 * with the version that we expect, we can not use it.
	 * 
	 * @return The version number of the native code.
	 */
	private static native int nativeGetNativeCodeVersion();

	/**
	 * Returns the data flavors which are currently in the NSPasteboard. The mime
	 * type of all flavors is application/octet-stream. The actual type information
	 * is in the human presentable name!
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		String[] types = OSXClipboardTransferable.nativeGetTypes();

		if (types == null) {

			return new DataFlavor[0];
		} else {
			DataFlavor[] flavors = new DataFlavor[types.length];

			for (int i = 0; i < types.length; i++) {
				try {
					flavors[i] = new DataFlavor(
							"application/octet-stream; type=" + URLEncoder.encode(types[i], "UTF-8"), types[i]);
				} catch (UnsupportedEncodingException ex) {
					InternalError ie = new InternalError("URLEncoder does not support UTF-8");
					ie.initCause(ex);
					throw ie;
				}
			}
			return flavors;
		}
	}

	/**
	 * Returns true if the "General Clipboard" Cocoa NSPasteboard currently supports
	 * the specified data flavor.
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] f = getTransferDataFlavors();
		for (int i = 0; i < f.length; i++) {
			if (f[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads the data from the "General Clipboard" Cocoa NSPasteboard. If the data
	 * flavor is supported, return it as a byte array or as an input stream.
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor == null) {
			throw new NullPointerException("flavor");
		}

		String type = URLDecoder.decode(flavor.getParameter("type"), "UTF-8");

		byte[] data = nativeGetDataForType(type);

		if (data == null) {
			throw new UnsupportedFlavorException(flavor);
		}

		if (flavor.isRepresentationClassInputStream()) {
			return new ByteArrayInputStream(data);
		} else {
			return data;
		}
	}
}
