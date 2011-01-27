package com.nayaware.jdockers.custom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * A <code>JTabbedPane</code> that has an optional close button in its top or
 * bottom right corner if used in <code>TAB_SCROLL_LAYOUT</code>. This
 * implementation also fixes a number of well known bugs in the 1.4.2_01
 * implementation: The tabbedpane doesn't get mouse events (now it does) and
 * scroll pane and buttons aren't correctly removed (now they are).
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class JClosableTabbedPane extends JTabbedPane {

	private static final String uiClassID = "ClosableTabbedPaneUI";

	static {
		// listen for look and feel changes and add the missing information
		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("lookAndFeel".equals(evt.getPropertyName())) {
					if ("GTK".equals(((LookAndFeel) evt.getOldValue()).getID())) {
						// remove GTK specific additions set in setUiClassName
						UIDefaults table = UIManager.getDefaults();
						table.put("TabbedPane.tabInsets", null);
						table.put("TabbedPane.selectedTabPadInsets", null);
						table.put("TabbedPane.tabAreaInsets", null);
						table.put("TabbedPane.contentBorderInsets", null);
						table.put("TabbedPane.background", null);
						table.put("TabbedPane.foreground", null);
						table.put("TabbedPane.light", null);
						table.put("TabbedPane.highlight", null);
						table.put("TabbedPane.shadow", null);
						table.put("TabbedPane.darkShadow", null);
						table.put("TabbedPane.focus", null);
						table.put("TabbedPane.font", null);
					}
					setUiClassName();
				}
			}
		};
		UIManager.addPropertyChangeListener(listener);
		setUiClassName();
	}

	private static void setUiClassName() {
		// we have one implementation for each look and feel...
		String uiClassName = "com.sun.winsys.swing.BasicClosableTabbedPaneUI";
		String id = UIManager.getLookAndFeel().getID();
		if ("Windows".equals(id)) {
			uiClassName = "com.sun.winsys.swing.WindowsClosableTabbedPaneUI";
			UIDefaults table = UIManager.getDefaults();
			Color bg = (ColorUIResource) table
					.getColor("TabbedPane.background");
			table.put("TabbedPane.selected", new ColorUIResource(bg));
			// table.put("TabbedPane.selected", new
			// ColorUIResource(GTKClosableTabbedPaneUI.multiplyColor(bg,
			// 1.09)));
			table.put("TabbedPane.background", new ColorUIResource(
					GTKClosableTabbedPaneUI.multiplyColor(bg, 0.912)));
		} else if ("Motif".equals(id)) {
			uiClassName = "com.sun.winsys.swing.MotifClosableTabbedPaneUI";
		} else if ("Metal".equals(id)) {
			uiClassName = "com.sun.winsys.swing.MetalClosableTabbedPaneUI";
		} else if ("GTK".equals(id)) {
			uiClassName = "com.sun.winsys.swing.GTKClosableTabbedPaneUI";
			// the gtk look is lacking these definitions so that the
			// BasicTabbedPaneUI
			// would throws NullPointerException if these were missing...
			UIDefaults table = UIManager.getDefaults();
			table.put("TabbedPane.tabInsets", new Insets(0, 4, 1, 4));
			table.put("TabbedPane.selectedTabPadInsets", new Insets(2, 2, 2, 1));
			table.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 2));
			table.put("TabbedPane.contentBorderInsets", new Insets(2, 2, 3, 3));
			// and it is lacking the following color definitions...
			ColorUIResource bg = new ColorUIResource(214, 214, 214);
			ColorUIResource fg = new ColorUIResource(Color.black);
			table.put("TabbedPane.background", bg);
			table.put("TabbedPane.foreground", fg);
			table.put("TabbedPane.light", bg);
			table.put("TabbedPane.highlight",
					new ColorUIResource(bg.brighter()));
			table.put("TabbedPane.shadow", new ColorUIResource(bg.darker()));
			table.put("TabbedPane.darkShadow", fg);
			table.put("TabbedPane.focus", fg);
			table.put("TabbedPane.font", new FontUIResource("sansserif",
					Font.PLAIN, 10));
			// a few additional definitions for internal frames
			ColorUIResource gradient = new ColorUIResource(
					GTKClosableTabbedPaneUI.multiplyColor(bg, 0.888));
			ColorUIResource title = new ColorUIResource(Color.white);
			table.put("InternalFrame.activeTitleBackground",
					new ColorUIResource(0, 0, 130));
			table.put("InternalFrame.activeTitleGradient", gradient);
			table.put("InternalFrame.activeTitleForeground", title);
			table.put(
					"InternalFrame.inactiveTitleBackground",
					new ColorUIResource(GTKClosableTabbedPaneUI.multiplyColor(
							bg, 0.5)));
			table.put("InternalFrame.inactiveTitleGradient", gradient);
			table.put("InternalFrame.inactiveTitleForeground", title);
		}
		UIManager.put(uiClassID, uiClassName);
	}

	private boolean closable;

	public JClosableTabbedPane() {
		super();
	}

	public JClosableTabbedPane(int tabPlacement) {
		super(tabPlacement);
	}

	public JClosableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
	}

	/**
	 * Returns <code>true</code> if this tabbedpane has a close button.
	 */
	public boolean isCloasable() {
		return closable;
	}

	/**
	 * Sets whether this tabbedpane shall display a close button or not.
	 */
	public void setClosable(boolean closable) {
		if (this.closable != closable) {
			this.closable = closable;
			firePropertyChange("closable", !closable, closable);
		}
	}

	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Adds an <code>ActionListener</code> to this tabbedpane to listen for
	 * close notifications, generated either by clicking the component's close
	 * button or by pressing Ctrl+F4.
	 */
	public void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	/**
	 * Removes an <code>ActionListener</code> from this tabbedpane.
	 */
	public void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	/**
	 * Notfies all listeners that the close action (generated either by clicking
	 * the component's close button or by pressing Ctrl+F4) was performed.
	 */
	protected void fireCloseAction() {
		ActionListener[] l = (ActionListener[]) listenerList
				.getListeners(ActionListener.class);
		if (l.length > 0) {
			ActionEvent e = new ActionEvent(this, getSelectedIndex(), "close");
			for (int i = 0; i < l.length; i++) {
				l[i].actionPerformed(e);
			}
		}
	}
}
