package com.nayaware.jdockers.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

/**
 * This class implements the splitting of <code>RWindowPane</code>s and
 * <code>RTiledContainer</code>s.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RSplitPane extends RPane implements PropertyChangeListener {

	/**
	 * Reference to the component implementing this Pane.
	 */
	private JSplitPane splitPane;

	private double initialWeight;

	private int dim;

	private boolean ignoreChange;

	private boolean updateDimensions;

	// constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs a new empty Split Pane which must be populated using
	 * {@link #setTopLeftPane(RPane)} and {@link #setBottomRightPane(RPane)}.
	 */
	RSplitPane(int orientation, boolean ltor, double initialWeight) {
		this.initialWeight = initialWeight;
		splitPane = new JSplitPane(orientation) {
			public void doLayout() {

				// a child is missing, no not update dim divider location
				if (getTopLeftPane() == null || getBottomRightPane() == null) {
					super.doLayout();
					return;
				}

				try {
					ignoreChange = true;
					if (RSplitPane.this.initialWeight >= 0) {
						dim = (int) (getDimSize() * RSplitPane.this.initialWeight);
						if (!isLtor()) {
							dim = -dim;
						}
						RSplitPane.this.initialWeight = -1;
					}
					if (dim < 0) {
						if (-dim > getDimSize()) {
							dim = -(getDimSize() - getDividerSize() - getMin(getTopLeftPane()));
						}
					} else {
						if (dim > getDimSize()) {
							dim = getDimSize() - getDividerSize()
									- getMin(getBottomRightPane());
						}
					}
					int d = dim < 0 ? getDimSize() + dim - getDividerSize()
							: dim;
					splitPane.setDividerLocation(d);
				} finally {
					ignoreChange = false;
				}
				super.doLayout();
				if (updateDimensions) {
					updateDimensions = false;
					RLayoutContainer lc = getLayoutContainer();
					if (lc != null) {
						lc.updateLayoutHintDimensions();
					}
				}
			}
		};
		splitPane.setResizeWeight(ltor ? 0.0 : 1.0);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.addPropertyChangeListener(
				JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		add(splitPane);
	}

	private int getMin(Component c) {
		if (c == null) {
			return 0;
		}
		Dimension size = c.getMinimumSize();
		return isHorizontalSplit() ? size.width : size.height;
	}

	private int getDimSize() {
		return isHorizontalSplit() ? getWidth() : getHeight();
	}

	private int getDividerSize() {
		return splitPane.getDividerSize();
	}

	// accessors
	// ---------------------------------------------------------------------------------

	RPane getTopLeftPane() {
		return (RPane) splitPane.getTopComponent();
	}

	void setTopLeftPane(RPane pane) {
		splitPane.setTopComponent(pane);
		updateLook();
	}

	RPane getBottomRightPane() {
		return (RPane) splitPane.getBottomComponent();
	}

	void setBottomRightPane(RPane pane) {
		splitPane.setBottomComponent(pane);
		updateLook();
	}

	private void updateLook() {
		if (isDocumentSplitting()) {
			splitPane.setDividerSize(2);
		} else {
			splitPane.setDividerSize(4);
		}
		RSplitPane parent = getParentSplitPane();
		if (parent != null) {
			parent.updateLook();
		}
	}

	// inherited methods
	// -------------------------------------------------------------------------

	/* See RPane */
	RWindowPane findWindowPane(RDockableWindow window) {
		RWindowPane pane = getTopLeftPane().findWindowPane(window);
		if (pane == null) {
			pane = getBottomRightPane().findWindowPane(window);
		}
		return pane;
	}

	/* See RPane */
	boolean isDocumentSplitting() {
		return getTopLeftPane() != null && getBottomRightPane() != null
				&& getTopLeftPane().isDocumentSplitting()
				&& getBottomRightPane().isDocumentSplitting();
	}

	// -------------------------------------------------------------------------------------------

	RPane getOther(RPane pane) {
		if (getTopLeftPane() == pane) {
			return getBottomRightPane();
		} else if (getBottomRightPane() == pane) {
			return getTopLeftPane();
		} else {
			throw new Error(); // should never happen
		}
	}

	void replaceChild(RPane oldPane, RPane newPane) {
		if (getTopLeftPane() == oldPane) {
			setTopLeftPane(newPane);
		} else if (getBottomRightPane() == oldPane) {
			setBottomRightPane(newPane);
		} else {
			throw new Error(); // should never happen
		}
		validate(); // fixes revalidate problem if you collapse a windows of a
					// split pane W1|W2|W3
	}

	// private helpers
	// ---------------------------------------------------------------------------

	public void propertyChange(PropertyChangeEvent evt) {
		if (ignoreChange) {
			return;
		}
		// a child is missing, not not update dim
		if (getTopLeftPane() == null || getBottomRightPane() == null) {
			return;
		}

		if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(evt.getPropertyName())) {
			int d = dim;
			if (dim < 0) {
				dim = splitPane.getDividerLocation() + getDividerSize()
						- getDimSize();
			} else {
				dim = splitPane.getDividerLocation();
			}
		}
		updateDimensions = true;
	}

	private double getWeight() {
		int dim = getDimSize() - splitPane.getDividerSize();
		double w = (double) splitPane.getDividerLocation() / dim;
		return isLtor() ? w : 1.0 - w;
	}

	private boolean isLtor() {
		return splitPane.getResizeWeight() == 0.0;
	}

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		memento.putString("type", "splitPane");
		memento.putInteger("orientation", splitPane.getOrientation());
		memento.putDouble("ltor", splitPane.getResizeWeight());
		memento.putDouble("weight", getWeight());
		getTopLeftPane().saveLayout(memento.createMemento("pane"));
		getBottomRightPane().saveLayout(memento.createMemento("pane"));
	}

	void dropRequestDock(RWindowPane source) {
		if (source == getTopLeftPane() || source == getBottomRightPane()) {
			RLayoutContainer lwc = getLayoutContainer();
			RPane rightPane = getOther(source);
			source.removeFromParent();
			lwc.dockPaneAt(getDropZone() - 1, source, rightPane, 0.25);
		} else {
			super.dropRequestDock(source);
		}
	}

	boolean isHorizontalSplit() {
		return splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
	}

	List getPanes() {
		List result = new ArrayList();
		RPane topLeft = getTopLeftPane();
		if (topLeft != null) {
			result.addAll(topLeft.getPanes());
		}
		RPane bottomRight = getBottomRightPane();
		if (bottomRight != null) {
			result.addAll(bottomRight.getPanes());
		}
		return result;
	}

	void updateLayoutHints() {
		getTopLeftPane().updateLayoutHints();
		getBottomRightPane().updateLayoutHints();
	}

	// for unit tests only
	int componentCountForTest() {
		return getTopLeftPane().componentCountForTest()
				+ getBottomRightPane().componentCountForTest();
	}

	String debugName() {
		return (isHorizontalSplit() ? "horizontal" : "vertical") + " split["
				+ getTopLeftPane().debugName() + ", "
				+ getBottomRightPane().debugName() + "]";
	}
}