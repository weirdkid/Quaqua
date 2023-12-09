/*
 * @(#)InactivatableColorUIResource.java  
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

import javax.swing.plaf.UIResource;

/**
 * InactivatableColorUIResource is a color, that can be rendered using an an
 * active state and an inactive state.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class InactivatableColorUIResource extends PaintableColor implements UIResource {
	private static final long serialVersionUID = 1L;
	private boolean isActive;
	private Color active;
	private Color inactive;
	private boolean isTransparent;

	/** Creates a new instance. */
	public InactivatableColorUIResource(int activeRGB, int inactiveRGB) {
		super(activeRGB);
		this.active = new Color(activeRGB);
		this.inactive = new Color(inactiveRGB);
	}

	public InactivatableColorUIResource(int activeRGB, int inactiveRGB, boolean hasAlpha) {
		super(activeRGB, hasAlpha);
		this.active = new Color(activeRGB, hasAlpha);
		this.inactive = new Color(inactiveRGB, hasAlpha);
	}

	public InactivatableColorUIResource(Color active, Color inactive) {
		super(active.getRGB(), true);
		this.active = active;
		this.inactive = inactive;
	}

	public void setActive(boolean newValue) {
		isActive = newValue;
	}

	public void setTransparent(boolean newValue) {
		isTransparent = newValue;
	}

	@Override
	public int getTransparency() {
		return (isTransparent) ? Transparency.TRANSLUCENT : super.getTransparency();
	}

	@Override
	public int getAlpha() {
		return (isTransparent) ? 0x0 : super.getAlpha();
	}

	@Override
	public int getRGB() {
		return (isTransparent) ? 0x0 : ((isActive) ? active.getRGB() : inactive.getRGB());

	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle r, Rectangle2D r2d, AffineTransform xform,
			RenderingHints hints) {
		return (isActive) ? active.createContext(cm, r, r2d, xform, hints)
				: inactive.createContext(cm, r, r2d, xform, hints);
	}

	@Override
	public Paint getPaint(Component c, int x, int y, int width, int height) {
		Color clr = (isActive) ? active : inactive;
		if (clr instanceof PaintableColor) {
			return ((PaintableColor) clr).getPaint(c, x, y, width, height);
		} else {
			return clr;
		}
	}

}
