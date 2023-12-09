/*
 * @(#)BackgroundBorderUIResource.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

/**
 * A BackgroundBorderUIResource is used by the Quaqua Look And Feel to tag a
 * BorderUIResource that has to be drawn on to the background of a JComponent.
 * <p>
 * It is used like a regular Border object, the BackgroundBorderUIResource works
 * like an EmptyBorder. It just has insets, but draws nothing. Using the
 * getBackgroundBorder method, one can retrieve the background border used to
 * draw on the background of a JComponent.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BackgroundBorderUIResource implements Border, BackgroundBorder, PressedCueBorder, UIResource {
	private Border backgroundBorder;

	/**
	 * Creates an EmptyBorder which has the same insets as the specified background
	 * border.
	 */
	public BackgroundBorderUIResource(Border backgroundBorder) {
		this.backgroundBorder = backgroundBorder;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return backgroundBorder.getBorderInsets(c);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		// do nothing
	}

	@Override
	public Border getBackgroundBorder() {
		return backgroundBorder;
	}

	@Override
	public boolean hasPressedCue(JComponent c) {
		if (backgroundBorder instanceof PressedCueBorder) {
			return ((PressedCueBorder) backgroundBorder).hasPressedCue(c);
		}
		return true;
	}
}
