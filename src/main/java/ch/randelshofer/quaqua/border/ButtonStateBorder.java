/*
 * @(#)ButtonStateBorder.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.Border;

import ch.randelshofer.quaqua.QuaquaBorderFactory;
import ch.randelshofer.quaqua.QuaquaUtilities;
import ch.randelshofer.quaqua.util.Images;

/**
 * ButtonStateBorder.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ButtonStateBorder implements Border {

	public final static int E = 0;
	public final static int EP = 1;
	public final static int ES = 2;
	public final static int EPS = 3;
	public final static int D = 4;
	public final static int DS = 5;
	public final static int I = 6;
	public final static int IS = 7;
	public final static int DI = 8;
	public final static int DIS = 9;
	public final static int DEFAULT = 10;
	/**
	 * Borders
	 */
	private Border[] borders;
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
	 * Whether the tiledImage needs to be tiled horizontally or vertically to get
	 * the icons out of it.
	 */
	private boolean isTiledHorizontaly;
	private Insets borderInsets;
	/** Only used for tiled image. */
	private boolean fill;
	/** Only used for tiled image. */
	private Insets imageInsets;

	/**
	 * Creates a new instance. All borders must have the same insets. If a border is
	 * null, nothing is drawn for this state.
	 */
	public ButtonStateBorder(Border e, Border ep, Border es, Border eps, Border d, Border ds, Border i, Border is,
			Border di, Border dis) {
		borders = new Border[DEFAULT + 1];
		borders[E] = e;
		borders[EP] = ep;
		borders[ES] = es;
		borders[EPS] = eps;
		borders[D] = d;
		borders[DS] = ds;
		borders[I] = i;
		borders[IS] = is;
		borders[DI] = dis;
		borders[DIS] = dis;
	}

	/**
	 * Creates a new instance. All borders must have the same insets.
	 */
	public ButtonStateBorder(Border[] borders) {
		this.borders = new Border[DEFAULT + 1];
		System.arraycopy(borders, 0, this.borders, 0, Math.min(borders.length, this.borders.length));
	}

	/**
	 * Creates a new instance. All borders must have the same dimensions.
	 */
	public ButtonStateBorder(Image[] images, Insets imageInsets, Insets borderInsets, boolean fill) {
		this.borders = new Border[DEFAULT + 1];
		for (int i = 0, n = Math.min(images.length, borders.length); i < n; i++) {
			if (images[i] != null) {
				borders[i] = QuaquaBorderFactory.create(images[i], imageInsets, borderInsets, fill);
			}
		}
	}

	/**
	 * Creates a new instance. All borders must have the same dimensions.
	 */
	public ButtonStateBorder(Image tiledImage, int tileCount, boolean isTiledHorizontaly, Insets imageInsets,
			Insets borderInsets, boolean fill) {
		this.tiledImage = tiledImage;
		this.tileCount = tileCount;
		this.isTiledHorizontaly = isTiledHorizontaly;
		this.imageInsets = imageInsets;
		this.borderInsets = borderInsets;
		this.fill = fill;
	}

	public void setBorder(int key, Border b) {
		borders[key] = b;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		if (borderInsets != null) {
			return (Insets) borderInsets.clone();
		} else {
			generateBordersFromTiledImage();
			return (Insets) borders[0].getBorderInsets(c).clone();
		}
	}

	@Override
	public boolean isBorderOpaque() {
		generateBordersFromTiledImage();
		return borders[0].isBorderOpaque();
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		generateBordersFromTiledImage();
		Border border = getBorder(c);
		if (border != null) {
			border.paintBorder(c, g, x, y, width, height);
		}
	}

	protected Border getBorder(Component c) {
		Border border;
		boolean isActive = QuaquaUtilities.isOnActiveWindow(c);

		if (c instanceof AbstractButton) {
			ButtonModel model = ((AbstractButton) c).getModel();
			if (isActive) {
				if (model.isEnabled()) {
					if (model.isPressed() && model.isArmed()) {
						if (model.isSelected()) {
							border = borders[EPS];
						} else {
							border = borders[EP];
						}
					} else if (model.isSelected()) {
						border = borders[ES];
					} else {
						if (!model.isPressed() && borders[DEFAULT] != null && (c instanceof JButton)
								&& ((JButton) c).isDefaultButton()) {
							border = borders[DEFAULT];
						} else {
							border = borders[E];
						}
					}
				} else {
					if (model.isSelected()) {
						border = borders[DS];
					} else {
						border = borders[D];
					}
				}
			} else {
				if (model.isEnabled()) {
					if (model.isSelected()) {
						border = borders[IS];
					} else {
						border = borders[I];
					}
				} else {
					if (model.isSelected()) {
						border = borders[DIS];
					} else {
						border = borders[DI];
					}
				}
			}
		} else {
			if (isActive) {
				if (c.isEnabled()) {
					border = borders[E];
				} else {
					border = borders[D];
				}
			} else {
				if (c.isEnabled()) {
					border = borders[I];
				} else {
					border = borders[DI];
				}
			}
		}
		return border;
	}

	private void generateBordersFromTiledImage() {
		if (borders == null) {
			borders = new Border[DEFAULT + 1];
			Image[] images = Images.split(tiledImage, tileCount, isTiledHorizontaly);
			for (int i = 0, n = Math.min(images.length, borders.length); i < n; i++) {
				borders[i] = QuaquaBorderFactory.create(images[i], imageInsets, borderInsets, fill);
			}
			generateMissingBorders();
			tiledImage = null;
		}
	}

	private void generateMissingBorders() {
	}

	public static class UIResource extends ButtonStateBorder implements javax.swing.plaf.UIResource {

		public UIResource(Border[] borders) {
			super(borders);
		}

		/**
		 * Creates a new instance. All borders must have the same dimensions.
		 */
		public UIResource(Image[] images, Insets imageInsets, Insets borderInsets, boolean fill) {
			super(images, imageInsets, borderInsets, fill);
		}

		/**
		 * Creates a new instance. All borders must have the same dimensions.
		 */
		public UIResource(Image tiledImage, int tileCount, boolean isTiledHorizontaly, Insets imageInsets,
				Insets borderInsets, boolean fill) {
			super(tiledImage, tileCount, isTiledHorizontaly, imageInsets, borderInsets, fill);
		}
	}
}