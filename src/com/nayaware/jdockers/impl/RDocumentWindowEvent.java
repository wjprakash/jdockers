package com.nayaware.jdockers.impl;

import com.nayaware.jdockers.DocumentWindow;
import com.nayaware.jdockers.DocumentWindowEvent;

/**
 * @author Winston Prakash
 * @version 1.0
 */
public class RDocumentWindowEvent extends RDockableWindowEvent implements
		DocumentWindowEvent {

	public RDocumentWindowEvent(RDocumentWindow source) {
		super(source);
	}

	public DocumentWindow getDocumentWindow() {
		return (DocumentWindow) getSource();
	}
}
