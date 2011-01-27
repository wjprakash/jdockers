package com.nayaware.jdockers.impl;

/**
 * @author Winston Prakash
 * @version 1.0
 */
import java.util.EventObject;

import javax.swing.JComponent;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DockableWindowEvent;

public class RDockableWindowEvent extends EventObject implements
		DockableWindowEvent {

	public RDockableWindowEvent(RLayoutWindow source) {
		super(source);
	}

	public DockableWindow getDockableWindow() {
		return (DockableWindow) getSource();
	}

	public JComponent getActiveComponent() {
		return ((RLayoutWindow) getSource()).getActiveComponent();
	}

}
