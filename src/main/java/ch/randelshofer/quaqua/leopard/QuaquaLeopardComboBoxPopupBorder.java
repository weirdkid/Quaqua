/*
 * @(#)QuaquaLeopardComboBoxPopupBorder.java
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.leopard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.Border;

import ch.randelshofer.quaqua.QuaquaUtilities;

/**
 * A replacement for the AquaComboBoxPopupBorder.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaLeopardComboBoxPopupBorder implements Border {

	protected static Insets popupBorderInsets;
	protected static Insets itemBorderInsets;

	@Override
	public void paintBorder(Component component, Graphics gr, int x, int y, int width, int height) {
		Graphics2D g = (Graphics2D) gr;
		Object oldHints = QuaquaUtilities.beginGraphics(g);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Punch out a hole and then draw a rounded rectangle over it
		Composite composite = g.getComposite();
		g.setComposite(AlphaComposite.Src);
		g.setColor(new Color(0xffffff, true));
		g.fillRect(x, y, width, height);
		g.setComposite(composite);
		g.setColor(Color.WHITE);
		g.fill(new RoundRectangle2D.Float(x, y, width, height, 10f, 10f));
		QuaquaUtilities.endGraphics(g, oldHints);
	}

	@Override
	public Insets getBorderInsets(Component component) {
		return new Insets(4, 0, 4, 0);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}
}
