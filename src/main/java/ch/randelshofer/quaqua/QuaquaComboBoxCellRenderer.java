/*
 * @(#)QuaquaComboBoxCellRenderer.java
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

/**
 * QuaquaComboBoxCellRenderer.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaComboBoxCellRenderer implements ListCellRenderer {
	private ListCellRenderer valueRenderer;
	private JPanel panel;

	public QuaquaComboBoxCellRenderer(ListCellRenderer valueRenderer, boolean isInTable, boolean isEditable) {
		this.valueRenderer = valueRenderer;
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if (isInTable) {
			panel.setBorder(null);
		} else {
			if (isEditable) {
				panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
			} else {
				panel.setBorder(BorderFactory.createEmptyBorder(1, 13, 1, 7));
			}
		}
		panel.setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component valueComponent = valueRenderer.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		panel.removeAll();

		panel.add(valueComponent);
		panel.setBackground((isSelected) ? valueComponent.getBackground() : UIManager.getColor("PopupMenu.background"));

		return panel;

	}
}
