/*
 * @(#)MultiIcon.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.icon;

import java.awt.Component;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ch.randelshofer.quaqua.util.Images;

/**
 * An icon which paints one out of multiple icons depending on the state of the
 * component. MultiIcon can lazily create the icons from a tiled image.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class MultiIcon implements Icon {

	/**
	 * The icons from which we choose from. This variable is null, if we are using a
	 * tiled image as our base.
	 */
	protected Icon[] icons;
	/**
	 * Holds the icon pictures in a single image. This variable is used only until
	 * we create the icons array. Then it is set to null.
	 */
	private Image tiledImage;
	/**
	 * The number of icons in the tiledImage.
	 */
	private int tileCount;
	/**
	 * Whether the tiledImage needs to be tiled horizontaly or vertically to get the
	 * icons out of it.
	 */
	private boolean isTiledHorizontaly;

	/**
	 * Creates a new instance from an array of icons. All icons must have the same
	 * dimensions. If an icon is null, an icon is derived for the state from the
	 * other icons.
	 * <p>
	 * Note: For efficiency reasons this method stores the passed in array
	 * internally without copying it. Do not modify the array after invoking this
	 * method.
	 */
	public MultiIcon(Icon[] icons) {
		if (icons == null) {
			throw new IllegalArgumentException("'icons' must not be null");
		}
		if (icons.length == 0) {
			throw new IllegalArgumentException("'icons' array must have length > 0");
		}
		this.icons = icons;
		generateMissingIcons();
	}

	/**
	 * Creates a new instance from an array of images. All icons must have the same
	 * dimensions. If an icon is null, an icon is derived for the state from the
	 * other icons.
	 */
	public MultiIcon(Image[] images) {
		this.icons = new Icon[images.length];
		for (int i = 0, n = icons.length; i < n; i++) {
			if (images[i] != null) {
				icons[i] = new ImageIcon(images[i]);
			}
		}
		generateMissingIcons();
	}

	/**
	 * Creates a new instance. The icon representations are created lazily from the
	 * tiled image.
	 */
	public MultiIcon(Image tiledImage, int tileCount, boolean isTiledHorizontaly) {
		this.tiledImage = tiledImage;
		this.tileCount = tileCount;
		this.isTiledHorizontaly = isTiledHorizontaly;
	}

	@Override
	public int getIconHeight() {
		generateIconsFromTiledImage();
		return (icons[0] == null) ? 0 : icons[0].getIconHeight();
	}

	@Override
	public int getIconWidth() {
		generateIconsFromTiledImage();
		return (icons[0] == null) ? 0 : icons[0].getIconWidth();
	}

	@Override
	public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
		generateIconsFromTiledImage();
		Icon icon = getIcon(c);
		if (icon != null) {
			icon.paintIcon(c, g, x, y);
		}
	}

	private void generateIconsFromTiledImage() {
		if (icons == null) {
			icons = new Icon[tileCount];
			Image[] images = Images.split(tiledImage, tileCount, isTiledHorizontaly);
			for (int i = 0, n = Math.min(images.length, icons.length); i < n; i++) {
				if (images[i] != null) {
					icons[i] = new ImageIcon(images[i]);
				}
			}
			generateMissingIcons();
			tiledImage = null;
		}
	}

	protected abstract Icon getIcon(Component c);

	protected abstract void generateMissingIcons();
}
