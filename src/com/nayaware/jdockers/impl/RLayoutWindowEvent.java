package com.nayaware.jdockers.impl;

import java.util.EventObject;

import javax.swing.JComponent;

import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.LayoutWindow;
import com.nayaware.jdockers.LayoutWindowEvent;

/**
 * This class implements a Layout Manager event.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RLayoutWindowEvent extends EventObject implements
		LayoutWindowEvent {

	private LayoutWindow window;

	private JComponent activeComponent;

	private JComponent previousComponent;

	public RLayoutWindowEvent(RLayoutManager source, RLayoutWindow window) {
		this(source, window, null);
	}

	public RLayoutWindowEvent(RLayoutManager source, RLayoutWindow window,
			JComponent previousComponent) {
		super(source);
		this.window = window;
		this.activeComponent = window.getActiveComponent();
		this.previousComponent = previousComponent;
	}

	public LayoutManager getLayoutManager() {
		return (LayoutManager) getSource();
	}

	public LayoutWindow getLayoutWindow() {
		return window;
	}

	public JComponent getActiveComponent() {
		return activeComponent;
	}

	public JComponent getPreviousComponent() {
		return previousComponent;
	}
}
