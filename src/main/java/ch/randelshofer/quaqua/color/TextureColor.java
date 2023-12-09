/*
 * @(#)TextureColor.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import java.awt.Component;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import ch.randelshofer.quaqua.QuaquaIconFactory;
import ch.randelshofer.quaqua.util.Images;

/**
 * This class used to pass TexturePaint's 'through' the Swing API, so that users
 * of our Look and Feel can work with TexturePaint's like with regular colors,
 * but Quaqua UI components will paint using the texture instead of with the
 * color.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class TextureColor extends PaintableColor {
	private static final long serialVersionUID = 1L;
	protected Image texture;

	/** Creates a new instance. */
	public TextureColor(int rgb) {
		super(rgb);
	}

	public TextureColor(int r, int g, int b) {
		super(r, g, b);
	}

	public TextureColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public TextureColor(int r, int g, int b, Image texture) {
		super(r, g, b);
		this.texture = texture;
	}

	public TextureColor(int r, int g, int b, int a, Image texture) {
		super(r, g, b, a);
		this.texture = texture;
	}

	public TextureColor(int rgb, String location) {
		super(rgb);
		this.texture = QuaquaIconFactory.createImage(location);
	}

	public BufferedImage getTexture() {
		texture = Images.toBufferedImage(texture);
		return (BufferedImage) texture;
	}

	@Override
	public Paint getPaint(Component c, int x, int y, int width, int height) {
		BufferedImage txtr = getTexture();
		if (txtr != null) {
			Point p = getRootPaneOffset(c);
			return new TexturePaint(txtr, new Rectangle(p.x + x, p.y + y, txtr.getWidth(), txtr.getHeight()));
		} else {
			return this;
		}
	}

	public static class UIResource extends TextureColor implements javax.swing.plaf.UIResource {
		private static final long serialVersionUID = 1L;

		public UIResource(int rgb) {
			super(rgb);
		}

		public UIResource(int r, int g, int b) {
			super(r, g, b);
		}

		public UIResource(int r, int g, int b, int a) {
			super(r, g, b, a);
		}

		public UIResource(int r, int g, int b, BufferedImage texture) {
			super(r, g, b, texture);
		}

		public UIResource(int r, int g, int b, int a, BufferedImage texture) {
			super(r, g, b, a, texture);
		}

		public UIResource(int rgb, String location) {
			super(rgb, location);
		}
	}
}
