/*
 * @(#)QuaquaTextFieldFocusHandler.java
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 * QuaquaTextFieldFocusHandler. Selects all text of a JTextComponent, if
 * the user used a keyboard focus traversal key to transfer the focus on the
 * JTextComponent.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaTextFieldFocusHandler implements FocusListener {

    private static QuaquaTextFieldFocusHandler instance;

    public static QuaquaTextFieldFocusHandler getInstance() {
        if (instance == null) {
            instance = new QuaquaTextFieldFocusHandler();
        }
        return instance;
    }

    /**
     * Allow instance creation by UIManager.
     */
    public QuaquaTextFieldFocusHandler() {
    }

    public void focusGained(FocusEvent event) {
        QuaquaUtilities.repaintBorder((JComponent) event.getComponent());

        final JTextComponent tc = (JTextComponent) event.getSource();
        if (tc.isEditable() && tc.isEnabled()) {

            String uiProperty;
            if (tc instanceof JPasswordField) {
                uiProperty = "PasswordField.autoSelect";
            } else if (tc instanceof JFormattedTextField) {
                uiProperty = "FormattedTextField.autoSelect";
            } else {
                uiProperty = "TextField.autoSelect";
            }

            if (tc.getClientProperty("Quaqua.TextComponent.autoSelect") == Boolean.TRUE ||
                    tc.getClientProperty("Quaqua.TextComponent.autoSelect") == null &&
                    UIManager.getBoolean(uiProperty)) {
                if (event instanceof FocusEvent) {
                    FocusEvent cfEvent = (FocusEvent) event;
                    if (cfEvent.getID() == FocusEvent.FOCUS_GAINED) {
                        tc.selectAll();
                    }
                }
            }
        }
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager() instanceof QuaquaKeyboardFocusManager) {
            QuaquaKeyboardFocusManager kfm = (QuaquaKeyboardFocusManager) KeyboardFocusManager.getCurrentKeyboardFocusManager();
            kfm.setLastKeyboardTraversingComponent(null);
        }
    }

    public void focusLost(FocusEvent event) {
        QuaquaUtilities.repaintBorder((JComponent) event.getComponent());
    }
}

