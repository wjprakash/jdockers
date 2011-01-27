package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.nayaware.jdockers.custom.JClosableTabbedPane;

/**
 * Top level Autohide pane container Dockable Pane
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RCombinedDockablePane extends RTitledPane implements
		ChangeListener, PropertyChangeListener {

	/**
	 * Reference to the last selected window - required to generate correct
	 * hide/show events
	 */
	protected RDockableWindow lastSelectedWindow;

	/**
	 * Reference to the first window that isn't shown as part of a TabbedPane
	 */
	protected RDockableWindow firstWindow;

	/**
	 * Reference to the TabbedPane used to display multiple windows
	 */
	protected JClosableTabbedPane tabbedPane;

	/**
	 * Action to hide the currently selected window.
	 */
	Action hideTabAction = new AbstractAction("Hide") {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow window = getSelectedWindow();
			if (window != null) {
				window.getLayoutManager().hideLayoutWindow(window);
			}
		}
	};

	/**
	 * Action to float the currently selected window.
	 */
	Action floatTabAction = new AbstractAction("Floating") {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow window = getSelectedWindow();
			if (window != null) {
				if (window.getBounds() == null) {
					window.setBounds(getAbsoluteBounds());
				}
				window.getLayoutManager().floatDockableWindow(window);
			}
		}
	};

	/**
	 * Action to autohide the currently selected window.
	 */
	Action autoHideTabAction = new AbstractAction("Auto Hide") {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow window = getSelectedWindow();
			if (window != null) {
				getLayoutPane().requestAutoHide(RCombinedDockablePane.this,
						window);
			}
		}
	};

	/**
	 * Action to activate the window whose index is encoded in the ActionEven's
	 * command string.
	 */
	Action selectAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			tabbedPane.setSelectedIndex(Integer.parseInt(e.getActionCommand()));
		}
	};

	/**
	 * Action to split the currently selected window from the container, using
	 * the direction encoded in the ActionEvent's command string.
	 */
	Action splitAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (getWindowCount() > 1) {
				RDockableWindow window = getSelectedWindow();
				if (window != null) {
					removeWindow(window);
					RCombinedDockablePane pane = new RCombinedDockablePane(
							window);
					getParentTiledContainer().dockPaneAt(
							Integer.parseInt(e.getActionCommand()), pane,
							RCombinedDockablePane.this, 0.5);
				}
			}
		}
	};

	// constructors
	// ------------------------------------------------------------------------------

	RCombinedDockablePane() {
		add(createTitleBar(), BorderLayout.NORTH);
	}

	RCombinedDockablePane(RDockableWindow window) {
		this();
		addWindow(window);
	}

	RCombinedDockablePane(RDockableWindow[] windows) {
		this();
		addWindows(windows);
	}

	RCombinedDockablePane(RDockableWindow[] windows,
			RDockableWindow selectedWindow) {
		this();
		addWindows(windows);
		if (tabbedPane != null) {
			for (int i = 0; i < windows.length; i++) {
				if (windows[i] == selectedWindow) {
					tabbedPane.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	protected RFloatingFrame ff;

	RCombinedDockablePane(final RFloatingFrame ff) {
		this.ff = ff;
		add(createTitleBar(false), BorderLayout.NORTH);
	}

	// actions
	// -----------------------------------------------------------------------------------

	/**
	 * Activates the currently selected window. Fails silently if there's no
	 * window.
	 */
	void activateRequest() {
		RDockableWindow window = getSelectedWindow();
		if (window != null) {
			window.activate();
		}
	}

	/**
	 * Hides the active window of this pane. Fails silently if there's no active
	 * window.
	 */
	void hideRequest() {
		RDockableWindow window = getSelectedWindow();
		if (window != null) {
			window.hide();
		}
	}

	/**
	 * Autohides the window(s) of this pane.
	 */
	void pinRequest() {
		getLayoutPane().requestAutoHide(this);
	}

	/**
	 * Floats the window(s) of this pane.
	 */
	void floatRequest() {
		updateWindowBounds();
		RLayoutPane layoutPane = getLayoutPane();
		RDockableWindow[] windows = getWindows();
		removeFromParent();
		layoutPane.floatWindows(windows, null);
	}

	private void updateWindowBounds() {
		for (int i = 0; i < getWindowCount(); i++) {
			RDockableWindow window = getWindow(i);
			if (window.getBounds() == null) {
				window.setBounds(getAbsoluteBounds());
			}
			getLayoutContainer().setupLayoutHint(window);
		}
	}

	// events
	// ------------------------------------------------------------------------------------

	/*
	 * If a window of this pane gets (programmatically) activated, set the title
	 * bar to active and make the active window's component visible, that is set
	 * the selected index of the TabbedPane acordingly.
	 */
	void activateWindow(RDockableWindow window) {
		if (tabbedPane != null) {
			int index = tabbedPane
					.indexOfComponent(window.getWindowComponent());
			if (index != -1) {
				setActive(true);
				tabbedPane.setSelectedIndex(index);
			}
		} else if (firstWindow == window) {
			setActive(true);
		}
	}

	/*
	 * If a window of the pane gets (programmatically) deactived, we set the
	 * title bar to inactive and remove the window's component from this pane.
	 * This might cause the whole pane to go away.
	 */
	void deactivateWindow(RDockableWindow window) {
		if (isActive()) {
			if (tabbedPane != null) {
				if (tabbedPane.indexOfComponent(window.getWindowComponent()) != -1) {
					setActive(false);
				}
			} else if (firstWindow == window) {
				setActive(false);
			}
		}
	}

	// methods
	// -----------------------------------------------------------------------------------

	/**
	 * Adds a new Dockable Window to this container. The first window's
	 * component is directly displayed. From the second window on, a TabbedPane
	 * is used to display all window components. It is an error if the window is
	 * already part of this or some other container.
	 */
	void addWindow(RDockableWindow window) {
		if (tabbedPane != null) {
			tabbedPane.removeChangeListener(this);
			addWindowAsTab(window);
		} else if (firstWindow != null) {
			remove(firstWindow.getWindowComponent());
			createTabbedPane();
			addWindowAsTab(firstWindow);
			addWindowAsTab(window);
			firstWindow = null;
			validate();
		} else {
			firstWindow = window;
			add(window.getWindowComponent(), BorderLayout.CENTER);
			setTitle(window.getTitle());
			validate();
		}
		if (getActiveWindow() == window) {
			if (tabbedPane != null) {
				tabbedPane.setSelectedComponent(window.getWindowComponent());
				setTitle(window.getTitle());
			}
			setActive(true);
		}
		fireChangedNoActivate();
		if (tabbedPane != null) {
			tabbedPane.addChangeListener(this);
		}
		window.addPropertyChangeListener(this);
		markWindow(window);
	}

	void markWindow(RDockableWindow window) {
		window.markAsDocked();
	}

	void addWindows(RDockableWindow[] windows) {
		for (int i = 0; i < windows.length; i++) {
			addWindow(windows[i]);
		}
	}

	private void addWindowAsTab(RDockableWindow window) {
		tabbedPane.addTab(window.getTitleForTab(), window.getIcon(),
				window.getWindowComponent(), window.getTitle());
	}

	private void createTabbedPane() {
		tabbedPane = new JClosableTabbedPane(JTabbedPane.BOTTOM,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addMouseListener(createMouseListener());
		add(tabbedPane, BorderLayout.CENTER);

		dnd.addSource(tabbedPane);
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			/*
			 * Clicking (even right-clicking) the tabs will activate the the
			 * clicked tab, clicking the container itself will activate the
			 * previously selected window.
			 */
			public void mousePressed(MouseEvent e) {
				activateRequest();
			}

			/*
			 * Tabs have context menus
			 */
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int index = DnDSupport.indexAtLocation(tabbedPane,
							e.getX(), e.getY());
					JPopupMenu menu = new JPopupMenu();
					if (index != -1) {
						menu.add(hideTabAction);
						if (ff == null) {
							menu.add(floatTabAction);
							menu.add(autoHideTabAction);
						}
					} else {
						menu.add(hideTabAction);
						menu.add(floatAction);
						if (ff == null) {
							menu.add(autoHideAction);
						}
					}
					int count = getWindowCount();
					JMenu windows = new JMenu("Select Window");
					for (int i = 0; i < count; i++) {
						JMenuItem item = new JMenuItem();
						item.setText(getWindow(i).getTitle());
						item.setActionCommand(String.valueOf(i));
						item.addActionListener(selectAction);
						windows.add(item);
					}
					menu.addSeparator();
					menu.add(windows);

					if (index != -1 && count > 1 && ff == null) {
						JMenu splits = new JMenu("Split");
						JMenuItem item;
						item = new JMenuItem("Top");
						item.setActionCommand("0");
						item.addActionListener(splitAction);
						splits.add(item);
						item = new JMenuItem("Left");
						item.setActionCommand("1");
						item.addActionListener(splitAction);
						splits.add(item);
						item = new JMenuItem("Bottom");
						item.setActionCommand("2");
						item.addActionListener(splitAction);
						splits.add(item);
						item = new JMenuItem("Right");
						item.setActionCommand("3");
						item.addActionListener(splitAction);
						splits.add(item);
						menu.add(splits);
					}

					menu.show(tabbedPane, e.getX(), e.getY());
				}
			}
		};
	}

	/**
	 * Removes a Dockable Window from this container. If the window's component
	 * is part of the TabbedPane, remove it. If the TabbedPane would only show
	 * one window, remove the TabbedPane. Fails silently, if the window is
	 * unknown.
	 */
	void removeWindow(RDockableWindow window) {
		if (tabbedPane != null) {
			int index = tabbedPane
					.indexOfComponent(window.getWindowComponent());
			if (index != -1) {
				window.removePropertyChangeListener(this);
				tabbedPane.removeChangeListener(this);
				tabbedPane.remove(index);
				fireChangedNoActivate();
				tabbedPane.addChangeListener(this);

				if (getActiveWindow() == window) {
					setActive(false);
				}

				if (tabbedPane.getTabCount() == 1) {
					window = modelOf(tabbedPane.getComponentAt(0));
					destroyTabbedPane();
					addWindow(window);
				}
			}
		} else if (firstWindow != null) {
			if (window == firstWindow) {
				window.removePropertyChangeListener(this);
				remove(window.getWindowComponent());
				firstWindow = null;

				fireChangedNoActivate();
				if (getActiveWindow() == window) {
					setActive(false);
				}

				validate();
				repaint();
			}
		}
	}

	private void destroyTabbedPane() {
		tabbedPane.removeChangeListener(this);
		remove(tabbedPane);
		tabbedPane = null;
	}

	/**
	 * Returns an array of all windows of this container.
	 */
	RDockableWindow[] getWindows() {
		if (tabbedPane != null) {
			RDockableWindow[] windows = new RDockableWindow[tabbedPane
					.getTabCount()];
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				windows[i] = modelOf(tabbedPane.getComponentAt(i));
			}
			return windows;
		} else if (firstWindow != null) {
			return new RDockableWindow[] { firstWindow };
		} else {
			return new RDockableWindow[0];
		}
	}

	int getWindowCount() {
		if (tabbedPane != null) {
			return tabbedPane.getTabCount();
		} else if (firstWindow != null) {
			return 1;
		} else {
			return 0;
		}
	}

	RDockableWindow getWindow(int index) {
		if (tabbedPane != null) {
			return modelOf(tabbedPane.getComponentAt(index));
		}
		if (firstWindow == null || index != 0) {
			throw new IndexOutOfBoundsException();
		}
		return firstWindow;
	}

	RDockableWindow getSelectedWindow() {
		if (tabbedPane != null) {
			Component c = tabbedPane.getSelectedComponent();
			return c == null ? null : modelOf(c);
		}
		return firstWindow;
	}

	/**
	 * Rturns the Dockable Window associated with the specified component.
	 */
	RDockableWindow modelOf(Component comp) {
		return (RDockableWindow) ((JComponent) comp).getClientProperty("model");
	}

	/*
	 * Changing the tab changes the pane's title. It also changes the currently
	 * active window of the Layout Pane.
	 */
	public void stateChanged(ChangeEvent e) {
		RDockableWindow window = getSelectedWindow();
		if (window != null) {
			setTitle(window.getTitle());
			fireChangedNoActivate();
			activateRequest();
		}
	}

	void fireChangedNoActivate() {
		RDockableWindow selectedWindow = getSelectedWindow();
		if (lastSelectedWindow != selectedWindow) {
			if (lastSelectedWindow != null) {
				lastSelectedWindow.fireHidden();
			}
			lastSelectedWindow = selectedWindow;
			if (lastSelectedWindow != null) {
				lastSelectedWindow.fireShown();
			}
		}
	}

	/*
	 * A window property changed, update the UI.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		RDockableWindow window = (RDockableWindow) e.getSource();
		if (tabbedPane != null) {
			int index = tabbedPane
					.indexOfComponent(window.getWindowComponent());

			if ("title".equals(e.getPropertyName())) {
				tabbedPane.setToolTipTextAt(index, window.getTitle());
				if (window.getTabName() == null) {
					tabbedPane.setTitleAt(index, window.getTitle());
				}
				if (index == tabbedPane.getSelectedIndex()) {
					setTitle(window.getTitle());
				}
			} else if ("tabName".equals(e.getPropertyName())) {
				if (window.getTabName() == null) {
					tabbedPane.setTitleAt(index, window.getTitle());
				} else {
					tabbedPane.setTitleAt(index, window.getTabName());
				}
			} else if ("icon".equals(e.getPropertyName())) {
				tabbedPane.setIconAt(index, window.getIcon());
			}
		} else if (firstWindow != null) {
			if ("title".equals(e.getPropertyName())) {
				setTitle(window.getTitle());
			}
		}
	}

	void setSelectedIndex(int index) {
		if (tabbedPane != null) {
			tabbedPane.setSelectedIndex(index);
		} else if (index > 0) {
			throw new IndexOutOfBoundsException("Index: " + index
					+ ", tab count: 1");
		}
	}

	void dispose() {
		RDockableWindow[] windows = getWindows();
		for (int i = 0; i < windows.length; i++) {
			removeWindow(windows[i]);
		}
	}

	RWindowPane findWindowPane(RDockableWindow window) {
		if (tabbedPane != null) {
			if (tabbedPane.indexOfComponent(window.getWindowComponent()) != -1) {
				return this;
			}
		} else if (firstWindow == window) {
			return this;
		}
		return null;
	}

	/* See DnDPanel */
	protected JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		memento.putString("type", "combinedDockableContainer");
		for (int i = 0; i < getWindowCount(); i++) {
			memento.createMemento("window").putString("name",
					getWindow(i).getName());
		}
		if (getSelectedWindow() != null) {
			memento.putString("selectedWindow", getSelectedWindow().getName());
		}
	}

	// tests
	// -------------------------------------------------------------------------------------

	boolean isDocumentSplitting() {
		return false;
	}

}
