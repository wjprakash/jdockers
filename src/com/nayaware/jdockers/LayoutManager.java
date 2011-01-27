package com.nayaware.jdockers;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * The Layout Manager manages the whole layout system. It creates the Layout
 * Windows and places them as instructed by the window manager into a Layout
 * Pane. It also fires window events to its listeners.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface LayoutManager {

	/**
	 * Type constant for
	 * {@link #createLayoutWindow(String, String, ImageIcon, String)}.
	 */
	public static final String TYPE_DOCKABLE = "dockable";

	/**
	 * Type constant for
	 * {@link #createLayoutWindow(String, String, ImageIcon, String)}.
	 */
	public static final String TYPE_DOCUMENT = "document";

	/**
	 * Adds a listener to this Layout Manager.
	 * 
	 * @param listener
	 *            the listener
	 * @throws NullPointerException
	 *             if listener is <code>null</code>
	 */
	public void addLayoutWindowListener(LayoutWindowListener listener);

	/**
	 * Removes the specified listener from this Layout Manager. Fails silently
	 * if the listener is unknown or <code>null</code>.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeLayoutWindowListener(LayoutWindowListener listener);

	/**
	 * Returns the Layout Pane associated with this Layout Manager.
	 * 
	 * @return the main component all (non-floating) layout windows are
	 *         contained in (never <code>null</code>).
	 */
	public JComponent getLayoutPane();

	/**
	 * Creates a new Layout Window. The name must be unique.
	 * 
	 * @param name
	 *            internal name of the layout window
	 * @param title
	 *            title that will be displayed corresponding to the layout
	 *            window
	 * @param icon
	 *            optional icon of the the layout window
	 * @param type
	 *            type of the layout window (dockable or document)
	 * @return Layout Window (never <code>null</code>).
	 * @throws NullPointerException
	 *             if name is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if type is neither dockable/document
	 * @throws LayoutManagerException
	 *             if name is not unique
	 */
	public LayoutWindow createLayoutWindow(String name, String title,
			ImageIcon icon, String type);

	/**
	 * Opens a Layout Window. The window must be created by this Layout Manager.
	 * 
	 * @param layoutWindow
	 *            Layout Window
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void openLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Closes a Layout Window. Fails silently if the window is still shown or
	 * wasn't opened. The window must be created by this Layout Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void closeLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Shows a Layout Window. The window must be opened. Does nothing if the
	 * window is already shown. The window must be created by this Layout
	 * Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void showLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Shows a Layout Window in AUTOHIDDEN mode. The window must be opened. Does
	 * nothing if the window is already shown. This window must be created by
	 * this Layout Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void autohideLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Hides the Layout Window. The window must be opened. Does nothing if the
	 * window is already hidden. For document windows, the window is only
	 * hidden, if listeners don't veto. The window must be created by this
	 * Layout Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void hideLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Activates the Layout Window. The window must be opened. Does nothing if
	 * the window isn't shown or is already active. The window must be created
	 * by this Layout Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void activateLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Updates the Layout Window display based on its state. Actually, this
	 * simply triggers the layoutWindowChanged event. The window must be created
	 * by this Layout Manager.
	 * 
	 * @param layoutWindow
	 *            LayoutWindow
	 * @throws NullPointerException
	 *             if window is <code>null</code>
	 * @throws LayoutManagerException
	 *             if window wasn't created by this Layout Manager
	 */
	public void updateLayoutWindow(LayoutWindow layoutWindow);

	/**
	 * Finds a Layout Window.
	 * 
	 * @param layoutWindowName
	 * @return LayoutWindow or <code>null</code> the the window name is unkown
	 *         or <code>null</code>
	 */
	public LayoutWindow findLayoutWindow(String layoutWindowName);

	/**
	 * Returns all the opened Layout Windows. The array is a copy - modify if
	 * you like.
	 * 
	 * @return Subscripted array of Layout Window (never <code>null</code>).
	 */
	public LayoutWindow[] getAllLayoutWindows();

	/**
	 * Hides all the Layout Windows which are opened and shown.
	 */
	public void hideAllLayoutWindows();

	/**
	 * Closes all the Layout Windows which were opened, shown and are hidden.
	 */
	public void closeAllLayoutWindows();

	/**
	 * Convenient method for create Dockable Window.
	 * 
	 * @param name
	 *            Name of the Dockable Window
	 * @param title
	 *            Title that will be displayed corresponding to the Dockable
	 *            Window
	 * @param icon
	 *            Icon of the Dockable Window
	 * @return Dockable Window
	 */
	public DockableWindow createDockableWindow(String name, String title,
			ImageIcon icon);

	/**
	 * Returns all the opened Dockable Windows in the Layout Pane. The array is
	 * a copy - modify if you like.
	 * 
	 * @return Dockable Windows (never <code>null</code>)
	 */
	public DockableWindow[] getAllDockableWindows();

	/**
	 * Finds a Dockable Window by name. This method will return null, if there's
	 * a window with that name but not of type "dockable".
	 * 
	 * @param dockableWindowName
	 *            name of the Dockable Window
	 * @return Dockable Window
	 */
	public DockableWindow findDockableWindow(String dockableWindowName);

	/**
	 * Hide all shown dockable windows in the layout pane.
	 */
	public void hideAllDockableWindows();

	/**
	 * Closes all opened the dockable windows in the layout pane.
	 */
	public void closeAllDockableWindows();

	/**
	 * Switches the dockable window to auto-hide mode (and hides it).
	 * 
	 * @param dockableWindow
	 *            Dockbale Window
	 */
	public void autohideDockableWindow(DockableWindow dockableWindow);

	/**
	 * Floats the dockable window it docked or auto-hidden.
	 * 
	 * @param dockableWindow
	 *            Dockbale Window
	 */
	public void floatDockableWindow(DockableWindow dockableWindow);

	/**
	 * Docks a floating or auto-hidden dockable window back to the Layout Pane.
	 * 
	 * @param dockableWindow
	 *            Dockbale Window
	 */
	public void redockDockableWindow(DockableWindow dockableWindow);

	/**
	 * Convenient method to create the Document Window
	 * 
	 * @param name
	 *            String - Name of the document window
	 * @param title
	 *            Title that will be displayed corresponding to the document
	 *            window
	 * @param icon
	 *            Icon of the the document window
	 * @return Document Window
	 */
	public DocumentWindow createDocumentWindow(String name, String title,
			ImageIcon icon);

	/**
	 * Finds a Document Window by name This method will return null, if there's
	 * a window with that name but not of type "document".
	 * 
	 * @param documentWindowName
	 *            name of the Document Window
	 * @return Document Window
	 */
	public DocumentWindow findDocumentWindow(String documentWindowName);

	/**
	 * Returns all the opened Document Windows in the Layout Pane. The array is
	 * a copy - modify if you like.
	 * 
	 * @return Array of all document windows (never <code>null</code>)
	 */
	public DocumentWindow[] getAllDocumentWindows();

	/**
	 * Hides all the Dockable Windows managed in this layout pane.
	 */
	public void hideAllDocumentWindows();

	/**
	 * Closes all the Document window in the layout pane.
	 */
	public void closeAllDocumentWindows();

	/**
	 * Defines the specified set of window names as a new "window set". Both
	 * DockableWindow and DocumentWindow should have unique name among them.
	 * Silently overwrites older definitions with the same windowSetName.
	 * 
	 * @param windowSetName
	 * @param windowNames
	 * @throws NullPointerException
	 *             if windowSetName is <code>null</code>
	 * @throws NullPointerException
	 *             if windowNames is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if windowNames contains something other than Strings.
	 */
	public void addWindowSet(String windowSetName, Set windowNames);

	/**
	 * Removes the named Window set definition. Fails silently if the
	 * windowSetName is unknown or <code>null</code>.
	 * 
	 * @param windowSetName
	 */
	public void removeWindowSet(String windowSetName);

	/**
	 * Shows the named Window set. All hidden opened windows whose names are in
	 * the window set definition are shown. All other windows are left
	 * untouched. Fails silently if the windowSetName is unknown or
	 * <code>null</code>.
	 * 
	 * @param windowSetName
	 */
	public void showWindowSet(String windowSetName);

	/**
	 * Hides the named Window set. All opened and shown windows whose names are
	 * in the window set definition are hidden. All other windows are left
	 * untouched. Fails silently if the windowSetName is unknown or
	 * <code>null</code>.
	 * 
	 * @param windowSetName
	 */
	public void hideWindowSet(String windowSetName);

	/**
	 * Show only the windows in the vector of names and give the window set a
	 * default name and make it default
	 * 
	 * @param windowNames
	 */
	public void showWindowSet(Set windowNames);

	/**
	 * Save the layout to a file - across session persistence
	 * 
	 * @param layoutFile
	 * @throws NullPointerException
	 *             if layoutFile is <code>null</code>
	 * @throws IOException
	 *             if the file cannot be created, written or closed
	 */
	public void saveLayout(File layoutFile) throws IOException;

	/**
	 * Loads the layout from a file.
	 * 
	 * @param layoutFile
	 * @throws NullPointerException
	 *             if layoutFile is <code>null</code>
	 * @throws IOException
	 *             if the file cannot be opened, read or closed
	 * @throws IOException
	 *             if the file contents is invalid (syntax error)
	 */
	public void loadLayout(File layoutFile) throws IOException;

	/**
	 * Retrieves the layout information as a String.
	 * 
	 * @return string encoding persistency data (bounds and states of all
	 *         windows)
	 */
	public String getLayout();

	/**
	 * Restores a layout from the specified string.
	 * 
	 * @param layoutData
	 * @throws IOException
	 *             if the file contents is invalid (syntax error)
	 */
	public void setLayout(String layoutData) throws IOException;

	/**
	 * Flag for enabling Layout Manager Debug
	 * 
	 * @param enable
	 */
	public void enableDebug(boolean enable);
}
