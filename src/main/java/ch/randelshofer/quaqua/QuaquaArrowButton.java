/*
 * @(#)QuaquaArrowButton.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;

/**
 * QuaquaArrowButton is used handle events for the arrow buttons of a
 * QuaquaScrollBarUI. Since the QuaquaScrollBarUI does all the button drawing,
 * the button is completely transparent.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaArrowButton extends JButton implements SwingConstants {
	private static final long serialVersionUID = 1L;
	private JScrollBar scrollbar;

	public QuaquaArrowButton(JScrollBar scrollbar) {
		this.scrollbar = scrollbar;
		setRequestFocusEnabled(false);
		setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {
		return;
	}
	/*
	 * public Dimension getPreferredSize() { if (scrollbar.getOrientation() ==
	 * JScrollBar.VERTICAL) { if (scrollbar.getFont().getSize() <= 11) { return new
	 * Dimension(11, 12); } else { return new Dimension(15, 16); } } else { if
	 * (scrollbar.getFont().getSize() <= 11) { return new Dimension(12, 11); } else
	 * { return new Dimension(16, 15); } } }
	 * 
	 * public Dimension getMinimumSize() { return new Dimension(5, 5); }
	 * 
	 * public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,
	 * Integer.MAX_VALUE); }
	 */

	@Override
	public boolean isFocusable() {
		return false;
	}
}
