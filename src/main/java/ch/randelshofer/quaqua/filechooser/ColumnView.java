/*
 * Copyright (c) 2014 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.filechooser;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

/**
 * The file chooser column view.
 */

public abstract class ColumnView extends JPanel implements FileChooserView {

	private static final long serialVersionUID = 1L;
	private ChangeListener changeListener;
	private ChangeEvent changeEvent = new ChangeEvent(this);
	private SelectListener selectListener;

	@Override
	public final void addSelectionChangeListener(ChangeListener l) {
		changeListener = l;
	}

	@Override
	public final void addSelectListener(SelectListener l) {
		selectListener = l;
	}

	protected final void selectionChanged() {
		if (changeListener != null) {
			changeListener.stateChanged(changeEvent);
		}
	}

	protected final void select(TreePath path) {
		if (selectListener != null) {
			selectListener.select(path);
		}
	}

	@Override
	public void ensureSelectionIsVisible() {
		List<TreePath> paths = getSelection();
		if (!paths.isEmpty()) {
			ensurePathIsVisible(paths.get(0));
		}
	}
}
