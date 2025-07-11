/*
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published
 *    by the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sciss.treetable.j;

import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultTreeTableNode extends DefaultMutableTreeNode implements MutableTreeTableNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultTreeTableNode() {
		this("");
	}

	public DefaultTreeTableNode(Object... rowData) {
		if (rowData == null) {
			throw new NullPointerException();
		}
		this.rowData = rowData;
	}

	private Object[] rowData;

	@Override
	public Object getValueAt(int column) {
		return rowData[column];
	}

	@Override
	public void setValueAt(Object value, int column) {
		rowData[column] = value;
	}

	@Override
	public int getColumnCount() {
		return rowData.length;
	}
}
