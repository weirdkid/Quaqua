/*
 * @(#)QuaquaColorWellBorder.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * QuaquaColorWellBorder.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaColorWellBorder implements Border {
	private Border squareButtonBorder;

	/** Creates a new instance. */
	public QuaquaColorWellBorder() {
		this(QuaquaBorderFactory.createSquareButtonBorder());
	}

	public QuaquaColorWellBorder(Border squareButtonBorder) {
		this.squareButtonBorder = squareButtonBorder;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(5, 5, 5, 5);
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		squareButtonBorder.paintBorder(c, g, x, y, width, height);
		g.setColor(c.getBackground());
		g.fillRect(x + 6, y + 6, width - 12, height - 12);
		g.setColor(c.getBackground().darker());
		g.drawRect(x + 5, y + 5, width - 11, height - 11);
	}

	@Override
	public boolean isBorderOpaque() {
		return squareButtonBorder.isBorderOpaque();
	}
}
