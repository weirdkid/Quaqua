/*
 * @(#)QuaquaFocusHandler.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;

/**
 * QuaquaFocusHandler.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaFocusHandler implements FocusListener {
	private static QuaquaFocusHandler instance;

	public static QuaquaFocusHandler getInstance() {
		if (instance == null) {
			instance = new QuaquaFocusHandler();
		}
		return instance;
	}

	/**
	 * Prevent instance creation.
	 */
	private QuaquaFocusHandler() {
	}

	@Override
	public void focusGained(FocusEvent event) {
		QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
	}

	@Override
	public void focusLost(FocusEvent event) {
		QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
	}
}
