/*
 * @(#)QuaquaToolBarSeparatorUI.java  1.2  2006-06-16
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;

import ch.randelshofer.quaqua.util.Debug;

/**
 * QuaquaToolBarSeparatorUI.
 *
 * @author Werner Randelshofer
 * @version 1.2 2006-06-16 Fixed size values again. <br>
 *          1.1 2006-02-12 Fixed size values and drawing code. <br>
 *          1.0 28 March 2005 Created.
 */
public class QuaquaToolBarSeparatorUI extends BasicToolBarSeparatorUI {
	private final static Stroke separatorStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
			new float[] { 1f, 2f }, 0f);

	public static ComponentUI createUI(JComponent c) {
		return new QuaquaToolBarSeparatorUI();
	}

	@Override
	protected void installDefaults(JSeparator s) {
		super.installDefaults(s);
		s.setForeground(UIManager.getColor("ToolBarSeparator.foreground"));
	}

	@Override
	public void paint(Graphics gr, JComponent c) {
		Graphics2D g = (Graphics2D) gr;
		int width = c.getWidth();
		int height = c.getHeight();
		g.setColor(c.getForeground());
		g.setStroke(separatorStroke);
		JToolBar.Separator sep = (JToolBar.Separator) c;
		if (sep.getOrientation() == SwingConstants.HORIZONTAL) {
			g.drawLine(2, height / 2, width - 3, height / 2);
		} else {
			g.drawLine(width / 2, 2, width / 2, height - 3);
		}
		Debug.paint(g, c, this);
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		// return new Dimension(11, 11);
		JToolBar.Separator sep = (JToolBar.Separator) c;
		if (sep.getOrientation() == SwingConstants.HORIZONTAL) {
			return new Dimension(1, 11);
		} else {
			return new Dimension(11, 1);
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		// return new Dimension(11, 11);
		JToolBar.Separator sep = (JToolBar.Separator) c;
		if (sep.getOrientation() == SwingConstants.HORIZONTAL) {
			return new Dimension(1, 11);
		} else {
			return new Dimension(11, 1);
		}
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		JToolBar.Separator sep = (JToolBar.Separator) c;
		if (sep.getOrientation() == SwingConstants.HORIZONTAL) {
			return new Dimension(Integer.MAX_VALUE, 11);
		} else {
			return new Dimension(11, Integer.MAX_VALUE);
		}
	}
}
