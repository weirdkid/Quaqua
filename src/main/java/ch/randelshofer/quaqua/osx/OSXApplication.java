/*
 * @(#)OSXApplication.java
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import ch.randelshofer.quaqua.QuaquaIconFactory;
import ch.randelshofer.quaqua.QuaquaManager;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.tiff.TIFFDecodeParam;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.tiff.TIFFImageDecoder;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.util.MemoryCacheSeekableStream;
import ch.randelshofer.quaqua.util.Images;
import ch.randelshofer.quaqua.util.Methods;

/**
 * {@code OSXApplication} can get the native Mac OS X icon image of the
 * application, and it can make the application icon bounce in the Dock.
 * <p>
 * The functionality is performed using the Cocoa class <a href=
 * "http://developer.apple.com/documentation/Cocoa/Reference/ApplicationKit/Classes/NSApplication_Class/Reference/Reference.html"
 * >NSApplication</a>.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class OSXApplication {

	private final static boolean DEBUG = true;
	/**
	 * This variable is set to true, if native code is available.
	 */
	private static volatile Boolean isNativeCodeAvailable;
	/**
	 * Version of the native code library.
	 */
	private final static int EXPECTED_NATIVE_CODE_VERSION = 7;
	/** This lock is used for synchronizing calls to nativeGetIconImage. */
	private final static Object ICON_IMAGE_LOCK = new Object();

	/**
	 * Load the native code.
	 */
	private static boolean isNativeCodeAvailable() {
		return false;
	}

	/** Prevent instance creation. */
	private OSXApplication() {
	}

	/**
	 * Requests user attention through JNI or through Cocoa Java. This method will
	 * fail silently if neither JNI nor Cocoa Java is available.
	 *
	 * @param requestCritical Set this to true, if your application invokes a modal
	 *                        dialog. Set this to false, in all other cases.
	 */
	public static void requestUserAttention(boolean requestCritical) {
		if (isNativeCodeAvailable()) {
			nativeRequestUserAttention(true);
		} else {
			// We may only use the Java to Cocoa Bridge when we run on OS X.
			// If we run on Darwin unter OS X, this will crash our application
			// with the following console message:
			// "ObjCJava FATAL: Detected more than one VM... ObjCJava Exit".

			if (QuaquaManager.isOSX()) {
				/*
				 * NSApplication app = NSApplication.sharedApplication(); int id =
				 * app.requestUserAttention( NSApplication.UserAttentionRequestInformational);
				 */
				try {
					Object app = Methods.invokeStatic("com.apple.cocoa.application.NSApplication", "sharedApplication");
					Methods.invoke(app, "requestUserAttention",
							app.getClass().getDeclaredField("UserAttentionRequestInformational").getInt(app));
				} catch (Throwable ex) {
					System.err.println("Quaqua Warning: Couldn't invoke NSApplication.requestUserAttention");
				}
			}
		}
	}

	/**
	 * Requests user attention through JNI.
	 * 
	 * @param requestCritical Set this to true, if your application invokes a modal
	 *                        dialog. Set this to false, in all other cases.
	 * @exception java.lang.UnsatisfiedLinkError if JNI is not available.
	 */
	private static native void nativeRequestUserAttention(boolean requestCritical);

	/**
	 * Returns the icon image of the application.
	 *
	 * @param size the desired size of the icon in pixels (width and height)
	 * @return The application image. Returns a generic application image if JNI is
	 *         not available.
	 */
	public static BufferedImage getIconImage(int size) {
		BufferedImage image = null;
		if (isNativeCodeAvailable()) {
			try {
				byte[] tiffData;
				synchronized (ICON_IMAGE_LOCK) {
					tiffData = nativeGetIconImage(size);
				}

				TIFFImageDecoder decoder = new TIFFImageDecoder(
						new MemoryCacheSeekableStream(new ByteArrayInputStream(tiffData)), new TIFFDecodeParam());

				RenderedImage rImg = decoder.decodeAsRenderedImage(0);
				image = Images.toBufferedImage(rImg);
				/*
				 * if (rImg instanceof BufferedImage) { image = (BufferedImage) rImg; } else {
				 * Raster r = rImg.getData(); WritableRaster wr =
				 * WritableRaster.createWritableRaster( r.getSampleModel(), null);
				 * rImg.copyData(wr); image = new BufferedImage( rImg.getColorModel(), wr,
				 * rImg.getColorModel().isAlphaPremultiplied(), null ); }
				 */
			} catch (IOException ex) {
				if (DEBUG) {
					ex.printStackTrace();
					// suppress, we return a default image
				}
			}
		}

		if (image == null) {
			image = Images.toBufferedImage(Images.createImage(
					QuaquaIconFactory.class.getResource("/ch/randelshofer/quaqua/images/ApplicationIcon.png")));
		}

		if (image.getWidth() != size) {
			image = Images.toBufferedImage(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));
		}

		return image;
	}

	/**
	 * Returns the icon image of the application through JNI.
	 *
	 * @param size the desired size of the icon in pixels (width and height)
	 * @return Byte array with TIFF image data or null in case of failure.
	 */
	private static synchronized native byte[] nativeGetIconImage(int size);

	/**
	 * Returns the version of the native code library. If the version does not match
	 * with the version that we expect, we can not use it.
	 * 
	 * @return The version number of the native code.
	 */
	private static native int nativeGetNativeCodeVersion();
}
