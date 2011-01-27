package com.nayaware.jdockers;

/**
 * This is a conveinent interface for providing specialized Layout window event.
 * Useful to create document style window event.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface DocumentWindowEvent extends DockableWindowEvent {
	/**
	 * Returns the Document Window that caused this event.
	 * 
	 * @return the object that caused with event (never <code>null</code>).
	 */
	public DocumentWindow getDocumentWindow();
}
