/*
 * @(#)ButtonFocusBorder.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.border.Border;

import ch.randelshofer.quaqua.QuaquaUtilities;

/**
 * A Border which only draws if the component has focus.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class FocusBorder implements Border {
	private Border focusRing;

	/** Creates a new instance. */
	public FocusBorder(Border focusRing) {
		this.focusRing = focusRing;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return focusRing.getBorderInsets(c);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if (c.isEnabled() && QuaquaUtilities.isFocused(c)
				&& (!(c instanceof AbstractButton) || ((AbstractButton) c).isFocusPainted())) {
			focusRing.paintBorder(c, g, x, y, width, height);
		}
	}
}
