/*
 * @(#)QuaquaMenuBarUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.color.PaintableColor;

/**
 * QuaquaMenuBarUI.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaMenuBarUI extends BasicMenuBarUI {

	/** Creates a new instance. */
	public QuaquaMenuBarUI() {
	}

	public static ComponentUI createUI(JComponent x) {
		return new QuaquaMenuBarUI();
	}

	@Override
	public void paint(Graphics gr, JComponent c) {
		Graphics2D g = (Graphics2D) gr;
		int w = c.getWidth();
		int h = c.getHeight();

		g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
		g.fillRect(0, 0, w, h);

		if (c.getBorder() instanceof BackgroundBorder) {
			Border bb = ((BackgroundBorder) c.getBorder()).getBackgroundBorder();
			bb.paintBorder(c, gr, 0, 0, w, h);
		}

		Color shadow = UIManager.getColor("MenuBar.shadow");
		if (shadow != null) {
			g.setColor(shadow);
			g.fillRect(0, h - 1, w, 1);
		}
	}
}
