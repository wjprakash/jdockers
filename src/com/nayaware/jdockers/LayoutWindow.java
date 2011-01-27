package com.nayaware.jdockers;

import java.awt.Rectangle;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Layout Window is where the layout manager will place the components given to
 * it by the window manager This window takes special forms that can be docked
 * or floated or tabbed or autohided
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface LayoutWindow {
	/**
	 * Sets the name of the Layout Window. Used to identity the window object.
	 * Bound property.
	 * 
	 * @param name
	 *            Name of the Layout Window.
	 * @throws NullPointerException
	 *             if name is <code>null</code>.
	 */
	public void setName(String name);

	/**
	 * Returns the name of the Layout Window. Used to identity the window
	 * object.
	 * 
	 * @return Name of the Layout Window (never <code>null</code>)
	 */
	public String getName();

	/**
	 * Sets the title of the Layout Window. To be displayed as the title of the
	 * window. Bound property.
	 * 
	 * @param title
	 *            Title of the Layout Window (or <code>null</code>).
	 */
	public void setTitle(String title);

	/**
	 * Set the title of the Layout Window. Name that is displayed at the title
	 * bar of the window.
	 * 
	 * @return Title of the Layout Window (or <code>null</code>).
	 */
	public String getTitle();

	/**
	 * Sets the icon of the Layout Window. Bound property.
	 * 
	 * @param icon
	 *            Image Icon for the Layout Window (or <code>null</code>).
	 */
	public void setIcon(ImageIcon icon);

	/**
	 * Returns the icon of the Layout Window.
	 * 
	 * @return Image Icon of the Layout Window (or <code>null</code>).
	 */
	public ImageIcon getIcon();

	/**
	 * Sets the bounds of the Layout Window. Useful when the window is floated,
	 * ignored otherwise. Do <b>not</b> modify the bounds object later on. Bound
	 * property.
	 * 
	 * @param Rectangle
	 *            that holds the bounds.
	 * @throws NullPointerException
	 *             if bounds is <code>null</code>.
	 */
	public void setBounds(Rectangle bounds);

	/**
	 * Returns the bounds of the Layout Window. Do <b>not</b> modify the bounds
	 * object later on.
	 * 
	 * @return Bounds of the Layout Window (never <code>null</code>).
	 */
	public Rectangle getBounds();

	/**
	 * Sets the tab name of the Layout Window. Without this the name of window
	 * will be used. Bound property.
	 * 
	 * @param tabName
	 *            the tab name of the Layout Window (or <code>null</code>).
	 */
	public void setTabName(String tabName);

	/**
	 * Returns the tab name of the Layout Window.
	 * 
	 * @return tab name of the Layout Window (or <code>null</code>).
	 */
	public String getTabName();

	/**
	 * Sets if this Layout Window can be closed after firing a
	 * {@link LayoutWindowListener#layoutWindowClosed(LayoutWindowEvent)}.
	 * Unbound property.
	 * 
	 * @param closable
	 *            Set to <code>true</code> to let the Layout Manager close the
	 *            window and to <code>false</code> to veto the close operation.
	 */
	public void setClosable(boolean closable);

	/**
	 * Checks if this Layout Window can be closed.
	 * 
	 * @return whether the window is allowed to be closed by the Layout Manager
	 *         after firing
	 *         {@link LayoutWindowListener#layoutWindowClosed(LayoutWindowEvent)}
	 *         .
	 */
	public boolean isClosable();

	/**
	 * Adds a displayable component to the Layout Window. It is an error, if
	 * there is already a component with the same name.
	 * 
	 * @param comp
	 *            Component that need to be added to this Layout Window
	 * @param tabName
	 *            Name of the tab that holds this component
	 * @throws NullPointerException
	 *             if comp is <code>null</code>.
	 * @throws NullPointerException
	 *             if tabName is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if there's already a component with the same tab name.
	 */
	public void addComponent(JComponent comp, String tabName);

	/**
	 * Adds a displayable component to the Layout Window. It is an error, if
	 * there is already a component with the same name.
	 * 
	 * @param comp
	 *            Component that need to be added to this Layout Window
	 * @param tabName
	 *            Name of the tab that holds this component
	 * @param icon
	 *            optional icon displayed in front of the tabName.
	 * @throws NullPointerException
	 *             if comp is <code>null</code>.
	 * @throws NullPointerException
	 *             if tabName is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if there's already a component with the same tab name.
	 */
	public void addComponent(JComponent comp, String tabName, ImageIcon icon);

	/**
	 * Removes the named component. Fails silently if the name is unknown or
	 * <code>null</code>.
	 * 
	 * @param tabName
	 *            Name of the tab that holds the component.
	 */
	public void removeComponent(String tabName);

	/**
	 * Removes all the components from this Layout Window.
	 */
	public void removeAllComponents();

	/**
	 * Returns all the components contained by this Layout Window. The array
	 * returned is a copy - change if you like.
	 * 
	 * @return an array of all components (never <code>null</code>).
	 */
	public JComponent[] getAllComponents();

	/**
	 * Sets a new active component as if the user had clicked on the specified
	 * tabName. Fails silently if the name is unknown or <code>null</code>. The
	 * first component with that name is shown.
	 * 
	 * @param tabName
	 *            Name of the tab that holds the component.
	 */
	public void setActiveComponent(String tabName);

	/**
	 * Shows this window in the Layout Manager is was created from. Does nothing
	 * if the window is already shown.
	 */
	public void show();

	/**
	 * Hides this window. Does nothing if the window is already hidden.
	 */
	public void hide();

	/**
	 * Closes this window and frees resources. The window must be hidden.
	 */
	public void close();

	/**
	 * Activates this window. Does nothing if the window isn't shown or already
	 * the active window of the Layout Manager this window was created from.
	 */
	public void activate();

}
