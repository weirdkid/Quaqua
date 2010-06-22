/*
 * @(#)QuaquaButtonListener.java  
 *
 * Copyright (c) 2004-2010 Werner Randelshofer
 * Hausmatt 10, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.beans.*;
import java.util.Enumeration;

/**
 * QuaquaButtonListener.
 *
 * @author  Werner Randelshofer
 * @version $Id$
 */
public class QuaquaButtonListener extends BasicButtonListener {

    transient long lastPressedTimestamp = -1;
    transient boolean shouldDiscardRelease = false;

    /** Creates a new instance. */
    public QuaquaButtonListener(AbstractButton button) {
        super(button);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop.equals("Frame.active")) {
            ((AbstractButton) e.getSource()).repaint();
        }
        super.propertyChange(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            AbstractButton b = (AbstractButton) e.getSource();

            if (b.contains(e.getX(), e.getY())) {
                long multiClickThreshhold = Methods.invokeGetter(b, "getMultiClickThreshhold", (long) 0);
                long lastTime = lastPressedTimestamp;
                long currentTime = lastPressedTimestamp = e.getWhen();
                if (lastTime != -1 && currentTime - lastTime < multiClickThreshhold) {
                    shouldDiscardRelease = true;
                    return;
                }

                ButtonModel model = b.getModel();
                if (!model.isEnabled()) {
                    // Disabled buttons ignore all input...
                    return;
                }
                if (!model.isArmed()) {
                    // button not armed, should be
                    model.setArmed(true);
                }
                model.setPressed(true);
                if (!b.hasFocus()) {
                    if (b.isRequestFocusEnabled()) {
                        b.requestFocus();
                    } else {
                        // request focus if one of the buttons in the button group
                        // has focus
                        if (model instanceof DefaultButtonModel) {
                            ButtonGroup grp = ((DefaultButtonModel) model).getGroup();
                            if (grp != null) {
                                for (Enumeration i = grp.getElements(); i.hasMoreElements();) {
                                    AbstractButton grpButton = (AbstractButton) i.nextElement();
                                    if (grpButton.hasFocus()) {
                                        b.requestFocus();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Support for multiClickThreshhold
            if (shouldDiscardRelease) {
                shouldDiscardRelease = false;


                return;


            }
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            model.setPressed(false);
            model.setArmed(false);


        }
    }

    ;

    @Override
    public void mouseEntered(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();


        if (b.isRolloverEnabled()) {
            model.setRollover(true);


        }
        if (model.isPressed()) {
            model.setArmed(true);
        }


    }

    ;

    @Override
    public void mouseExited(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();


        if (b.isRolloverEnabled()) {
            model.setRollover(false);


        }
        model.setArmed(false);


    }

    ;

    @Override
    public void focusGained(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();


        if (b instanceof JButton && ((JButton) b).isDefaultCapable()) {
            JRootPane root = b.getRootPane();




            if (root != null) {
                QuaquaButtonUI ui = (QuaquaButtonUI) QuaquaUtilities.getUIOfType(
                        ((AbstractButton) b).getUI(), QuaquaButtonUI.class);




                if (ui != null
                        && UIManager.get(ui.getPropertyPrefix() + "defaultButtonFollowsFocus") != Boolean.FALSE) {
                    root.putClientProperty("temporaryDefaultButton", b);
                    root.setDefaultButton((JButton) b);
                    root.putClientProperty("temporaryDefaultButton", null);
                }
            }

        }


        b.repaint();


    }

    @Override
    public void focusLost(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        JRootPane root = b.getRootPane();


        if (root != null) {
            JButton initialDefault = (JButton) root.getClientProperty("initialDefaultButton");




            if (b != initialDefault) {
                QuaquaButtonUI ui = (QuaquaButtonUI) QuaquaUtilities.getUIOfType(
                        ((AbstractButton) b).getUI(), QuaquaButtonUI.class);








                if (ui != null
                        && UIManager.get(ui.getPropertyPrefix() + "defaultButtonFollowsFocus") != Boolean.FALSE) {
                    root.setDefaultButton(initialDefault);
                }
            }


        }

        b.getModel().setArmed(false);

        b.repaint();

    }
}