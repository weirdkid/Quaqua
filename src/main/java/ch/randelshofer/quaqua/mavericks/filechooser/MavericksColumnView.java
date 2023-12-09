/*
 * Copyright (c) 2014 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement. For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.mavericks.filechooser;

import javax.swing.JFileChooser;

import ch.randelshofer.quaqua.lion.filechooser.LionColumnView;

/**
 * The file chooser column view for Mavericks.
 */

public class MavericksColumnView extends LionColumnView {

	private static final long serialVersionUID = 1L;

	public MavericksColumnView(JFileChooser fc) {
		super(fc);

		browser.setFixedCellWidth(207);
	}
}
