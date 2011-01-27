package com.nayaware.jdockers.impl;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * An Autohide Item Container contains single or tabbed Autohide Items and knows
 * how to layout them horizontally (left-to-right or right-to-left depending on
 * the component orientation) and vertically (always top-to-down).
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public class RAutoHideItemContainer extends JPanel {

	static final int ITEM_GAP = 8;

	int position;

	/**
	 * Constructs a new Autohide Item Container.
	 */
	RAutoHideItemContainer(int position) {
		super(layoutFor(position));
		this.position = position;
		setOpaque(false);
	}

	/**
	 * Helper because Java doesn't like initialization code because the super()
	 * call.
	 * 
	 * @see RAutoHideItemContainer(int)
	 */
	private static LayoutManager layoutFor(int position) {
		boolean horizontal = position == RLayoutPane.TOP
				|| position == RLayoutPane.BOTTOM;
		return new FillLayout(horizontal ? FillLayout.HORIZONTAL
				: FillLayout.VERTICAL, ITEM_GAP);
	}

	/**
	 * Returns the container's position (TOP/LEFT/BOTTOM/RIGHT), that is the
	 * edge it is located.
	 */
	int getPosition() {
		return position;
	}

	/**
	 * Returns the number of contained Autohide Items.
	 */
	int getItemCount() {
		return getComponentCount();
	}

	/**
	 * Returns the specified Autohide Item.
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                if item doesn't exist.
	 */
	RAutoHideItem getItem(int index) {
		return (RAutoHideItem) getComponent(index);
	}

	/**
	 * Returns a RCombinedAutoHidePane that contains the specified window or
	 * <code>null</code>.
	 */
	RWindowPane findWindowPane(RDockableWindow window) {
		for (int i = 0; i < getItemCount(); i++) {
			RWindowPane pane = getItem(i).autoHidePane.findWindowPane(window);
			if (pane != null) {
				return pane;
			}
		}
		return null;
	}

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		if (getItemCount() > 0) {
			memento = memento.createMemento("autoHideItemContainer");
			memento.putInteger("position", getPosition());
			for (int i = 0; i < getComponentCount(); i++) {
				getItem(i).saveLayout(memento.createMemento("autoHideItem"));
			}
		}
	}
}
