/*
 * @(#)AlphaColorUIResource.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import java.awt.Color;

import javax.swing.plaf.UIResource;

/**
 * A ColorUIResource whith an alpha channel.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AlphaColorUIResource extends Color implements UIResource {
	private static final long serialVersionUID = 1L;

	public AlphaColorUIResource(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public AlphaColorUIResource(int rgba) {
		super(rgba, true);
	}
}
