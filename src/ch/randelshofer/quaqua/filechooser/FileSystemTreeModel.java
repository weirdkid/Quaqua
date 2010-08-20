/*
 * @(#)FileSystemTreeModel.java
 *
 * Copyright (c) 2003-2010 Werner Randelshofer
 * Hausmatt 10, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 */
package ch.randelshofer.quaqua.filechooser;

import ch.randelshofer.quaqua.osx.OSXFile;
import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.util.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

/**
 * The FileSystemTreeModel provides the data model for the file system in a
 * QuaquaFileChooserUI.
 * <p>
 * It is capable of resolving aliases to files, and it updates its content
 * asynchronously to the AWT Event Dispatcher thread.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class FileSystemTreeModel implements TreeModel {

    private final static boolean DEBUG = false;
    public final static File COMPUTER = new File("/");
    /**
     * This is used for keeping track of the validation state of a node.
     */
    public final static int INVALID = 0;
    /**
     * This is used for keeping track of the validation state of a node.
     */
    public final static int VALIDATING = 1;
    /**
     * This is used for keeping track of the validation state of a node.
     */
    public final static int VALID = 2;
    /** We store all our listeners here. */
    protected EventListenerList listenerList = new EventListenerList();
    /**
     * We need a JFileChooser to determine the user presentable (localized) name
     * of a file.
     */
    private JFileChooser fileChooser;
    /**
     * This node holds the root of the file system.
     */
    private FileSystemTreeModel.RootNode root;
    /**
     * This comparator is used to compare the user name of two files.
     * The comparator is able to compare instances of java.io.File and
     * instances of FileSystemTreeModel.Node.
     */
    private Comparator nodeComparator;
    /**
     * When this is true, DirectoryNode's automatically fetch a directory
     * listing from the file system, if they are invalid and one of the following
     * methods is called: DirectoryNode.getChildCount(), DirectoryNode.getChildAt(),
     * DirectoryNode.children(),  DirectoryNode.getIndex().
     */
    private boolean isAutoValidate = true;
    /**
     * If this variable is true, aliases to files are resolved in addition to
     * aliases to directories.
     */
    private boolean isResolveAliasesToFiles = true;
    /**
     * If this variable is true, file labels are resolved.
     */
    private boolean isResolveFileLabels = true;
    /**
     * The collator used for sorting files.
     * Note: We use a static variable here, because creating a collator is
     * very expensive.
     */
    private static Collator collator;
    /**
     * Dispatcher for the validation of file infos.
     */
    private SequentialDispatcher fileInfoDispatcher;
    /**
     * Dispatcher for the validation of directory listings.
     */
    private ConcurrentDispatcher directoryDispatcher;
    /**
     * Dispatcher for the resolution of aliases.
     */
    private SequentialDispatcher aliasResolutionDispatcher;
    /**
     * This is set to true, when we optimize for speed rather than for quality.
     */
    private boolean doItFast;

    /**
     * Creates a new instance.
     *
     * @param fileChooser The JFileChooser is used to determine the user
     * presentable (localized) names of the files.
     */
    public FileSystemTreeModel(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        root = new RootNode();

        fileInfoDispatcher = new SequentialDispatcher();
        //fileInfoDispatcher.setLIFO(true);
        directoryDispatcher = new ConcurrentDispatcher();
        aliasResolutionDispatcher = new SequentialDispatcher();

        doItFast = UIManager.getBoolean("FileChooser.speed");
    }

    public void dispatchDirectoryUpdater(Runnable r) {
        directoryDispatcher.dispatch(r);
    }

    public void dispatchFileUpdater(Runnable r) {
        fileInfoDispatcher.dispatch(r);
    }

    public void dispatchAliasResolution(Runnable r) {
        aliasResolutionDispatcher.dispatch(r);
    }

    /**
     * Removes all children from the root node.
     */
    public void clear() {
        int[] removedIndices = new int[root.getChildCount()];
        Object[] removedChildren = new Object[removedIndices.length];
        for (int i = 0; i < removedIndices.length; i++) {
            removedIndices[i] = i;
            removedChildren[i] = root.getChildAt(0);
            root.remove(0);
        }
        fireTreeNodesRemoved(FileSystemTreeModel.this, new Object[]{root}, removedIndices, removedChildren);
    }

    public void dispose() {
        stopValidation();
        clear();
    }

    public Node getPrototypeValue() {
        return new Node(new File(QuaquaManager.getProperty("user.home")), "Prototype", false);
    }

    public Object getChild(Object parent, int index) {
        return ((FileSystemTreeModel.Node) parent).getChildAt(index);
    }

    public int getChildCount(Object parent) {
        return ((FileSystemTreeModel.Node) parent).getChildCount();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((FileSystemTreeModel.Node) parent).getIndex((FileSystemTreeModel.Node) child);
    }

    private int getIndexOfChildForFile(FileSystemTreeModel.Node parent, File file) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (((FileSystemTreeModel.Node) parent.getChildAt(i)).getResolvedFile().equals(file)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a comparator which is able to compare the user names of two files.
     * The comparator is able to compare instances of java.io.File and
     * instances of FileSystemTreeModel.Node. Both kinds can by mixed freely.
     */
    private Comparator getNodeComparator() {
        if (nodeComparator == null) {
            nodeComparator = UIManager.getBoolean("FileChooser.orderByType")
                    ? (Comparator) new FoldersFirstComparator()
                    : (Comparator) new ByNameComparator();
        }
        return nodeComparator;
    }

    private Collator getCollator() {
        if (collator == null) {
            Locale locale;
            try {
                locale = fileChooser.getLocale();
            } catch (IllegalComponentStateException e) {
                locale = Locale.getDefault();
            }
            collator = new OSXCollator(locale);
            //collator = Collator.getInstance(locale);
        }
        return collator;
    }

    private int getInsertionIndexForNode(FileSystemTreeModel.Node parent, FileSystemTreeModel.Node child) {
        Comparator comparator = getNodeComparator();
        int i;
        for (i = 0; i < parent.getChildCount(); i++) {
            if (comparator.compare(parent.getChildAt(i), child) >= 0) {
                return i;
            }
        }
        return i;
    }

    /**
     * Invoked this to insert newChild at location index in parents children.
     * This will then message nodesWereInserted to create the appropriate
     * event. This is the preferred way to add children as it will create
     * the appropriate event.
     */
    private void insertNodeInto(FileSystemTreeModel.Node newChild,
            FileSystemTreeModel.Node parent, int index) {
        parent.insert(newChild, index);

        int[] newIndices = new int[1];

        newIndices[0] = index;
        fireTreeNodesInserted(this, parent.getPath(), newIndices, new Object[]{newChild});
    }

    public Object getRoot() {
        return root;
    }

    private QuaquaFileSystemView getFileSystemView() {
        FileSystemView fcFileSystemView = fileChooser.getFileSystemView();
        if (fcFileSystemView instanceof QuaquaFileSystemView) {
            return (QuaquaFileSystemView) fcFileSystemView;
        } else {
            return QuaquaFileSystemView.getQuaquaFileSystemView();
        }
    }

    /**
     * Creates a node for the specified file.
     *
     * This is used to create nodes depending on their type (file, directory,
     * alias to file, or alias to directory) and depending on the "resolveAliases"
     * property.
     */
    protected Node createNode(File f) {
        // Determine file type
        File resolvedFile = null;
        int fileType = OSXFile.getFileType(f);
        boolean isDirectory = false;
        boolean isHidden = getFileSystemView().isHiddenFile(f);
        boolean isAlias = fileType == OSXFile.FILE_TYPE_ALIAS;
        if (isAlias) {
            // XXX - Fixme !!!
            resolvedFile = OSXFile.resolveAlias(f, false);
            isDirectory = resolvedFile.isDirectory();
            if (!isResolveAliasesToFiles() && !isDirectory) {
                isAlias = false;
                resolvedFile = f;
            }
        } else {
            resolvedFile = f;
            isDirectory = fileType == OSXFile.FILE_TYPE_DIRECTORY;
        }
        boolean isTraversable;
        if (UIManager.getBoolean("FileChooser.speed")) {
            isTraversable = isDirectory;
        } else {
            isTraversable = isDirectory && fileChooser.isTraversable(resolvedFile);
        }
        // Create node
        Node node;
        if (isAlias) {
            if (isDirectory) {
                node = new AliasDirectoryNode(f, resolvedFile, isHidden);
                node.setTraversable(isTraversable);
            } else {
                node = new AliasNode(f, resolvedFile, isHidden);
            }
        } else {
            if (isDirectory) {
                node = new DirectoryNode(f, isHidden);
                node.setTraversable(isTraversable);
            } else {
                node = new Node(f, isHidden);
            }
        }

        return node;
    }

    public TreePath toPath(File file, TreePath templatePath) {
        // Make sure the file does not contain any relative path components
        // before we work with it.
        file = OSXFile.getAbsoluteFile(file);
        QuaquaFileSystemView fsv = getFileSystemView();

        // Short circuit for the computer folder.
        if (file.equals(fsv.getComputer())) {
            return new TreePath(getRoot());
        }

        // Decompose file into a list of path components.
        // We only add existing path components to the list.
        LinkedList list = new LinkedList();
        File dir = file;
        boolean exists = false;
        do {
            exists |= dir.exists();
            if (exists) {
                list.addFirst(dir);
            }
            if (fsv.isRoot(dir)) {
                if (fsv.getComputer().equals(dir)) {
                    list.set(0, fsv.getSystemVolume());
                }
                break;
            }
            if (exists) {
                dir = fsv.getParentDirectory(dir);
            } else {
                dir = dir.getParentFile();
            }
        } while (dir != null);

        // Determine where we merge the file path with the template path.
        LinkedList components = new LinkedList();
        components.add(getRoot());
        int mergeIndex = 0;
        if (templatePath != null) {
            Outer:
            for (int i = list.size() - 1; i >= 0; i--) {
                File f = (File) list.get(i);
                for (int j = templatePath.getPathCount() - 1; j >= 1; j--) {
                    Node node = (Node) templatePath.getPathComponent(j);
                    if (node.getResolvedFile().equals(f) || node.getFile().equals(f)) {
                        // We have found a merge point, add the template path
                        // up to the merge point to the path components.
                        for (int k = 1; k <= j; k++) {
                            components.add(templatePath.getPathComponent(k));
                        }
                        mergeIndex = i + 1;
                        break Outer;
                    }
                }
            }
        }

        // We have found a merge point, add the file path
        // starting from the merge point to the path components.
        for (int i = mergeIndex; i < list.size(); i++) {
            Node node = (Node) components.getLast();

            // If the file path is not valid, we may encounter a leaf node.
            // We must not try to add a child to it, therefore we break here.
            if (!node.getAllowsChildren() || node.isAlias()) {
                break;
            }

            File childFile = (File) list.get(i);
            int index = getIndexOfChildForFile(node, childFile);
            if (index == -1) {
                Node newChild = createNode(childFile);
                insertNodeInto(newChild, node, getInsertionIndexForNode(node, newChild));
                node = newChild;
            } else {
                node = (Node) node.getChildAt(index);
            }
            components.add(node);
        }
        return new TreePath(components.toArray());
    }

    public TreePath toPath0(File file) {
        // Make sure the file does not contain any relative path components
        // before we work with it.
        file = OSXFile.getAbsoluteFile(file);

        // Decompose file into a list of path components
        LinkedList list = new LinkedList();
        FileSystemView fsv = getFileSystemView();
        File dir = file;
        boolean exists = false;
        do {
            exists = exists || dir.exists();
            if (exists) {
                list.addFirst(dir);
            }
            if (fsv.isRoot(dir)) {
                break;
            }
            if (exists) {
                dir = fsv.getParentDirectory(dir);
            } else {
                dir = dir.getParentFile();
            }
        } while (dir != null);

        LinkedList components = new LinkedList();
        Node node = (Node) getRoot();
        for (int i = 0; i < list.size(); i++) {
            if (node.isLeaf() || node.isAlias()) {
                break;
            }

            components.add(node);

            File childFile = (File) list.get(i);
            int index = getIndexOfChildForFile(node, childFile);
            if (index == -1) {
                Node newChild = createNode(childFile);
                insertNodeInto(newChild, node, getInsertionIndexForNode(node, newChild));
                node = newChild;
            } else {
                node = (Node) node.getChildAt(index);
            }
        }
        components.add(node);
        return new TreePath(components.toArray());
    }

    public boolean isLeaf(Object node) {
        return ((FileSystemTreeModel.Node) node).isLeaf();
    }

    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        // XXX this should be used to rename/move a file.
    }

    /**
     * Sets auto validation of the tree. If the tree is autovalidating, it
     * synchronizes its content with the file system.
     */
    public void setAutoValidate(boolean b) {
        isAutoValidate = b;
    }

    public boolean isAutoValidate() {
        return isAutoValidate;
    }

    public void setResolveAliasesToFiles(boolean newValue) {
        if (isResolveAliasesToFiles != newValue) {
            isResolveAliasesToFiles = newValue;
            invalidateAll();
        }
    }

    public boolean isResolveAliasesToFiles() {
        return isResolveAliasesToFiles;
    }

    public void setResolveFileLabels(boolean newValue) {
        isResolveFileLabels = newValue;
    }

    public boolean isResolveFileLabels() {
        return isResolveFileLabels;
    }

    public void invalidateCache() {
        root.invalidateTree();
    }

    /**
     * Invalidates the provided path.
     * This should be used to invalidateChildren the tree model when there are
     * significant changes in the JFileChooser. Such as showing the JFileChooser
     * and changing the FileFilters of the JFileChooser.
     * To actually get a refresh of the tree, validatePath must be called.
     */
    public void invalidatePath(TreePath path) {
        if (path != null) {
            for (int i = 0; i < path.getPathCount(); i++) {
                Node node = (Node) path.getPathComponent(i);
                node.invalidateChildren();
            }
            // Always invalidate root
            if (path.getPathComponent(0) != root) {
                root.invalidateChildren();
            }
        }
    }

    /**
     * Invalidates all nodes in the tree.
     * This should be used to invalidate the tree model when there are
     * significant changes in the JFileChooser.
     * To actually get a refresh of the tree, validatePath must be called.
     */
    public void invalidateAll() {
        root.invalidateTree();
    }

    /**
     * Stalls validation of the the provided path.
     * This should be used to stop validation of the tree model when it is no
     * longer needed.
     */
    public void stopValidation() {
        root.stopValidationSubtree();
        aliasResolutionDispatcher.stop();
        fileInfoDispatcher.stop();
        directoryDispatcher.stop();
    }

    /**
     * Lazily invalidates the provided path.
     * This should be used to trigger lazy refreshes of the tree model when
     * the user navigates through the tree.
     */
    public void lazyInvalidatePath(TreePath path) {
        if (DEBUG) {
            System.out.println("lazyInvalidatePath auto=" + isAutoValidate + " " + path);
        }
        if (path != null) {
            if (isAutoValidate) {
                root.lazyInvalidateChildren();
                if (path.getPathComponent(0) != root) {
                    ((Node) path.getPathComponent(0)).lazyInvalidateChildren();
                }
                if (path.getPathCount() > 1) {
                    ((Node) path.getPathComponent(path.getPathCount() - 1)).lazyInvalidateChildren();
                    if (path.getPathCount() > 2) {
                        ((Node) path.getPathComponent(path.getPathCount() - 2)).lazyInvalidateChildren();
                    }
                }
            }
        }
    }

    /**
     * Validates (refreshes) the nodes specified by the provided path.
     * Validation is done for nodes only, which have been marked as invalid.
     * The validation is done asynchronously in worker threads.
     */
    public void validatePath(TreePath path) {
        if (DEBUG) {
            System.out.println("AliasFileSystemTreeModel.validatPath " + path);
        }
        for (int i = 0; i < path.getPathCount(); i++) {
            Node node = (Node) path.getPathComponent(i);
            node.validateChildren();
        }
        // Always validate root
        if (path.getPathComponent(0) != root) {
            root.validateChildren();
        }
    }

    //
    //  Events
    //
    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @see     #removeTreeModelListener
     * @param   l       the listener to add
     */
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param node the node being changed
     * @see EventListenerList
     */
    protected void fireTreeNodeChanged(FileSystemTreeModel.Node node) {
        FileSystemTreeModel.Node parent = (FileSystemTreeModel.Node) node.getParent();
        if (parent != null) {
            fireTreeNodesChanged(
                    this,
                    parent.getPath(),
                    new int[]{parent.getIndex(node)},
                    new Object[]{node});
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node being changed
     * @param path the path to the root node
     * @param childIndices the indices of the changed elements
     * @param children the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(TreeModel source, Object[] path,
            int[] childIndices,
            Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path,
                            childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where new elements are being inserted
     * @param path the path to the root node
     * @param childIndices the indices of the new elements
     * @param children the new elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(TreeModel source, Object[] path,
            int[] childIndices,
            Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path,
                            childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where elements are being removed
     * @param path the path to the root node
     * @param childIndices the indices of the removed elements
     * @param children the removed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(TreeModel source, Object[] path,
            int[] childIndices,
            Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path,
                            childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(TreeModel source, Object[] path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /*
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see EventListenerList
     */
    private void fireTreeStructureChanged(TreeModel source, TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /**
     * Returns true, if the file is accepted for selection in the file chooser.
     */
    private boolean accept(File f) {
        return fileChooser.accept(f);
    }

    /**
     * This is the implementation for a file node (a leaf node).
     */
    public class Node implements MutableTreeNode, FileInfo {

        protected TreeNode parent;
        protected File file;
        protected String userName;
        protected CollationKey collationKey;
        /**
         * Holds a Finder label for the file represented by this node.
         * The label is a value in the interval from 0 through 7.
         * The value -1 is used, if the label has not (yet) been retrieved, or
         * if it couldn't be determined due to the lack of native support.
         */
        protected int fileLabel = -1;
        /**
         * Holds the icon of the file.
         * The value null is used, if the icon has not (yet) been retrieved,
         * or if it couldn't be determined due to the lack of native support.
         */
        protected Icon icon;
        /**
         * This is set to true, if infos for the file are not valid.
         */
        private int infoState = INVALID;
        /**
         * Contains Boolean.TRUE or Boolean.FALSE, if the file has been
         * accepted or rejected by the FileFilter of the JFileChooser.
         * Contains null, if the acceptance has not been determined yet.
         */
        protected Boolean isAcceptable;
        /**
         * Contains the hidden state of the file
         */
        protected boolean isHidden;

        public Node(File f, boolean isHidden) {
            //this(f, fileChooser.getName(f));
            this(f, null, isHidden);
        }

        public Node(File f, String userName, boolean isHidden) {
            this.file = f;
            this.userName = userName;
            this.isHidden=isHidden;
        }

        /**
         * Lazily resolves the file. This method returns null, if the
         * File has not been resolved yet.
         */
        public File lazyGetResolvedFile() {
            return file;
        }

        public File getFile() {
            return file;
        }

        public long getFileLength() {
            if (lazyGetResolvedFile() == null) {
                return -1L;
            } else {
                return (getResolvedFile().isDirectory()) ? -1l : file.length();
            }
        }

        public String getUserName() {
            if (userName == null) {
                userName = fileChooser.getName(file);
            }
            return userName;
        }

        public int getFileLabel() {
            validateInfo();
            return fileLabel;
        }

        public Icon getIcon() {
            validateInfo();
            if (icon == null) {
                return (isLeaf())
                        ? UIManager.getIcon("FileView.fileIcon")
                        : UIManager.getIcon("FileView.directoryIcon");
            }
            return icon;
        }

        public CollationKey getCollationKey() {
            if (collationKey == null) {
                collationKey = getCollator().getCollationKey(getUserName());
            }
            return collationKey;
        }

        public boolean isAlias() {
            return false;
        }

        /** Changes the traversability of a directory node.
         * This method has no effect on non-directory nodes.
         */
        public void setTraversable(boolean newValue) {

        }

        /**
         * Returns false, if the node is not accepted by the file filter
         * of the JFileChooser.
         */
        public boolean isAcceptable() {
            if (isAcceptable == null) {
                isAcceptable = (accept(getFile())) ? Boolean.TRUE : Boolean.FALSE;
            }
            return isAcceptable.booleanValue();
        }

        public boolean isHidden() {
            return isHidden;
        }

        public String getFileKind() {
            if (file.isDirectory()) {
                String path = file.getPath();
                if (path.endsWith(".app")) {
                    return "application";
                } else if (path.endsWith(".wdgt")) {
                    return "widget";
                } else {
                    return "folder";
                }
            } else {
                return "document";
            }
        }

        /**
         * Clears cached info
         */
        public void invalidateInfo() {
            infoState = INVALID;
            userName = null;
            collationKey = null;
            isAcceptable = null;
        }

        /**
         * Updates values, that may change in a file.
         */
        public void validateInfo() {
            if (infoState == INVALID) {
                infoState = VALIDATING;
                fileInfoDispatcher.dispatch(new Worker<Boolean>() {

                    public Boolean construct() {
                        if (!doItFast) {
                            Icon oldIcon = icon;
                            int oldFileLabel = fileLabel;
                            // Note: We mustn't invoke this method asynchronously.
                            // Apple's FileView does not like to be used
                            // in a reentrant way.
                            icon = fileChooser.getIcon(file);

                            if (isResolveFileLabels) {
                                fileLabel = OSXFile.getLabel(file);
                            }

                            return (oldIcon != icon || oldFileLabel != fileLabel) ? Boolean.TRUE : Boolean.FALSE;
                        }
                        return Boolean.FALSE;
                    }

                    @Override
                    public void done(Boolean value) {
                        // Fire a TreeNodeChanged only, if validation was
                        // successful, and if we are still part of the tree
                        if (value == Boolean.TRUE
                                && getRoot() == FileSystemTreeModel.this.getRoot()) {
                            fireTreeNodeChanged(Node.this);
                        }
                        infoState = VALID;
                    }
                });
            }
        }

        /**
         * Marks this node as invalid.
         * If the node denotes not a directory, nothing happens.
         */
        public void invalidateChildren() {
            // nothing to do, because Node is not a directory.
        }

        /**
         * Marks this node as invalid if the node is not currently being updated.
         * If the node denotes not a directory, nothing happens.
         */
        public void lazyInvalidateChildren() {
            // nothing to do, because Node is not a directory.
        }

        public void stopValidationSubtree() {
            // nothing to do, because Node is a leaf.
        }

        /**
         * Marks this subtree as invalid.
         */
        public void invalidateTree() {
            invalidateInfo();
            // nothing to do, because Node is a leaf.
        }

        /**
         * Validates this node if it is invalid.
         * If this node is invalid, a worker thread is launched, which reads
         * the directory denoted by this node and merges it with the existing
         * children of this node.
         * If the node denotes not a directory, nothing happens.
         */
        public void validateChildren() {
            // nothing to do, because Node is not a directory.
        }

        public boolean isValidating() {
            // FIXME - should be return isValidatingChildren() || isValidatingInfos();
            return isValidatingChildren();
        }

        public boolean isValidatingChildren() {
            return false;
        }

        public TreeNode getParent() {
            return parent;
        }

        public void setParent(MutableTreeNode newParent) {
            parent = newParent;
        }

        public void removeFromParent() {
            if (parent != null) {
                ((MutableTreeNode)parent).remove(this);
            }
        }

        public void setUserObject(Object object) {
            file = (File) object;
        }

        public TreeNode[] getPath() {
            return getPathToRoot(this, 0);
        }

        protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
            TreeNode[] retNodes;

            // Check for null, in case someone passed in a null node, or
            //   they passed in an element that isn't rooted at root.
            if (aNode == null) {
                if (depth == 0) {
                    return null;
                } else {
                    retNodes = new TreeNode[depth];
                }
            } else {
                depth++;
                retNodes = getPathToRoot(aNode.getParent(), depth);
                retNodes[retNodes.length - depth] = aNode;
            }
            return retNodes;
        }

        /**
         * Returns the root of the tree that contains this node.  The root is
         * the ancestor with a null parent.
         *
         * @see	#isNodeAncestor
         * @return	the root of the tree that contains this node
         */
        public TreeNode getRoot() {
            TreeNode ancestor = this;
            TreeNode previous;

            do {
                previous = ancestor;
                ancestor = ancestor.getParent();
            } while (ancestor != null);

            return previous;
        }

        public boolean isNodeAncestor(TreeNode anotherNode) {
            if (anotherNode == null) {
                return false;
            }

            TreeNode ancestor = this;

            do {
                if (ancestor == anotherNode) {
                    return true;
                }
            } while ((ancestor = ancestor.getParent()) != null);

            return false;
        }

        @Override
        public String toString() {
            return (userName == null) ? file.getName() : userName;
            //return userName+"#"+hashCode();
        }

        public Enumeration children() {
            return DefaultMutableTreeNode.EMPTY_ENUMERATION;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public TreeNode getChildAt(int childIndex) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }

        public int getChildCount() {
            return 0;
        }

        public int getIndex(TreeNode node) {
            return -1;
        }

        public void insert(MutableTreeNode child, int index) {
            throw new IllegalStateException("node does not allow children");
        }

        public boolean isLeaf() {
            return true;
        }

        public void remove(MutableTreeNode node) {
            throw new IllegalArgumentException("argument is not a child");
        }

        public void remove(int index) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }

        public boolean isTraversable() {
            return !isLeaf();
        }

        public File getResolvedFile() {
            return file;
        }
    }

    /**
     * This is the implementation for a directory node (a composite node).
     */
    public class DirectoryNode extends Node {

        /**
         * The time when we are going to refresh our children.
         */
        private volatile long cacheInvalidationTime;
        /**
         * This is used to keep track of child validation.
         */
        /*private */ int childrenState;
        /**
         * The children.
         */
        private ArrayList children;
        /**
         * The current validator.
         */
        private Runnable validator;

        /** Whether the directory is traversable. */
        private boolean isTraversable=true;

        private class DirectoryValidator implements Runnable {

            /**
             * This method is called by a worker thread. It reads the directory
             * represented by this tree node and updates the children
             * of this node accordingly.
             * Since this runs asynchronously with the AWT event dispatcher thread,
             * the updating may become obsolete. i.e, because the user chooses
             * a different file filter. If this is detected, this method does nothing.
             */
            public void run() {
                if (this != validator) {
                    return;
                }

                final long startTime = System.currentTimeMillis();

                // Check if the directory denoted by this node exists.
                final boolean exists = file != null && file.exists();

                // The updating algorithm is split up into two steps.
                // Phase 1 does the I/O intensive part. It is done on the worker
                // thread.
                // Phase 2 updates the contents of the tree and informs the listeners.
                // It is done on the AWT event dispatcher thread.

                // Phase 1: I/O intensive part.

                // Step 1.1: I/O intensive part of reading a directory and merging
                //         the freshly read list of files with the existing
                //         children of the tree.
                //         The result of this step are as follows:
                //         mergedChildren - a Vector containing a new set of
                //                          merged children
                //         newChildren  - an array containing the inserted children.
                //         newChildIndices - an array containing the indices
                //                          of inserted children. The indices refer
                //                          to the vector of old children
                //                          (instance variable "children").
                //         deletedChildren - an array containing the deleted children.
                //         deletedChildIndices - an array containing the indices
                //                          of deleted children. The indices refer
                //                          to the vector of old children
                //                          (instance variable "children").

                // Step 1.1 Fetch fresh files
                final File[] freshFiles;
                if (exists) {
                    freshFiles = getFiles();
                } else {
                    freshFiles = new File[0];
                }
                if (this != validator) {
                    return;
                }


                // Step 1.2 For each fresh file:
                //          - Determine its type
                //          - If it is an alias, resolve it
                //          - Check whether the file is wanted by the file filter
                //          - Create a fresh node for the file
                boolean isDirectoriesOnly = fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
                ArrayList freshNodeList = new ArrayList(freshFiles.length);
                boolean isFileHidingEnabled=fileChooser.isFileHidingEnabled();
                QuaquaFileSystemView fsv = getFileSystemView();
                for (int i = 0; i < freshFiles.length; i++) {
                    File freshFile = freshFiles[i];

                    // Resolve alias and determine if fresh file is traversable
                    // and if it is a directory.
                    boolean freshIsTraversable;
                    int freshFileType = OSXFile.getFileType(freshFile);
                    boolean freshIsDirectory = freshFileType == OSXFile.FILE_TYPE_DIRECTORY;
                    File resolvedFreshFile = null;
                    boolean freshIsAlias;
                    if (isResolveAliasesToFiles()) {
                        freshIsAlias = freshFileType == OSXFile.FILE_TYPE_ALIAS;
                    } else {
                        freshIsAlias = false;
                    }
                    if (freshIsAlias) {
                        resolvedFreshFile = OSXFile.resolveAlias(freshFile, true);
                        if (resolvedFreshFile == null) {
                            freshIsTraversable = false;
                        } else {
                            freshIsTraversable = fileChooser.isTraversable(resolvedFreshFile);
                        }
                    } else {
                        freshIsTraversable = fileChooser.isTraversable(freshFile);
                        resolvedFreshFile = freshFile;
                    }
                    boolean freshIsHidden=fsv.isHiddenFile(freshFile);
                    // Skip the fresh file if it is hidden
                    if ((!isDirectoriesOnly || freshIsDirectory && freshIsTraversable) && (!isFileHidingEnabled || !freshIsHidden)) {

                        // Note: The following code is redundant with method
                        //       createNode().
                        //       Changes applied to this code may also have to
                        //       be done in the other method.
                        if (freshIsAlias) {
                            if (freshIsDirectory ) {
                                Node n=new AliasDirectoryNode(freshFile, resolvedFreshFile, freshIsHidden);
                                n.setTraversable(freshIsTraversable);
                                freshNodeList.add(n);
                            } else {
                                freshNodeList.add(new AliasNode(freshFile, resolvedFreshFile, freshIsHidden));
                            }
                        } else {
                            if (freshIsDirectory) {
                                Node n=new DirectoryNode(freshFile, freshIsHidden);
                                n.setTraversable(freshIsTraversable);
                                freshNodeList.add(n);
                            } else {
                                freshNodeList.add(new Node(freshFile, freshIsHidden));
                            }
                        }
                    }
                }
                final Node[] freshNodes = (Node[]) freshNodeList.toArray(new Node[freshNodeList.size()]);
                if (this != validator) {
                    return;
                }

                // Step 1.3 Sort the fresh nodes
                Arrays.sort(freshNodes, getNodeComparator());
                if (this != validator) {
                    return;
                }


                // Phase 2: Thread sensitive part of the merging.
                //         We update the contents of the tree model and inform our
                //         listeners. This has to be done on the AWT thread.
                //         Since we do some part of the updating in a worker thread,
                //         the data our update is based on may have
                //         been invalidated by the AWT thread. That's why we check
                //         if isUpdatingCache is still true.
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        // Check if we have become obsolete
                        if (DirectoryValidator.this != validator || getRoot() != FileSystemTreeModel.this.getRoot()) {
                            return;
                        }

                        // Step 2.1 Merge the fresh nodes with the old nodes
                        ArrayList mergedChildren = new ArrayList(freshFiles.length);
                        LinkedList newChildren = new LinkedList();
                        int[] newChildIndices = new int[freshFiles.length];
                        LinkedList deletedChildren = new LinkedList();
                        int[] deletedChildIndices = new int[getChildCount()];

                        int freshIndex, oldIndex, mergeIndex, comparison;
                        Node[] oldNodes = (children == null) ? new Node[0] : (Node[]) children.toArray(new Node[children.size()]);

                        int count = freshNodes.length + oldNodes.length;
                        freshIndex = 0;
                        oldIndex = 0;
                        mergeIndex = 0;
                        int lastFreshIndex = -1;
                        File resolvedFreshFile = null;
                        Comparator comparator = getNodeComparator();
                        for (int i = 0; i < count; i++) {
                            if (freshIndex >= freshNodes.length) {
                                comparison = (oldIndex >= oldNodes.length) ? 0 : 1;
                            } else if (oldIndex >= oldNodes.length) {
                                comparison = -1;
                            } else {
                                //comparison = freshNodes[freshIndex].getCollationKey()
                                //.compareTo(oldNodes[oldIndex].getCollationKey());
                                comparison = comparator.compare(freshNodes[freshIndex], oldNodes[oldIndex]);

                                // This little trick is necessary to handle the special case,
                                // when a file gets replaced by a directory of the same name
                                // or vice versa.
                                if (comparison == 0) {
                                    if (freshNodes[freshIndex].getAllowsChildren() != oldNodes[oldIndex].getAllowsChildren()) {
                                        comparison = -1;
                                    }
                                }
                            }

                            if (comparison < 0) {
                                newChildIndices[newChildren.size()] = mergeIndex;
                                Node newNode = freshNodes[freshIndex];
                                newNode.parent = DirectoryNode.this; // Link new child, this saves a loop in STEP 2
                                newChildren.add(newNode);
                                mergedChildren.add(newNode);
                                freshIndex++;
                                mergeIndex++;
                            } else if (comparison == 0) {
                                if (oldIndex < oldNodes.length) {
                                    Node oldNode = oldNodes[oldIndex];
                                    if (!doItFast) {
                                        oldNode.invalidateInfo();
                                    }
                                    mergedChildren.add(oldNode);
                                }
                                oldIndex++;
                                freshIndex++;
                                mergeIndex++;
                            } else {
                                deletedChildIndices[deletedChildren.size()] = mergeIndex + deletedChildren.size() - newChildren.size();
                                deletedChildren.add(oldNodes[oldIndex]);
                                oldIndex++;
                            }
                        }

                        // Step 2.2:
                        // If the directory denoted by this Node does not exist,
                        // we lazily refresh our parent node.
                        if (!exists) {
                            Node parent = FileSystemTreeModel.DirectoryNode.this;
                            while ((parent = (Node) parent.getParent()) != null) {
                                parent.lazyInvalidateChildren();
                                parent.validateChildren();
                            }
                        }

                        if (newChildren.size() > 0 || deletedChildren.size() > 0) {
                            // Unlink deleted children
                            for (Iterator i = deletedChildren.iterator(); i.hasNext();) {
                                Node n = (Node) i.next();
                                n.parent = null;
                                n.invalidateChildren();
                            }

                            // We do not need to link the new children, because we
                            // have done this in step 1 already. This saves the following
                            // iteration.
                            /*
                            for (Iterator i = newChildren.iterator(); i.hasNext(); ) {
                            Node n = (Node) i.next();
                            n.parent = Node.this;
                            }*/

                            // Inform listeners about the changes
                            // and replace the children with the merged children
                            if (newChildren.size() > 0 && deletedChildren.size() == 0) {
                                children = mergedChildren;
                                fireTreeNodesInserted(FileSystemTreeModel.this, getPath(), ArrayUtil.truncate(newChildIndices, 0, newChildren.size()), newChildren.toArray());
                            } else if (newChildren.size() == 0 && deletedChildren.size() > 0) {
                                children = mergedChildren;
                                fireTreeNodesRemoved(FileSystemTreeModel.this, getPath(), ArrayUtil.truncate(deletedChildIndices, 0, deletedChildren.size()), deletedChildren.toArray());
                            } else if (newChildren.size() > 0 && deletedChildren.size() > 0) {
                                // Instead of firing tree structure changed, we
                                // split the insertion and removal into two steps.
                                // This is needed, to update the selection in the
                                // JBrower properly.
                                removeAll(deletedChildren);
                                fireTreeNodesRemoved(FileSystemTreeModel.this, getPath(), ArrayUtil.truncate(deletedChildIndices, 0, deletedChildren.size()), deletedChildren.toArray());
                                children = mergedChildren;
                                fireTreeNodesInserted(FileSystemTreeModel.this, getPath(), ArrayUtil.truncate(newChildIndices, 0, newChildren.size()), newChildren.toArray());
                            }
                        }


                        validator = null;

                        // This is used to let the GUI know, that we have
                        // finished with validating. We need to check for root,
                        // because the node might have been removed from the
                        // tree while we are updating it. 
                        Node nodeRoot = (Node) getRoot();
                        if (nodeRoot == root) {
                            fireTreeNodeChanged(DirectoryNode.this);
                        }

                        // To avoid too many refreshes done by the tree, we
                        // compute a 'best before' time for the directory.
                        // The 'best before' time, is the current time plus
                        // a minimal TTL time plus three times the time we needed to
                        // load the directory.
                        long endTime = System.currentTimeMillis();
                        bestBeforeTimeMillis = endTime + getDirectoryTTL() + (endTime - startTime) * 3;

                        /*
                        // Validate the info of the first 16 nodes
                        for (int i=0, n = Math.min(16, mergedChildren.size()); i < n; i++) {
                        Node node = (Node) mergedChildren.get(i);
                        node.validateInfo();
                        }*/

                        if (DEBUG) {
                            System.out.println("AliasFileSystemTreeModel validated " + (endTime - startTime) + " " + file);
                        }

                        childrenState = VALID;
                    }
                });
            }
        }
        /**
         * The time until the directory is considered to stay valid.
         * The bestBeforeDate is a timestamp in milliseconds, that we can compare
         * with the curent time using System.currentTimeMillis().
         *
         * The value 0 is used to mark nodes which have never been validated before.
         * Directories which have never been visited before are validated with
         * precedence over directories which had been visited already. 
         */
        private long bestBeforeTimeMillis = 0;

        public DirectoryNode(File file, boolean isHidden) {
            super(file, isHidden);
            // No need to check for exists() && isTraversable in the code below,
            // because we are only creating DirectoryNode's for files of which we
            // know that they exist, and that they are traversable
            /*
            if (file != null
            && file.exists()
            && ! fileChooser.isTraversable(file)) {
            cacheInvalidationTime = Long.MAX_VALUE;
            }*/
        }

        @Override
        public long getFileLength() {
            return -1l;
        }

        @Override
        public String getFileKind() {
            return "folder";
        }
        /** Changes the traversability of a directory node.
         * This method has no effect on non-directory nodes.
         */
        @Override
        public void setTraversable(boolean newValue) {
            isTraversable=newValue;
        }

        /**
         * Marks this node as invalid.
         * If the node denotes not a directory, nothing happens.
         */
        @Override
        public void invalidateChildren() {
            if (DEBUG) {
                System.out.println("AliasFileSystemTreeModel.invalidateChildren " + lazyGetResolvedFile());
            }
            childrenState = INVALID;
            validator = null;
        }

        /**
         * Marks the children of this node as invalid.
         * This has only an effect, if this node denotes a directory.
         */
        @Override
        public void lazyInvalidateChildren() {
            if (validator == null && bestBeforeTimeMillis < System.currentTimeMillis()) {
                if (DEBUG) {
                    System.out.println("AliasFileSystemTreeModel.lazyInvalidateChildren YES  validator=" + validator + " good for " + (bestBeforeTimeMillis - System.currentTimeMillis()) + " millis " + lazyGetResolvedFile());
                }
                childrenState = INVALID;
            } else {
                if (DEBUG) {
                    System.out.println("AliasFileSystemTreeModel.lazyInvalidateChildren NO  validator=" + validator + " good for " + (bestBeforeTimeMillis - System.currentTimeMillis()) + " millis " + lazyGetResolvedFile());
                }
            }
        }

        @Override
        public boolean isValidatingChildren() {
            return validator != null;
        }

        @Override
        public void stopValidationSubtree() {
            validator = null;
            for (Enumeration i = super.children(); i.hasMoreElements();) {
                ((Node) i.nextElement()).stopValidationSubtree();
            }
        }

        /**
         * Marks this subtree as invalid.
         */
        @Override
        public void invalidateTree() {
            invalidateInfo();
            if (childrenState == VALID) {
                invalidateChildren();
                for (Enumeration i = children(); i.hasMoreElements();) {
                    ((Node) i.nextElement()).invalidateTree();
                }
            }
        }

        /**
         * Validates this node if it is invalid.
         * If this node is invalid, a worker thread is launched, which reads
         * the directory denoted by this node and merges it with the existing
         * children of this node.
         * If the node denotes not a directory, nothing happens.
         */
        @Override
        public void validateChildren() {
            if (childrenState == INVALID) {
                childrenState = VALIDATING;

                validator = new DirectoryValidator();
                //
                directoryDispatcher.dispatch(validator, bestBeforeTimeMillis == 0);

                // This is used to let the GUI know, that we are validating.
                // We must check for root, because the node might have been
                // removed while we were updating it.
                Node nodeRoot = (Node) getRoot();
                if (nodeRoot == root) {
                    fireTreeNodeChanged(DirectoryNode.this);
                }
                bestBeforeTimeMillis = System.currentTimeMillis() + getDirectoryTTL();
            }
        }

        /**
         * Validates this node if automatic validation is on and if it is invalid.
         */
        public void autoValidateChildren() {
            if (isAutoValidate && childrenState == INVALID) {
                validateChildren();
            }
        }

        @Override
        public Enumeration children() {
            autoValidateChildren();
            if (children == null) {
                return DefaultMutableTreeNode.EMPTY_ENUMERATION;
            } else {
                return new IteratorEnumeration(children.iterator());
            }
        }

        private void removeAll(LinkedList deletedChildren) {
            children.removeAll(deletedChildren);
            for (Iterator i = deletedChildren.iterator(); i.hasNext();) {
                Node n = (Node) i.next();
                n.parent = null;
            }
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            if (children == null) {
                throw new IndexOutOfBoundsException(childIndex + " >= 0");
            }
            autoValidateChildren();
            return (TreeNode) children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            autoValidateChildren();
            return (children == null) ? 0 : children.size();
        }

        @Override
        public int getIndex(TreeNode node) {
            autoValidateChildren();
            return (children == null) ? -1 : children.indexOf(node);
        }

        @Override
        public void insert(MutableTreeNode newChild, int childIndex) {
            invalidateChildren();

            if (newChild == null) {
                throw new IllegalArgumentException("new child is null");
            } else if (isNodeAncestor(newChild)) {
                throw new IllegalArgumentException("new child is an ancestor");
            }

            MutableTreeNode oldParent = (MutableTreeNode) newChild.getParent();

            if (oldParent != null) {
                oldParent.remove(newChild);
            }
            newChild.setParent(this);
            if (children == null) {
                children = new ArrayList();
            }
            children.add(childIndex, newChild);
        }

        @Override
        public boolean isLeaf() {
            return !isTraversable;
        }

        @Override
        public void remove(MutableTreeNode aChild) {
            if (aChild == null) {
                throw new IllegalArgumentException("argument is null");
            }

            if (aChild.getParent() != this) {
                throw new IllegalArgumentException("argument is not a child");
            }
            remove(getIndex(aChild));	// linear search
        }

        @Override
        public void remove(int childIndex) {
            invalidateChildren();

            MutableTreeNode child = (MutableTreeNode) getChildAt(childIndex);
            children.remove(childIndex);
            child.setParent(null);
        }

        protected File[] getFiles() {
            if (DEBUG) {
                System.out.println("AliasFileSystemTreeModel getFiles " + lazyGetResolvedFile());
            }
            File[] files = getFileSystemView().getFiles(
                    lazyGetResolvedFile(),
                    fileChooser.isFileHidingEnabled());
            return files;
        }

        protected long getDirectoryTTL() {
            return 2000;
        }
    }

    private class RootNode extends DirectoryNode {

        public RootNode() {
            super(COMPUTER, false);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public String toString() {
            return "Root#" + hashCode();
        }

        @Override
        protected long getDirectoryTTL() {
            return 1000;
        }

        @Override
        protected File[] getFiles() {
            LinkedList list = new LinkedList();
            File[] files = getFileSystemView().getRoots();
            for (int i = 0; i < files.length; i++) {
                if (DEBUG) {
                    System.out.println("AliasFileSystemTreeModel root:" + files[i]);
                }
                //if (accept(files[i])) {
                list.add(files[i]);
                //}
            }
            return (File[]) list.toArray(new File[list.size()]);
        }

        @Override
        public void validateChildren() {
            if (DEBUG) {
                System.out.println("AliasFileSystemTreeModel.validateChildren of ROOT " + (childrenState == INVALID) + " " + lazyGetResolvedFile());
            }
            super.validateChildren();
        }

        /* Don't hardcode "Computer"
        public String getUserName() {
        return "Computer";
        }*/
        @Override
        public Icon getIcon() {
            validateInfo();
            if (icon == null) {
                return UIManager.getIcon("FileView.computerIcon");
            }
            return icon;
        }
    }

    public class AliasNode extends Node {

        private File resolvedFile;
        private Worker<File> resolver;

        public AliasNode(File aliasFile, File resolvedFile, boolean isHidden) {
            super(aliasFile, isHidden);
            this.resolvedFile = resolvedFile;
        }

        @Override
        public File lazyGetResolvedFile() {
            if (resolvedFile == null && resolver == null) {
                resolver = new Worker<File>() {

                    public File construct() {
                        return OSXFile.resolveAlias(file, false);
                    }

                    @Override
                    public void done(File value) {
                        resolvedFile = value;
                    }

                    @Override
                    public void finished() {
                        resolver = null;

                        // only fire events, if we are still part of the tree
                        if (getRoot() == FileSystemTreeModel.this.getRoot()) {
                            fireTreeNodeChanged(AliasNode.this);
                            fireTreeStructureChanged(FileSystemTreeModel.this, getPath());
                        }
                    }
                };
                dispatchAliasResolution(resolver);
            }
            return resolvedFile;
        }

        @Override
        public File getResolvedFile() {
            if (resolvedFile == null) {
                resolvedFile = OSXFile.resolveAlias(file, false);
            }
            return (resolvedFile == null) ? file : resolvedFile;
        }

        @Override
        public String getFileKind() {
            return "alias";
        }

        @Override
        public boolean isAlias() {
            return true;
        }
    }

    public class AliasDirectoryNode extends DirectoryNode {

        private File resolvedFile;
        /**
         * The current resolver.
         */
        private Worker<File> resolver;

        public AliasDirectoryNode(File aliasFile, File resolvedFile, boolean isHidden) {
            super(aliasFile, isHidden);
            this.resolvedFile = resolvedFile;
        }

        @Override
        public String getFileKind() {
            return "alias";
        }

        @Override
        public boolean isAlias() {
            return true;
        }

        @Override
        public File lazyGetResolvedFile() {
            if (resolvedFile == null && resolver == null) {
                resolver = new Worker<File>() {

                    public File construct() {
                        return OSXFile.resolveAlias(file, false);
                    }

                    @Override
                    public void done(File value) {
                        resolvedFile = (File) value;
                    }

                    @Override
                    public void finished() {
                        resolver = null;
                        // only fire events, if we are still part of the tree
                        if (getRoot() == FileSystemTreeModel.this.getRoot()) {
                            fireTreeNodeChanged(AliasDirectoryNode.this);
                        }
                    }
                };
                dispatchAliasResolution(resolver);
            }
            return resolvedFile;
        }

        @Override
        public File getResolvedFile() {
            if (resolvedFile == null) {
                resolvedFile = OSXFile.resolveAlias(file, false);
            }
            return (resolvedFile == null) ? file : resolvedFile;
        }

        @Override
        public boolean isValidatingChildren() {
            return super.isValidatingChildren() || resolver != null;
        }
    }

    /**
     * This comparator compares two nodes by their name collation key.
     */
    private static class ByNameComparator implements Comparator {

        /**
         * Compares two nodes using their collation keys.
         *
         * @param o1 An instance of FileSystemTreeModel.Node.
         * @param o2 An instance of FileSystemTreeModel.Node.
         */
        public int compare(Object o1, Object o2) {
            return ((Node) o1).getCollationKey().compareTo(((Node) o2).getCollationKey());
        }
    }

    /**
     * This comparator compares two nodes by their type and then by their name
     * collation key.
     */
    private static class FoldersFirstComparator implements Comparator {

        /**
         * Compares two nodes using their collation keys.
         *
         * @param o1 An instance of FileSystemTreeModel.Node.
         * @param o2 An instance of FileSystemTreeModel.Node.
         */
        public int compare(Object o1, Object o2) {
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;
            if (n1.isLeaf() == n2.isLeaf()) {
                return n1.getCollationKey().compareTo(n2.getCollationKey());
            } else {
                return n1.isLeaf() ? 1 : -1;
            }
        }
    }
}
