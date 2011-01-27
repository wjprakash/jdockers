
package com.nayaware.jdockers.impl;

import com.nayaware.jdockers.DockableWindowListener;
import com.nayaware.jdockers.DocumentWindow;
import com.nayaware.jdockers.DocumentWindowListener;

/**
 * This class implements the requirements for Document Windows and otherwise
 * plays the role of a model in my Pane-DnD-Window (kind of MVC) triad.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public class RDocumentWindow extends RDockableWindow implements DocumentWindow {

	// constructors
	// ------------------------------------------------------------------------------

	public RDocumentWindow(RLayoutManager manager, String name) {
		super(manager, name);
	}

	// listeners
	// ---------------------------------------------------------------------------------

	public void addDocumentWindowListener(DocumentWindowListener listener) {
		super.addDockableWindowListener(listener);
	}

	public void removeDocumentWindowListener(DocumentWindowListener listener) {
		super.removeDockableWindowListener(listener);
	}

	public void addDockableWindowListener(DockableWindowListener listener) {
		throw new UnsupportedOperationException();
	}

	public void removeDockableWindowListener(DockableWindowListener listener) {
		throw new UnsupportedOperationException();
	}

}
