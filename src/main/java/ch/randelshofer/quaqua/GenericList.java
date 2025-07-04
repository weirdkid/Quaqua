/*
 * Copyright (c) 2014 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * A generic interface for a component that has list-like behavior (e.g. JList,
 * JTable, JTree).
 */

public interface GenericList {

	JComponent getComponent();

	boolean isEnabled();

	void requestFocus();

	int getRowCount();

	Object getRow(int index);

	boolean isMultipleSelection();

	boolean isRowSelected(int index);

	boolean isSelectionEmpty();

	void clearSelection();

	void setSelectionInterval(int index1, int index2);

	void addSelectionInterval(int index1, int index2);

	void removeSelectionInterval(int index1, int index2);

	int getAnchorSelectionIndex();

	void setAnchorSelectionIndex(int index);

	int getMinSelectionIndex();

	int getMaxSelectionIndex();

	boolean isValueAdjusting();

	void setValueIsAdjusting(boolean b);

	int identifyRowAtLocation(Point loc);

	void scrollToViewRows(int index1, int index2);

	boolean isDragEnabled();

	TransferHandler getTransferHandler();
}
