/*
 * @(#)MutableColorUIResource.java 
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

import javax.swing.plaf.UIResource;

/**
 * A ColorUIResource which can change its color.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MutableColorUIResource extends Color implements UIResource {
	private static final long serialVersionUID = 1L;
	private int argb;

	/** Creates a new instance. */
	public MutableColorUIResource(int rgb) {
		this(rgb, false);
	}

	public MutableColorUIResource(int argb, boolean hasAlpha) {
		super((hasAlpha) ? argb : 0xff000000 | argb, true);
		this.argb = argb;
	}

	public void setColor(Color newValue) {
		setRGB(newValue.getRGB());
	}

	public void setRGB(int newValue) {
		argb = newValue;
	}

	@Override
	public int getRGB() {
		return argb;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle r, Rectangle2D r2d, AffineTransform xform,
			RenderingHints hints) {
		return new Color(argb, true).createContext(cm, r, r2d, xform, hints);
	}
}
