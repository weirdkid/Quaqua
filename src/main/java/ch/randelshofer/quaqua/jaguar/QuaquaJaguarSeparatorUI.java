/*
 * @(#)QuaquaJaguarSeparatorUI.java
 *
 * Copyright (c) 2001-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.jaguar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

import ch.randelshofer.quaqua.VisuallyLayoutable;
import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.util.InsetsUtil;

/**
 * A replacement for the AquaSeparatorUI.
 * <p>
 * This class provides the following workaround for an issue in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Menu separators are drawn using a blank area instead of a black and white
 * line. This fix affects JSeparator's.</li>
 * </ul>
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id$
 */
public class QuaquaJaguarSeparatorUI extends BasicSeparatorUI implements VisuallyLayoutable {

	/** Creates a new instance of QuaquaSeparatorUI */
	public QuaquaJaguarSeparatorUI() {
	}

	public static ComponentUI createUI(JComponent c) {
		return new QuaquaJaguarSeparatorUI();
	}

	@Override
	protected void installDefaults(JSeparator s) {
		super.installDefaults(s);
		LookAndFeel.installBorder(s, "Separator.border");
	}

	@Override
	public void paint(Graphics gr, JComponent c) {
		Graphics2D g = (Graphics2D) gr;
		Dimension s = c.getSize();
		if (c.getParent() instanceof JPopupMenu) {
			g.setPaint(PaintableColor.getPaint(UIManager.getColor("Menu.background"), c));
			g.fillRect(0, 0, c.getWidth(), c.getHeight());
		} else {
			Insets insets = c.getInsets();
			Color highlightColor = UIManager.getColor("Separator.foreground");
			Color shadowColor = UIManager.getColor("Separator.background");
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				g.setColor(highlightColor);
				g.drawLine(insets.left, insets.top, insets.left, s.height - insets.top - insets.bottom);
				g.setColor(shadowColor);
				g.drawLine(insets.left + 1, insets.top, insets.left + 1, s.height - insets.top - insets.bottom);
			} else { // HORIZONTAL
				g.setColor(highlightColor);
				g.drawLine(insets.left, insets.top, s.width - insets.left - insets.right, insets.top);
				g.setColor(shadowColor);
				g.drawLine(insets.left, insets.top + 1, s.width - insets.left - insets.right, insets.top + 1);
			}
		}
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if (c.getParent() instanceof JPopupMenu) {
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				return new Dimension(12, 0);
			} else {
				return new Dimension(0, 12);
			}
		} else {
			Insets insets = c.getInsets();
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				return new Dimension(2 + insets.left + insets.right, insets.top + insets.bottom);
			} else {
				return new Dimension(insets.left + insets.right, 2 + insets.top + insets.bottom);
			}
		}
	}

	public Insets getVisualMargin(JSeparator tc) {
		Insets margin = (Insets) tc.getClientProperty("Quaqua.Component.visualMargin");
		if (margin == null) {
			margin = UIManager.getInsets("Component.visualMargin");
		}
		return (margin == null) ? new Insets(0, 0, 0, 0) : margin;
	}

	@Override
	public int getBaseline(JComponent c, int width, int height) {
		return -1;
	}

	@Override
	public Rectangle getVisualBounds(JComponent c, int type, int width, int height) {
		Rectangle bounds = new Rectangle(0, 0, width, height);
		if (type == VisuallyLayoutable.CLIP_BOUNDS) {
			return bounds;
		}

		JSeparator b = (JSeparator) c;

		InsetsUtil.subtractInto(getVisualMargin(b), bounds);
		return bounds;
	}
}
