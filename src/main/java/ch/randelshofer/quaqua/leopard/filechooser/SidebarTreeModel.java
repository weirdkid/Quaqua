/*
 * @(#)SidebarTreeModel.java
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.leopard.filechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ch.randelshofer.quaqua.QuaquaManager;
import ch.randelshofer.quaqua.filechooser.FileSystemTreeModel;
import ch.randelshofer.quaqua.filechooser.SidebarTreeFileNode;
import ch.randelshofer.quaqua.osx.OSXFile;
import ch.randelshofer.quaqua.util.SequentialDispatcher;
import ch.randelshofer.quaqua.util.Worker;

/**
 * SidebarTreeModel.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SidebarTreeModel extends DefaultTreeModel implements TreeModelListener {

    /**
     * Holds the tree volumesPath to the /Volumes folder.
     */
    private TreePath volumesPath;
    /**
     * Holds the FileSystemTreeModel.
     */
    private TreeModel model;
    /**
     * Represents the "Devices" node in the sidebar.
     */
    private DefaultMutableTreeNode devicesNode;
    /**
     * Represents the "Places" node in the sidebar.
     */
    private DefaultMutableTreeNode placesNode;
    /**
     * The JFileChooser.
     */
    private JFileChooser fileChooser;
    /**
     * Sequential dispatcher for the lazy creation of icons.
     */
    private SequentialDispatcher dispatcher = new SequentialDispatcher();
    /**
     * This hash map is used to determine the sequence and visibility of the
     * items in the system list.
     * HashMap&lt;String,SystemItemInfo&gt;
     */
    private HashMap systemItemsMap = new HashMap();
    /**
     * The defaultUserItems are used when we fail to read the user items from
     * the sidebarFile.
     */
    private final static File[] defaultUserItems = new File[]{
            new File(QuaquaManager.getProperty("user.home")),
            new File(QuaquaManager.getProperty("user.home"), "Desktop"),
            new File(QuaquaManager.getProperty("user.home"), "Documents")
        };


    /** Creates a new instance. */
    public SidebarTreeModel(JFileChooser fileChooser, TreePath path, TreeModel model) {
        super(new DefaultMutableTreeNode(), true);

        this.fileChooser = fileChooser;
        this.volumesPath = path;
        this.model = model;

        devicesNode = new DefaultMutableTreeNode(UIManager.getString("FileChooser.devices"));
        devicesNode.setAllowsChildren(true);
        placesNode = new DefaultMutableTreeNode(UIManager.getString("FileChooser.places"));
        placesNode.setAllowsChildren(true);

        DefaultMutableTreeNode r = (DefaultMutableTreeNode) getRoot();
        r.add(devicesNode);
        r.add(placesNode);

        //validate();
        updateDevicesNode();
        updatePlacesNode();
        
        model.addTreeModelListener(this);
    }

    
    private void updatePlacesNode() {
    	
    	ArrayList<FileNode> freshUserItems;

        freshUserItems = new ArrayList<FileNode>();
        for (int i = 0; i < defaultUserItems.length; i++) {
            if (defaultUserItems[i].exists()) {
                freshUserItems.add(new FileNode(defaultUserItems[i]));
            }
        }
        
        
        int oldUserItemsSize = placesNode.getChildCount();
        if (oldUserItemsSize > 0) {
            int[] removedIndices = new int[oldUserItemsSize];
            Object[] removedChildren = new Object[oldUserItemsSize];
            for (int i = 0; i < oldUserItemsSize; i++) {
                removedIndices[i] = i;
                removedChildren[i] = placesNode.getChildAt(i);
            }
            placesNode.removeAllChildren();
            fireTreeNodesRemoved(
                    SidebarTreeModel.this,
                    placesNode.getPath(),
                    removedIndices,
                    removedChildren);
        }
        
        if (freshUserItems.size() > 0) {
            int[] insertedIndices = new int[freshUserItems.size()];
            Object[] insertedChildren = new Object[freshUserItems.size()];
            for (int i = 0; i < freshUserItems.size(); i++) {
                insertedIndices[i] = i;
                insertedChildren[i] = freshUserItems.get(i);
                if (freshUserItems.get(i) == null) {
                    placesNode.add(new DefaultMutableTreeNode("null?"));
                } else {
                    placesNode.add((DefaultMutableTreeNode) freshUserItems.get(i));
                }
            }
            fireTreeNodesInserted(
                    SidebarTreeModel.this,
                    placesNode.getPath(),
                    insertedIndices,
                    insertedChildren);
        }
    }
    

    
    private void updateDevicesNode() {
        FileSystemTreeModel.Node modelDevicesNode = (FileSystemTreeModel.Node) volumesPath.getLastPathComponent();

        // Remove nodes from the view which are not present in the model
        for (int i = devicesNode.getChildCount() - 1; i >= 0; i--) {
            SidebarViewToModelNode viewNode = (SidebarViewToModelNode) devicesNode.getChildAt(i);
            if (viewNode.getTarget().getParent() != modelDevicesNode) {
                removeNodeFromParent(viewNode);
            }
        }

        // Add nodes to the view, wich are present in the model, but not
        // in the view. Only add non-leaf nodes
        for (int i = 0, n = modelDevicesNode.getChildCount(); i < n; i++) {
            FileSystemTreeModel.Node modelNode = (FileSystemTreeModel.Node) modelDevicesNode.getChildAt(i);
            if (!modelNode.isLeaf()) {
                boolean isInView = false;
                for (int j = 0, m = devicesNode.getChildCount(); j < m; j++) {
                    SidebarViewToModelNode viewNode = (SidebarViewToModelNode) devicesNode.getChildAt(j);
                    if (viewNode.getTarget() == modelNode) {
                        isInView = true;
                        break;
                    }
                }
                if (!isInView) {
                    SidebarViewToModelNode newNode = new SidebarViewToModelNode(modelNode);
                    int insertionIndex = 0;
                   SideBarViewToModelNodeComparator comparator=new SideBarViewToModelNodeComparator();
                    while (insertionIndex < devicesNode.getChildCount()
                            && comparator.compare((SidebarViewToModelNode) devicesNode.getChildAt(insertionIndex),newNode) < 0) {
                        insertionIndex++;
                    }
                    insertNodeInto(newNode, devicesNode, insertionIndex);
                }
            }
        }

        // Update the view
        if (devicesNode.getChildCount() > 0) {
            int[] childIndices = new int[devicesNode.getChildCount()];
            Object[] childNodes = new Object[devicesNode.getChildCount()];
            for (int i = 0; i < childIndices.length; i++) {
                childIndices[i] = i;
                childNodes[i] = devicesNode.getChildAt(i);
            }
            fireTreeNodesChanged(this, devicesNode.getPath(), childIndices, childNodes);
        }
    }


    public void treeNodesChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeNodesInserted(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeStructureChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    private class FileNode extends Node {

        private File file;
        private Icon icon;
        private String userName;
        private boolean isTraversable;
        /**
         * Holds a Finder label for the file represented by this node.
         * The label is a value in the interval from 0 through 7.
         * The value -1 is used, if the label could not be determined.
         */
        protected int fileLabel = -1;

        public FileNode(File file) {
            this.file = file;
            // userName = fileChooser.getName(file);
            isTraversable = true;
        }

        public File getResolvedFile() {
            return file;
        }

        public File getFile() {
            return file;
        }

        public Icon getIcon() {
            if (icon == null) {
                icon = (isTraversable())
                        ? UIManager.getIcon("FileView.directoryIcon")
                        : UIManager.getIcon("FileView.fileIcon");
                //
                if (!UIManager.getBoolean("FileChooser.speed")) {
                    dispatcher.dispatch(new Worker<Icon>() {

                        public Icon construct() {
                            return fileChooser.getIcon(file);
                        }

                        @Override
                        public void done(Icon value) {
                            icon = value;
                            int[] changedIndices = {getParent().getIndex(FileNode.this)};
                            Object[] changedChildren = {FileNode.this};
                            SidebarTreeModel.this.fireTreeNodesChanged(
                                    SidebarTreeModel.this,
                                    placesNode.getPath(),
                                    changedIndices, changedChildren);
                        }
                    });
                }
            }
            return icon;
        }

        public String getUserName() {
            if (userName == null) {
                userName = fileChooser.getName(file);
            }
            return userName;
        }

        public boolean isTraversable() {
            return isTraversable;
        }
    }

    /**
     * An AliasNode is resolved as late as possible.
     */
    private abstract class Node extends DefaultMutableTreeNode implements SidebarTreeFileNode {

        @Override
        public boolean getAllowsChildren() {
            return false;
        }
    }

    /**
     * An AliasNode is resolved as late as possible.
     */
    private class AliasNode extends Node {

        private byte[] serializedAlias;
        private File file;
        private Icon icon;
        private String userName;
        private String aliasName;
        private boolean isTraversable;
        /**
         * Holds a Finder label for the file represented by this node.
         * The label is a value in the interval from 0 through 7.
         * The value -1 is used, if the label could not be determined.
         */
        protected int fileLabel = -1;

        public AliasNode(byte[] serializedAlias, String aliasName) {
            this.file = null;
            this.aliasName = aliasName;
            this.serializedAlias = serializedAlias;
            isTraversable = true;
        }

        public File getResolvedFile() {
            if (file == null) {
                icon = null; // clear cached icon!
                file = OSXFile.resolveAlias(serializedAlias, false);
            }
            return file;
        }

        public Icon getIcon() {
            if (icon == null) {
                // Note: We clear this icon, when we resolve the alias
                icon = (isTraversable())
                        ? UIManager.getIcon("FileView.directoryIcon")
                        : UIManager.getIcon("FileView.fileIcon");
                //
                if (file != null && !UIManager.getBoolean("FileChooser.speed")) {
                    dispatcher.dispatch(new Worker<Icon>() {

                        public Icon construct() {
                            return fileChooser.getIcon(file);
                        }

                        @Override
                        public void done(Icon value) {
                            icon = value;

                            int[] changedIndices = new int[]{getParent().getIndex(AliasNode.this)};
                            Object[] changedChildren = new Object[]{AliasNode.this};
                            SidebarTreeModel.this.fireTreeNodesChanged(
                                    SidebarTreeModel.this,
                                    ((DefaultMutableTreeNode) AliasNode.this.getParent()).getPath(),
                                    changedIndices, changedChildren);
                        }
                    });
                }
            }
            return icon;
        }

        public String getUserName() {
            if (userName == null) {
                if (file != null) {
                    userName = fileChooser.getName(file);
                }
            }
            return (userName == null) ? aliasName : userName;
        }

        public boolean isTraversable() {
            return isTraversable;
        }
    }

    private static class SystemItemInfo {

        String name = "";
        int sequenceNumber = 0;
        boolean isVisible = true;
    }

    /** Note: SidebaViewToModelNode must not implement Comparable and must
     * not override equals()/hashCode(), because this confuses the layout algorithm
     * in JTree.
     */
    private class SidebarViewToModelNode extends Node /*implements Comparable*/ {

        private FileSystemTreeModel.Node target;

        public SidebarViewToModelNode(FileSystemTreeModel.Node target) {
            this.target = target;
        }

        public File getResolvedFile() {
            return target.getResolvedFile();
        }

        public String getUserName() {
            return target.getUserName();
        }

        public Icon getIcon() {
            return target.getIcon();
        }

        public FileSystemTreeModel.Node getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return target.toString();
        }
/*
        public int compareTo(Object o) {
            return compareTo((SidebarViewToModelNode) o);
        }

        public int compareTo(SidebarViewToModelNode that) {
            FileSystemTreeModel.Node o1 = this.getTarget();
            FileSystemTreeModel.Node o2 = that.getTarget();

            SystemItemInfo i1 = (SystemItemInfo) systemItemsMap.get(o1.getUserName());
            if (i1 == null && o1.getResolvedFile().getName().equals("")) {
                i1 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            SystemItemInfo i2 = (SystemItemInfo) systemItemsMap.get(o2.getUserName());
            if (i2 == null && o2.getResolvedFile().getName().equals("")) {
                i2 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            if (i1 != null && i2 != null) {
                return i1.sequenceNumber - i2.sequenceNumber;
            }

            if (i1 != null) {
                return -1;
            }
            if (i2 != null) {
                return 1;
            }

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof SidebarViewToModelNode) //
                    ? compareTo((SidebarViewToModelNode) o) == 0 //
                    : false;
        }

        @Override
        public int hashCode() {
            return getTarget() == null ? 0 : getTarget().getUserName().hashCode();
        }*/
    }
     private class SideBarViewToModelNodeComparator implements Comparator<SidebarViewToModelNode> {

        public int compare(SidebarViewToModelNode n1, SidebarViewToModelNode n2) {
            FileSystemTreeModel.Node o1 = n1.getTarget();
            FileSystemTreeModel.Node o2 = n2.getTarget();

            SystemItemInfo i1 = (SystemItemInfo) systemItemsMap.get(o1.getUserName());
            if (i1 == null && o1.getResolvedFile().getName().equals("")) {
                i1 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            SystemItemInfo i2 = (SystemItemInfo) systemItemsMap.get(o2.getUserName());
            if (i2 == null && o2.getResolvedFile().getName().equals("")) {
                i2 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            if (i1 != null && i2 != null) {
                return i1.sequenceNumber - i2.sequenceNumber;
            }

            if (i1 != null) {
                return -1;
            }
            if (i2 != null) {
                return 1;
            }

            return 0;
        }

    }
}
