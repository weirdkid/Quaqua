/*
 * @(#)EmptyIcon.java  
 * 
 * Copyright 2010 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

package ch.randelshofer.quaqua.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.Icon;

/**
 * {@code EmptyIcon}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class EmptyIcon implements Icon, Serializable {
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;

	public EmptyIcon(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// empty
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

}
