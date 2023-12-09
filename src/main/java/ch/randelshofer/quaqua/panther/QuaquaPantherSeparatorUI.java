/*
 * @(#)QuaquaPantherSeparatorUI.java
 *
 * Copyright (c) 2001-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.panther;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.util.InsetsUtil;

/**
 * A replacement for the AquaSeparatorUI.
 * <p>
 * This class provides the following workaround for an issue in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Menu separators are drawn using Separator.highlight and Separator.shadow
 * colors instead of a black and white line. This fix affects JSeparator's.</li>
 * </ul>
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id$
 */
public class QuaquaPantherSeparatorUI extends BasicSeparatorUI implements VisuallyLayoutable {

	/** Creates a new instance of QuaquaSeparatorUI */
	public QuaquaPantherSeparatorUI() {
	}

	public static ComponentUI createUI(JComponent c) {
		return new QuaquaPantherSeparatorUI();
	}

	@Override
	protected void installDefaults(JSeparator s) {
		super.installDefaults(s);
		LookAndFeel.installBorder(s, "Separator.border");
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Dimension s = c.getSize();
		if (c.getParent() instanceof JPopupMenu) {
			Color highlightColor = UIManager.getColor("Separator.highlight");
			Color shadowColor = UIManager.getColor("Separator.shadow");
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				g.setColor(highlightColor);
				g.drawLine(s.width / 2 - 1, 1, s.width / 2 - 1, s.height - 2);
				g.setColor(shadowColor);
				g.drawLine(s.width / 2, 1, s.width / 2, s.height - 2);
			} else { // HORIZONTAL
				g.setColor(highlightColor);
				g.drawLine(1, s.height / 2 - 1, s.width - 2, s.height / 2 - 1);
				g.setColor(shadowColor);
				g.drawLine(1, s.height / 2, s.width - 2, s.height / 2);
			}
		} else {
			Insets insets = c.getInsets();
			Color highlightColor = UIManager.getColor("Separator.foreground");
			Color shadowColor = UIManager.getColor("Separator.background");
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				g.setColor(highlightColor);
				g.drawLine(insets.left, insets.top, insets.left, s.height - insets.bottom - 1);
				g.setColor(shadowColor);
				g.drawLine(insets.left + 1, insets.top, insets.left + 1, s.height - insets.bottom - 1);
			} else { // HORIZONTAL
				g.setColor(highlightColor);
				g.drawLine(insets.left, insets.top, s.width - insets.right - 1, insets.top);
				g.setColor(shadowColor);
				g.drawLine(insets.left, insets.top + 1, s.width - insets.right - 1, insets.top + 1);
			}
		}
		Debug.paint(g, c, this);
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

	@Override
	public Dimension getMaximumSize(JComponent c) {
		Dimension d = getPreferredSize(c);
		if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
			d.height = Integer.MAX_VALUE;
		} else {
			d.width = Integer.MAX_VALUE;
		}
		return d;
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
