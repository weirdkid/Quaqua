/*
 * @(#)ViewportPainter.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

import java.awt.Graphics;

import javax.swing.JViewport;

/**
 * This interface is implemented by user interface delegates that wish to paint
 * onto the content area of a JViewport.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface ViewportPainter {
	/**
	 * Paints the viewport of a JViewport, that contains the component of the user
	 * interface delegate. This method is invoked by QuaquaViewportUI.
	 */
	public void paintViewport(Graphics g, JViewport c);
}
