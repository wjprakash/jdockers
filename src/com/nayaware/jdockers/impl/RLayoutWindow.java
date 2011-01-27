package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.nayaware.jdockers.LayoutWindow;
import com.nayaware.jdockers.LayoutWindowListener;
import com.nayaware.jdockers.custom.JClosableTabbedPane;

/**
 * This class implements the requirements for Layout Windows and otherwise plays
 * the role of a model in my Pane-DnD-Window (kind of MVC) triad.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public abstract class RLayoutWindow implements LayoutWindow, ChangeListener {

	/**
	 * A ComponentData instance stores the component along with its tabName and
	 * icon.
	 */
	private static class ComponentData {
		final JComponent component;
		final String tabName;
		final ImageIcon icon;

		ComponentData(JComponent component, String tabName, ImageIcon icon) {
			this.component = component;
			this.tabName = tabName;
			this.icon = icon;
		}
	}

	private String name;

	private String title;

	private ImageIcon icon;

	private Rectangle bounds;

	private String tabName;

	private boolean closable = true;

	private ComponentData[] components;

	private int count;

	private int tabIndex;

	private RLayoutManager manager;

	private boolean visible;

	private String type;

	private JPanel windowComponent;

	private JTabbedPane tabbedPane;

	// constructors
	// ------------------------------------------------------------------------------

	public RLayoutWindow(RLayoutManager manager, String name) {
		if (manager == null || name == null) {
			throw new NullPointerException();
		}
		this.manager = manager;
		this.name = name;
	}

	// accessors
	// ---------------------------------------------------------------------------------

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (!equals(this.name, name)) {
			manager.changeName(this, name);
			this.name = name;
		}
	}

	public String getName() {
		return name;
	}

	public void setTitle(String title) {
		String oldTitle = this.title;
		if (!equals(oldTitle, title)) {
			this.title = title;
			firePropertyChange("title", oldTitle, title);
			if (tabName == null) {
				firePropertyChange("titleForTab", oldTitle, title);
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public void setIcon(ImageIcon icon) {
		ImageIcon oldIcon = this.icon;
		if (!equals(oldIcon, icon)) {
			this.icon = icon;
			firePropertyChange("icon", oldIcon, icon);
		}
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void setBounds(Rectangle bounds) {
		Rectangle oldBounds = this.bounds;
		if (!equals(oldBounds, bounds)) {
			this.bounds = bounds;
			firePropertyChange("bounds", oldBounds, bounds);
		}
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setTabName(String tabName) {
		String oldTabName = this.tabName;
		if (!equals(oldTabName, tabName)) {
			this.tabName = tabName;
			firePropertyChange("tabName", oldTabName, tabName);
			firePropertyChange("titleForTab", oldTabName, tabName);
		}
	}

	public String getTabName() {
		return tabName;
	}

	public void setClosable(boolean closable) {
		this.closable = closable;
	}

	public boolean isClosable() {
		return closable;
	}

	// property change support
	// -------------------------------------------------------------------

	protected Vector propertyChangeListeners = new Vector(1);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		Vector l = (Vector) propertyChangeListeners.clone();
		int size = l.size();
		if (size > 0) {
			PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName,
					oldValue, newValue);
			for (int i = 0; i < size; i++) {
				((PropertyChangeListener) l.get(i)).propertyChange(e);
			}
		}
	}

	protected static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	// components
	// --------------------------------------------------------------------------------

	/**
	 * Adds a JComponent as the content of this window. If more than one
	 * component is added, a small tab strip is displayed at the bottom of the
	 * window. You can query the currently selected component with
	 * {@link #getActiveComponent()}. You can set a new active component with
	 * {@link #setActiveComponent}. Each component should have a unique tabName.
	 */
	public void addComponent(JComponent comp, String tabName) {
		addComponent(comp, tabName, null);
	}

	/**
	 * Adds a JComponent as the content to this window. If more than one
	 * component is added, a small tab strip is displayed at the bottom of the
	 * window. You can query the currently selected component with
	 * {@link #getActiveComponent()}. You can set a new active component with
	 * {@link #setActiveComponent}. Each component should have a unique tabName.
	 */
	public void addComponent(JComponent comp, String tabName, ImageIcon icon) {
		if (comp == null || tabName == null) {
			throw new NullPointerException();
		}
		if (components == null) {
			components = new ComponentData[1];
		} else if (count == components.length) {
			ComponentData[] ncomponents = new ComponentData[count + 3];
			System.arraycopy(components, 0, ncomponents, 0, count);
			components = ncomponents;
		}
		components[count++] = new ComponentData(comp, tabName, icon);
		if (tabIndex == -1) {
			tabIndex = 0;
		}
		updateWindowComponent();
		fireComponentAdded(comp);
		if (count == 1) {
			fireComponentChanged(null);
		}
	}

	/**
	 * Removes a JComponent from this window. The first component added with the
	 * name "tabName" is removed (but names must be unique anyhow). The method
	 * fails silently if the name is unknown or null.
	 */
	public void removeComponent(String tabName) {
		for (int i = 0; i < count; i++) {
			if (components[i].tabName.equals(tabName)) {
				JComponent previousComponent = components[i].component;
				System.arraycopy(components, i + 1, components, i, --count - i);
				components[count] = null;
				boolean removeCurrent = tabIndex == i;
				if (tabIndex == count) {
					--tabIndex;
				}
				updateWindowComponent();
				fireComponentRemoved(previousComponent);
				if (removeCurrent) {
					fireComponentChanged(previousComponent);
				}
				return;
			}
		}
	}

	/**
	 * Removes all added components from this window.
	 */
	public void removeAllComponents() {
		if (count != 0) {
			JComponent previousComponent = getActiveComponent();
			JComponent[] c = getAllComponents();
			components = null;
			count = 0;
			tabIndex = -1;
			updateWindowComponent();
			for (int i = 0; i < c.length; i++) {
				fireComponentRemoved(c[i]);
			}
			fireComponentChanged(previousComponent);
		}
	}

	/**
	 * Returns all added components (without their names). The method returns an
	 * empty array if no components exist.
	 */
	public JComponent[] getAllComponents() {
		JComponent[] result = new JComponent[count];
		for (int i = 0; i < count; i++) {
			result[i] = components[i].component;
		}
		return result;
	}

	/**
	 * Returns the currently active component, that is the component that is
	 * shown as the window's content. The method returns null if no components
	 * exist.
	 */
	public JComponent getActiveComponent() {
		if (count == 0) {
			return null;
		}
		return components[tabIndex].component;
	}

	/**
	 * Sets the active component, that is the component that is shown as the
	 * window's content. The method fails silently if the name is unknown or
	 * null. The first component with that name is shown.
	 */
	public void setActiveComponent(String tabName) {
		for (int i = 0; i < count; i++) {
			if (components[i].tabName.equals(tabName)) {
				if (tabIndex != i) {
					tabIndex = i;
					if (count > 1 && windowComponent != null) {
						((JTabbedPane) windowComponent.getComponent(0))
								.setSelectedIndex(i);
					}
				}
				return;
			}
		}
	}

	// -------------------------------------------------------------------------------------------

	/*
	 * Returns true if the window is displayed in the layout pane.
	 */
	boolean isVisible() {
		return visible;
	}

	/*
	 * Marks the window as displayed in the layout pane. Setting visible to true
	 * DOES NOT automatically display the window. This is a state marker only.
	 */
	void setVisible(boolean visible) {
		this.visible = visible;
	}

	/*
	 * Returns the window' type (dockable or document).
	 */
	String getType() {
		return type;
	}

	/*
	 * Sets the window's type. Do not change after initialization.
	 */
	void setType(String type) {
		this.type = type;
	}

	/*
	 * Returns the window's Layout Manager.
	 */
	RLayoutManager getLayoutManager() {
		return manager;
	}

	/*
	 * Returns the text to use as tab title for tabbed windows. If a tabName was
	 * set, return that. Otherwise return the title or an empty string if
	 * there's no title.
	 */
	String getTitleForTab() {
		if (tabName != null) {
			return tabName;
		}
		if (title != null) {
			return title;
		}
		return "";
	}

	// -------------------------------------------------------------------------------------------

	/*
	 * Returns the window's content. It's always the same JPanel so that
	 * adding/removing a tab stripe doesn't affect the external component
	 * hierachy. The panel is empty if no components have need added so far. If
	 * one component has been addded, it shows that component. If multiple
	 * components have been added, the panel holds a JTabbedPane that holds the
	 * components.
	 */
	JComponent getWindowComponent() {
		if (windowComponent == null) {
			windowComponent = new JPanel(new BorderLayout());
			windowComponent.putClientProperty("model", this);
			createWindowComponent();
		}
		return windowComponent;
	}

	/*
	 * Updates the window's content after components were added or removed.
	 */
	void updateWindowComponent() {
		if (windowComponent != null) {
			if (tabbedPane != null) {
				tabbedPane.removeChangeListener(this);
				tabbedPane = null;
			}
			windowComponent.removeAll();
			createWindowComponent();
			windowComponent.validate();
			windowComponent.repaint();
		}
	}

	/*
	 * (Re)creates the window's content.
	 */
	void createWindowComponent() {
		if (count == 0) {
			// do nothing
		} else if (count == 1) {
			windowComponent.add(components[0].component);
		} else {
			tabbedPane = new JClosableTabbedPane(JTabbedPane.BOTTOM,
					JTabbedPane.SCROLL_TAB_LAYOUT);
			for (int i = 0; i < count; i++) {
				tabbedPane.addTab(components[i].tabName, components[i].icon,
						components[i].component);
			}
			tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.PLAIN, 9));
			tabbedPane.setSelectedIndex(tabIndex);
			tabbedPane.addChangeListener(this);
			tabbedPane.putClientProperty("contentBorder", Boolean.FALSE);
			windowComponent.add(tabbedPane);
		}
	}

	/*
	 * Change Listener for tabbedPane.
	 */
	public void stateChanged(ChangeEvent e) {
		JComponent previousComponent = getActiveComponent();
		tabIndex = tabbedPane.getSelectedIndex();
		fireComponentChanged(previousComponent);
	}

	// API methods
	// -------------------------------------------------------------------------------

	public void show() {
		getLayoutManager().showLayoutWindow(this);
	}

	public void hide() {
		getLayoutManager().hideLayoutWindow(this);
	}

	public void close() {
		getLayoutManager().closeLayoutWindow(this);
	}

	public void activate() {
		getLayoutManager().activateLayoutWindow(this);
	}

	void fireShown() {
		getLayoutManager().fireWindowShown(this);
	}

	void fireHidden() {
		getLayoutManager().fireWindowHidden(this);
	}

	// fire component events
	// ---------------------------------------------------------------------

	void fireComponentAdded(JComponent previousComponent) {
		Vector l = getLayoutManager().getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(getLayoutManager(),
					this, previousComponent);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowComponentAdded(e);
			}
		}
	}

	void fireComponentRemoved(JComponent previousComponent) {
		Vector l = getLayoutManager().getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(getLayoutManager(),
					this, previousComponent);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i))
						.layoutWindowComponentRemoved(e);
			}
		}
	}

	void fireComponentChanged(JComponent previousComponent) {
		Vector l = getLayoutManager().getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(getLayoutManager(),
					this, previousComponent);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowChanged(e);
			}
		}
	}

	public String toString() {
		return getClass().getName() + "[" + getName() + ", visible=" + visible
				+ "]";
	}

	void setSize(Dimension size) {
		if (getBounds() == null) {
			setBounds(new Rectangle(size));
		} else {
			setBounds(new Rectangle(getBounds().getLocation(), size));
		}
	}
}
