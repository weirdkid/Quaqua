/*
 * @(#)QuaquaHighlighter.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.View;

/**
 * QuaquaHighlighter.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaHighlighter extends DefaultHighlighter implements UIResource {
	public final static LayeredHighlighter.LayerPainter painterInstance = new QuaquaHighlightPainter(null);

	public static class QuaquaHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
		Color highlightColor;

		public QuaquaHighlightPainter(Color color) {
			super(color);
		}

		@Override
		public Color getColor() {
			return highlightColor == null ? super.getColor() : highlightColor;
		}

		void setColor(JTextComponent c) {
			highlightColor = super.getColor();
			if (highlightColor == null) {
				highlightColor = c.getSelectionColor();
			}
			if (!QuaquaUtilities.isFocused(c)) {
				highlightColor = UIManager.getColor("TextField.inactiveSelectionBackground");
			}
		}

		@Override
		public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
			setColor(c);
			super.paint(g, offs0, offs1, bounds, c);
		}

		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			setColor(c);
			return super.paintLayer(g, offs0, offs1, bounds, c, view);
		}
	}
}
