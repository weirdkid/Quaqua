/*
 * @(#)QuaquaFileView.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.filechooser;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;

import ch.randelshofer.quaqua.osx.OSXFile;

/**
 * A FileView for the Quaqua Look and Feel.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaFileView extends FileView {
	private QuaquaFileSystemView fsv;
	private boolean isPackageTraversable;
	private boolean isApplicationTraversable;

	/**
	 * Creates a new instance.
	 */
	public QuaquaFileView(QuaquaFileSystemView fsv) {
		this.fsv = fsv;
	}

	public boolean isPackageTraversable() {
		return isPackageTraversable;
	}

	public void setPackageTraversable(boolean b) {
		isPackageTraversable = b;
	}

	public boolean isApplicationTraversable() {
		return isApplicationTraversable;
	}

	public void setApplicationTraversable(boolean b) {
		isApplicationTraversable = b;
	}

	/**
	 * The name of the file. Normally this would be simply f.getName()
	 */
	@Override
	public String getName(File f) {
		return fsv.getSystemDisplayName(f);
	}

	/**
	 * A human readable description of the file. For example, a file named jag.jpg
	 * might have a description that read: "A JPEG image file of James Gosling's
	 * face"
	 */
	@Override
	public String getDescription(File f) {
		return "";
	}

	/**
	 * A human readable description of the type of the file. For example, a jpg file
	 * might have a type description of: "A JPEG Compressed Image File"
	 */
	@Override
	public String getTypeDescription(File f) {
		return fsv.getSystemTypeDescription(f);
	}

	/**
	 * The icon that represents this file in the JFileChooser.
	 */
	@Override
	public Icon getIcon(File f) {
		return fsv.getSystemIcon(f);
	}

	/**
	 * Whether the directory is traversable or not. This might be useful, for
	 * example, if you want a directory to represent a compound document and don't
	 * want the user to descend into it.
	 */
	@Override
	public Boolean isTraversable(File f) {
		return OSXFile.isTraversable(f, isPackageTraversable, isApplicationTraversable);
	}
}
