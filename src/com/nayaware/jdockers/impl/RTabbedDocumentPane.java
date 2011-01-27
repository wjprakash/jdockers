package com.nayaware.jdockers.impl;

import java.awt.Color;
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
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.nayaware.jdockers.custom.JClosableTabbedPane;

/**
 * A Tabbed Document Pane aka Document Container maintains a collection of
 * Document Windows. The currently selected window can be closed. Each window
 * has a context menu to hide, select or split it it away. The container itself
 * has a context menu to hide all windows. Document Windows cannot float or
 * auto-hide.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RTabbedDocumentPane extends RWindowPane implements ChangeListener,
		PropertyChangeListener {

	/**
	 * <code>true</code> if one of the container's Document Windows is the
	 * active window
	 */
	private boolean active;

	/**
	 * Reference to the last selected window - required to generate correct
	 * hide/show events
	 */
	private RDockableWindow lastSelectedWindow;

	/**
	 * Reference to the TabbedPane used to implemented the container
	 */
	private JClosableTabbedPane tabbedPane;

	/**
	 * Action to hide the currently selected window.
	 */
	private Action hideTabAction = new AbstractAction("Hide") {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow window = getSelectedWindow();
			if (window != null) {
				window.hide();
				window.close();
			}
		}
	};

	/**
	 * Action to hide all windows.
	 */
	private Action hideAllAction = new AbstractAction("Hide All") {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow[] windows = getWindows();
			for (int i = 0; i < windows.length; i++) {
				windows[i].hide();
				windows[i].close();
			}
		}
	};

	/**
	 * Action to activate the window whose index is encoded in the ActionEven's
	 * command string.
	 */
	private Action selectAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			tabbedPane.setSelectedIndex(Integer.parseInt(e.getActionCommand()));
		}
	};

	/**
	 * Action to split the currently selected window from the container, using
	 * the direction encoded in the ActionEvent's command string.
	 */
	private Action splitAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (getWindowCount() > 1) {
				RDockableWindow window = getSelectedWindow();
				if (window != null) {
					removeWindow(window);
					RTabbedDocumentPane pane = new RTabbedDocumentPane(window);
					getParentTiledContainer().dockPaneAt(
							Integer.parseInt(e.getActionCommand()), pane,
							RTabbedDocumentPane.this, 0.5);
				}
			}
		}
	};

	/**
	 * Action to trigger a save event for the currently selected window.
	 */
	private Action saveAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			RDockableWindow window = getSelectedWindow();
			if (window != null) {
				window.getLayoutManager().fireSaveNeeded(window);
			}
		}
	};

	// constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs an empty container. The name of this container should be
	 * mainTab for Sun QA functional test
	 */
	RTabbedDocumentPane() {
		setName("mainTab");
	}

	/**
	 * Constructs a container containing the specified Document Window.
	 */
	RTabbedDocumentPane(RDockableWindow window) {
		this();
		addWindow(window);
	}

	// window methods
	// ----------------------------------------------------------------------------

	/**
	 * Adds a Document Window to this container. The window must not be part of
	 * this or some other container. If it is the currently active window, the
	 * container will be activated. A tabbedpane is automatically constructed if
	 * there isn't one.
	 */
	void addWindow(RDockableWindow window) {
		if (tabbedPane == null) {
			createTabbedPane();
		}
		// there must be a better way but we don't want stateChanged events when
		// a new tab is added so we'll temporarily remove the listener
		tabbedPane.removeChangeListener(this);
		tabbedPane.addTab(window.getTitleForTab(), window.getIcon(),
				window.getWindowComponent(), window.getTitle());
		if (getActiveWindow() == window) {
			tabbedPane.setSelectedComponent(window.getWindowComponent());
			setActive(true);
		}
		fireChangedNoActivate();
		tabbedPane.addChangeListener(this);

		window.addPropertyChangeListener(this);
	}

	private void createTabbedPane() {
		tabbedPane = new JClosableTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setClosable(true);
		tabbedPane.addActionListener(hideTabAction);
		tabbedPane.addChangeListener(this);

		dnd.addSource(tabbedPane);

		tabbedPane.addMouseListener(createMouseListener());
		tabbedPane.setForeground(getInactiveCaptionText());
		add(tabbedPane);
		validate();
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
			 * Tabs and container have (similar but different) context menus
			 */
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
					JPopupMenu menu = new JPopupMenu();

					if (index != -1) {
						JMenuItem item = new JMenuItem();
						item.setText("Save <"
								+ getWindow(index).getTitleForTab() + ">");
						item.addActionListener(saveAction);
						menu.add(item);
						menu.addSeparator();
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
					menu.add(windows);
					if (index != -1 && count > 1) {
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
					menu.addSeparator();
					if (index != -1) {
						menu.add(hideTabAction);
					} else {
						menu.add(hideAllAction);
					}

					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
	}

	/**
	 * Removes a Document Window from the container. If the window isn't part of
	 * this container the method fails silently. The tabbedpane is automatically
	 * removed if the last window of this container was removed.
	 */
	void removeWindow(RDockableWindow window) {
		window.removePropertyChangeListener(this);

		tabbedPane.removeChangeListener(this);
		tabbedPane.remove(window.getWindowComponent());
		fireChangedNoActivate();
		tabbedPane.addChangeListener(this);

		if (getActiveWindow() == window) {
			setActive(false);
		}

		if (tabbedPane.getTabCount() == 0) {
			destroyTabbedPane();
		}
	}

	private void destroyTabbedPane() {
		tabbedPane.removeChangeListener(this);
		remove(tabbedPane);
		tabbedPane = null;
		validate();
		repaint();
	}

	/**
	 * Returns an array of all Document Windows.
	 */
	RDockableWindow[] getWindows() {
		RDockableWindow[] windows = new RDockableWindow[getWindowCount()];
		for (int i = 0; i < windows.length; i++) {
			windows[i] = getWindow(i);
		}
		return windows;
	}

	/**
	 * Returns the number of Document Windows of this container.
	 */
	int getWindowCount() {
		return tabbedPane == null ? 0 : tabbedPane.getTabCount();
	}

	/**
	 * Returns the window at the specified index.
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index &lt; 0 or &gt;= <code>getWindowCount()</code>
	 */
	RDockableWindow getWindow(int index) {
		if (tabbedPane == null) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return modelOf(tabbedPane.getComponentAt(index));
	}

	/**
	 * Returns the selected window or <code>null</code> if there's no window
	 * selected.
	 */
	RDockableWindow getSelectedWindow() {
		Component comp = tabbedPane == null ? null : tabbedPane
				.getSelectedComponent();
		return comp == null ? null : modelOf(comp);
	}

	// event listener
	// ----------------------------------------------------------------------------

	/*
	 * Some property of one of the container's windows changed. Adapt title,
	 * tabName and/or icon.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		RDocumentWindow window = (RDocumentWindow) evt.getSource();
		int index = tabbedPane.indexOfComponent(window.getWindowComponent());
		if ("title".equals(evt.getPropertyName())) {
			if (window.getTabName() == null) {
				tabbedPane.setTitleAt(index, window.getTitle());
			}
			tabbedPane.setToolTipTextAt(index, window.getTitle());
		} else if ("tabName".equals(evt.getPropertyName())) {
			if (window.getTabName() == null) {
				tabbedPane.setTitleAt(index, window.getTitle());
			} else {
				tabbedPane.setTitleAt(index, window.getTabName());
			}
		} else if ("icon".equals(evt.getPropertyName())) {
			tabbedPane.setIconAt(index, window.getIcon());
		}
	}

	public void stateChanged(ChangeEvent e) {
		fireChangedNoActivate();
		activateRequest();
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

	// accessors
	// ---------------------------------------------------------------------------------

	public boolean isActive() {
		return active;
	}

	void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;
			tabbedPane.setForeground(active ? null : getInactiveCaptionText());
		}
	}

	Color getInactiveCaptionText() {
		// return UIManager.getColor("InternalFrame.inactiveTitleForeground");
		return UIManager.getColor(RSwing.isWindows() || RSwing.isMotif()
				|| RSwing.isGTK() ? "TabbedPane.shadow"
				: "TabbedPane.darkShadow");
	}

	// events
	// ------------------------------------------------------------------------------------

	/**
	 * Notify the container that the specified window will become the new active
	 * window. Normally this window should be part of the container but the
	 * method will fail silently if the window is unknown.
	 */
	void activateWindow(RDockableWindow window) {
		int index = tabbedPane.indexOfComponent(window.getWindowComponent());
		if (index != -1) {
			setActive(true);
			tabbedPane.setSelectedIndex(index);
		}
	}

	/**
	 * Notify the container this the specified window was deactivated. Normally
	 * this window should be part of the container but the method will fail
	 * silently if the window is unknown.
	 */
	void deactivateWindow(RDockableWindow window) {
		if (isActive()) {
			int index = tabbedPane
					.indexOfComponent(window.getWindowComponent());
			if (index != -1) {
				setActive(false);
			}
		}
	}

	// actions
	// -----------------------------------------------------------------------------------
	void activateRequest() {
		RDockableWindow window = getSelectedWindow();
		if (window != null) {
			window.activate();
		}
	}

	// -------------------------------------------------------------------------------------------
	/* needed for DnDPanel */
	protected JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	/* needed for DnDPanel */
	protected int getTitleHeight() {
		return getWindowCount() == 0 ? 0 : tabbedPane.getBoundsAt(0).height;
	}

	/* See RPane */
	void dispose() {
		if (tabbedPane != null) {
			for (int i = 0; i < getWindowCount(); i++) {
				getWindow(i).removePropertyChangeListener(this);
			}
		}
	}

	private RDocumentWindow modelOf(Component comp) {
		return (RDocumentWindow) ((JComponent) comp).getClientProperty("model");
	}

	/* See RPane */
	RWindowPane findWindowPane(RDockableWindow window) {
		if (tabbedPane != null) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (tabbedPane.getComponentAt(i) == window.getWindowComponent()) {
					return this;
				}
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------------------------

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		memento.putString("type", "documentContainer");
		for (int i = 0; i < getWindowCount(); i++) {
			memento.createMemento("window").putString("name",
					getWindow(i).getName());
		}
		if (getSelectedWindow() != null) {
			memento.putString("selectedWindow", getSelectedWindow().getName());
		}
	}

	boolean isDocumentSplitting() {
		return true;
	}
}