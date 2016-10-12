package com.demo;

import javax.swing.tree.DefaultMutableTreeNode;

public class HotfixTreeNode extends DefaultMutableTreeNode {

	/**
	 * Returns true if this node has no children. To distinguish between nodes
	 * that have no children and nodes that <i>cannot</i> have children (e.g. to
	 * distinguish files from empty directories), use this method in conjunction
	 * with <code>getAllowsChildren</code>
	 * 
	 * @see #getAllowsChildren
	 * @return true if this node has no children
	 * 
	 */
	@Override
	public boolean isLeaf() {
		return getUserObject() instanceof Hotfix;
	}
}
