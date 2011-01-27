package com.nayaware.jdockers.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;
import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.LayoutWindow;
import com.nayaware.jdockers.LayoutWindowEvent;
import com.nayaware.jdockers.LayoutWindowListener;
import com.nayaware.jdockers.impl.RLayoutManager;

/* Complete manual Test for the Window manager
 * @author Winston Prakash
 * @version 1.0
*/
public class TestFrame extends JFrame implements LayoutWindowListener {

	public static boolean unitTestMode;

	private static final int NONE = 0;
	private static final int TOP = 1;
	private static final int BOTTOM = 2;
	private static final int LEFT = 3;
	private static final int RIGHT = 4;

	private static final String SHOW = "Show";
	private static final String HIDE = "Hide Window";
	private static final String ACTIVATE = "Activate";
	private static final String CLOSE = "Close";

	private JPanel layoutContainer;
	private JMenuBar menuBar = new JMenuBar();

	private JMenu showWindowMenu = new JMenu("Show Window");
	private JMenu hideWindowMenu = new JMenu("Hide Window");
	private JMenu activateWindowMenu = new JMenu("Activate Window");
	private JMenu closeWindowMenu = new JMenu("Close Window");
	private JMenu showWindowSetMenu = new JMenu("Show Window Set");

	Map showMenuItems = new HashMap();
	Map hideMenuItems = new HashMap();
	Map activateMenuItems = new HashMap();
	Map closeMenuItems = new HashMap();

	private JScrollPane scrollPane;
	private JPanel debugAreaContainer;
	private JToolBar toolBar;

	boolean menusShown = false;

	static class TestComponent extends JPanel {
		private static int count = 0;
		private JLabel jLabel1;
		private String displayName;

		public TestComponent() {
			initComponents();
			int red = (int) (Math.random() * 200) + 50;
			int green = (int) (Math.random() * 200) + 50;
			int blue = (int) (Math.random() * 200) + 50;
			setBackground(new Color(red, green, blue));
			setName("Component " + count++);
			jLabel1.setText(getName());
		}

		private void initComponents() {
			jLabel1 = new JLabel();

			setLayout(new BorderLayout());

			setPreferredSize(new Dimension(40, 40));
			setMinimumSize(new Dimension(10, 10));
			jLabel1.setFont(new Font("Times New Roman", 1, 36));
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setText("Component 1");
			jLabel1.setBorder(new EtchedBorder());
			add(jLabel1, BorderLayout.CENTER);
		}
	}

	static class DebugArea extends JPanel {
		private JTextPane debugText;
		Document doc;
		Style styleContext = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		public DebugArea() {
			initComponents();

			Style styleRed = debugText.addStyle("red", styleContext);
			StyleConstants.setFontFamily(styleRed, "SansSerif");
			StyleConstants.setForeground(styleRed, Color.RED);

			Style styleBlue = debugText.addStyle("blue", styleContext);
			StyleConstants.setFontFamily(styleBlue, "SansSerif");
			StyleConstants.setForeground(styleBlue, Color.BLUE);

			Style styleLightBlue = debugText
					.addStyle("lightBlue", styleContext);
			StyleConstants.setFontFamily(styleLightBlue, "SansSerif");
			StyleConstants
					.setForeground(styleLightBlue, new Color(75, 75, 175));

			Style styleLightGreen = debugText.addStyle("lightGreen",
					styleContext);
			StyleConstants.setFontFamily(styleLightGreen, "SansSerif");
			StyleConstants.setForeground(styleLightGreen, new Color(100, 125,
					100));

			Style styleGreen = debugText.addStyle("green", styleContext);
			StyleConstants.setFontFamily(styleGreen, "SansSerif");
			StyleConstants.setForeground(styleGreen, new Color(50, 175, 50));

			Style styleBrown = debugText.addStyle("brown", styleContext);
			StyleConstants.setFontFamily(styleBrown, "SansSerif");
			StyleConstants.setForeground(styleBrown, new Color(100, 50, 50));

			Style styleLightBrown = debugText.addStyle("lightBrown",
					styleContext);
			StyleConstants.setFontFamily(styleLightBrown, "SansSerif");
			StyleConstants.setForeground(styleLightBrown, new Color(200, 100,
					70));

			Style styleOrange = debugText.addStyle("orange", styleContext);
			StyleConstants.setFontFamily(styleOrange, "SansSerif");
			StyleConstants.setForeground(styleOrange, new Color(240, 100, 50));

			Style stylePurple = debugText.addStyle("purple", styleContext);
			StyleConstants.setFontFamily(stylePurple, "SansSerif");
			StyleConstants.setForeground(stylePurple, new Color(240, 100, 250));
		}

		private void initComponents() {
			debugText = new JTextPane();
			doc = debugText.getDocument();
			setLayout(new BorderLayout());
			setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
			add(debugText, BorderLayout.CENTER);
			debugText.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) {
						JPopupMenu m = new JPopupMenu();
						m.add(new AbstractAction("Clear Debug Area") {
							public void actionPerformed(ActionEvent e) {
								debugText.setText("");
							}
						});
						m.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});

		}

		public void setDebugText(String text) {
			setDebugText(text, "blue");
		}

		public void setDebugText(final String text, final String color) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						doc.insertString(doc.getLength(), text + "\n",
								debugText.getStyle(color));
					} catch (BadLocationException exc) {
					}
				}
			});
		}
	}

	LayoutManager layoutManager = new RLayoutManager();
	DebugArea DebugArea = new DebugArea();

	private static int nameCounter = 0;
	private static int titleCounter = 0;

	private static String layoutData;

	private boolean shallAsk = true;

	/** Creates new form LayoutManagerTestFrame */
	public TestFrame() {
		initComponents();
		setTitle("TestFrame");
		layoutManager.addLayoutWindowListener(this);
		layoutContainer.add(layoutManager.getLayoutPane(), BorderLayout.CENTER);
		scrollPane.setViewportView(DebugArea);
	}

	private void initComponents() {
		layoutContainer = new JPanel();
		debugAreaContainer = new JPanel();
		scrollPane = new JScrollPane();
		toolBar = new JToolBar();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});

		layoutContainer.setLayout(new BorderLayout());

		layoutContainer.setPreferredSize(new Dimension(700, 700));
		getContentPane().add(layoutContainer, BorderLayout.CENTER);

		debugAreaContainer.setLayout(new BorderLayout());
		debugAreaContainer.setPreferredSize(new Dimension(700, 200));
		debugAreaContainer.add(scrollPane, BorderLayout.CENTER);

		getContentPane().add(debugAreaContainer, BorderLayout.SOUTH);

		toolBar.add(addDocumentWindowToolbarButton());
		toolBar.add(addDockableWindowToolbarButton(TOP));
		toolBar.add(addDockableWindowToolbarButton(BOTTOM));
		toolBar.add(addDockableWindowToolbarButton(LEFT));
		toolBar.add(addDockableWindowToolbarButton(RIGHT));
		toolBar.addSeparator();
		toolBar.add(addComponentButton());
		toolBar.add(removeComponentButton());
		getContentPane().add(toolBar, BorderLayout.NORTH);

		final JMenu fileMenu = new JMenu("File");

		fileMenu.add(new JMenuItem(new AbstractAction("Save Layout") {
			public void actionPerformed(ActionEvent e) {
				layoutData = layoutManager.getLayout();
				fileMenu.getItem(1).setEnabled(true);
				System.out.println(layoutData);
			}
		}), 0);

		fileMenu.add(new JMenuItem(new AbstractAction("Load Layout") {
			{
				this.setEnabled(false);
			}

			public void actionPerformed(ActionEvent e) {
				try {
					shallAsk = false;
					layoutManager.setLayout(layoutData);
					shallAsk = true;
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}), 1);

		JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setText("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});

		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);

		JMenu addMenu = new JMenu();
		addMenu.setText("AddWindow");

		addMenu.add(addDocumentWindowMenuItem());
		addMenu.add(addDockableWindowMenuItem(TOP));
		addMenu.add(addDockableWindowMenuItem(BOTTOM));
		addMenu.add(addDockableWindowMenuItem(LEFT));
		addMenu.add(addDockableWindowMenuItem(RIGHT));

		menuBar.add(addMenu);
		activateWindowMenu.setVisible(false);
		menuBar.add(activateWindowMenu);
		showWindowMenu.setVisible(false);
		menuBar.add(showWindowMenu);
		hideWindowMenu.setVisible(false);
		menuBar.add(hideWindowMenu);
		closeWindowMenu.setVisible(false);
		menuBar.add(closeWindowMenu);
		showWindowSetMenu.setVisible(false);
		menuBar.add(showWindowSetMenu);
		setJMenuBar(menuBar);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 700) / 2, (screenSize.height - 700) / 2,
				700, 700);
	}

	public JButton addDocumentWindowToolbarButton() {
		JButton button = new JButton();
		String iconName = "icon.png";
		button.setIcon(new ImageIcon(this.getClass().getResource(iconName)));
		button.setToolTipText("Add Document Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addDocumentWindow();
			}
		});
		return button;
	}

	public JButton addDockableWindowToolbarButton(final int side) {
		JButton button = new JButton();
		String iconName = "icon.png";
		String toolTip = "Add Dockable Window";
		switch (side) {
		case TOP:
			iconName = "top.png";
			toolTip = "Add Top Dockable Window";
			break;
		case BOTTOM:
			iconName = "bottom.png";
			toolTip = "Add Bottom Dockable Window";
			break;
		case RIGHT:
			iconName = "right.png";
			toolTip = "Add Right Dockable Window";
			break;
		case LEFT:
			iconName = "left.png";
			toolTip = "Add Left Dockable Window";
			break;
		}
		button.setIcon(new ImageIcon(this.getClass().getResource(iconName)));
		button.setToolTipText(toolTip);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addDockableWindow(side);
			}
		});
		return button;
	}

	private LayoutWindow activeWindow;

	public JButton addComponentButton() {
		JButton button = new JButton("+");
		button.setToolTipText("Add Component to Active Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (activeWindow != null) {
					TestComponent t = new TestComponent();
					activeWindow.addComponent(t, t.getName());
				}
			}
		});
		return button;
	}

	public JButton removeComponentButton() {
		JButton button = new JButton("-");
		button.setToolTipText("Remove Active Component from Active Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (activeWindow != null) {
					JComponent[] c = activeWindow.getAllComponents();
					if (c.length > 0) {
						activeWindow.removeComponent(c[c.length - 1].getName());
					}
				}
			}
		});
		return button;
	}

	public JMenuItem addDocumentWindowMenuItem() {
		JMenuItem menuItem = new JMenuItem();
		String iconName = "icon.png";
		menuItem.setIcon(new ImageIcon(this.getClass().getResource(iconName)));
		menuItem.setText("Add Document Window");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addDocumentWindow();
			}
		});
		return menuItem;
	}

	public JMenuItem addDockableWindowMenuItem(final int side) {
		JMenuItem menuItem = new JMenuItem();
		String iconName = "icon.png";
		String toolTip = "Add Dockable Window";
		switch (side) {
		case TOP:
			iconName = "top.png";
			toolTip = "Add Top Dockable Window";
			break;
		case BOTTOM:
			iconName = "bottom.png";
			toolTip = "Add Bottom Dockable Window";
			break;
		case RIGHT:
			iconName = "right.png";
			toolTip = "Add Right Dockable Window";
			break;
		case LEFT:
			iconName = "left.png";
			toolTip = "Add Left Dockable Window";
			break;
		}
		menuItem.setIcon(new ImageIcon(this.getClass().getResource(iconName)));
		menuItem.setText(toolTip);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addDockableWindow(side);
			}
		});
		return menuItem;
	}

	public void addToMenu(LayoutWindow layoutWindow, JMenu menu,
			Map menuCollection, String type) {
		if (!menuCollection.containsKey(layoutWindow.getName())) {
			MyMenuItem menuItem = new MyMenuItem();
			menuItem.setName(layoutWindow.getName());
			menuItem.setText(layoutWindow.getTitle());
			menuItem.setType(type);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					MyMenuItem menuItem = (MyMenuItem) evt.getSource();
					LayoutWindow layoutWindow = layoutManager
							.findLayoutWindow(menuItem.getName());
					if (menuItem.getType().equals(SHOW))
						layoutManager.showLayoutWindow(layoutWindow);
					if (menuItem.getType().equals(HIDE))
						layoutManager.hideLayoutWindow(layoutWindow);
					if (menuItem.getType().equals(ACTIVATE))
						layoutManager.activateLayoutWindow(layoutWindow);
					if (menuItem.getType().equals(CLOSE))
						layoutManager.closeLayoutWindow(layoutWindow);
				}
			});
			if (!menu.isVisible())
				menu.setVisible(true);

			menu.add(menuItem);
			menuCollection.put(layoutWindow.getName(), menuItem);
		}
	}

	class MyMenuItem extends JMenuItem {
		String type;

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	public void removeFromMenu(LayoutWindow layoutWindow, JMenu menu,
			Map menuCollection) {
		if (menuCollection.containsKey(layoutWindow.getName())) {
			JMenuItem menuItem = (JMenuItem) menuCollection.get(layoutWindow
					.getName());
			menu.remove(menuItem);
			menuCollection.remove(layoutWindow.getName());
			if (menuCollection.isEmpty())
				menu.setVisible(false);
		}

	}

	public String getLayoutWindowName() {
		return "LayoutWindow-" + nameCounter++;
	}

	public String getLayoutWindowTitleName() {
		return "Layout Window " + titleCounter++;
	}

	public ImageIcon getLayoutWindowIcon(int side) {
		String iconName = "icon.png";
		switch (side) {
		case TOP:
			iconName = "top.png";
			break;
		case BOTTOM:
			iconName = "bottom.png";
			break;
		case RIGHT:
			iconName = "right.png";
			break;
		case LEFT:
			iconName = "left.png";
			break;
		}
		return new ImageIcon(this.getClass().getResource(iconName));
	}

	private DocumentWindow addDocumentWindow() {
		return addDocumentWindow(true);
	}

	private DocumentWindow addDocumentWindow(boolean show) {
		// Add your handling code here:
		TestComponent comp = new TestComponent();
		ImageIcon icon = getLayoutWindowIcon(NONE);
		ImageIcon tabIcon = new ImageIcon(this.getClass().getResource(
				"frame.png"));
		String name = getLayoutWindowName();
		String title = getLayoutWindowTitleName();
		DebugArea.setDebugText("(Creating Dockable Window - " + title + ")",
				"green");
		DocumentWindow docWindow = layoutManager.createDocumentWindow(name,
				title, icon);
		docWindow.setBounds(new Rectangle(200, 200));
		docWindow.addComponent(comp, comp.getName(), tabIcon);
		TestComponent comp1 = new TestComponent();
		docWindow.addComponent(comp1, comp1.getName(), tabIcon);
		if (show) {
			DebugArea.setDebugText("(Showing Dockable Window - " + title + ")",
					"lightGreen");
			layoutManager.showLayoutWindow(docWindow);
		}
		return docWindow;
	}

	private DockableWindow addDockableWindow(int side) {
		return addDockableWindow(side, true);
	}

	private DockableWindow addDockableWindow(int side, boolean show) {
		ImageIcon icon = getLayoutWindowIcon(NONE);
		String dockSide = DockableWindow.DOCK_SIDE_LEFT;
		switch (side) {
		case TOP:
			icon = getLayoutWindowIcon(TOP);
			dockSide = DockableWindow.DOCK_SIDE_TOP;
			break;
		case BOTTOM:
			icon = getLayoutWindowIcon(BOTTOM);
			dockSide = DockableWindow.DOCK_SIDE_BOTTOM;
			break;
		case RIGHT:
			icon = getLayoutWindowIcon(RIGHT);
			dockSide = DockableWindow.DOCK_SIDE_RIGHT;
			break;
		case LEFT:
			icon = getLayoutWindowIcon(LEFT);
			dockSide = DockableWindow.DOCK_SIDE_LEFT;
			break;
		}
		TestComponent comp = new TestComponent();
		String name = getLayoutWindowName();
		String title = getLayoutWindowTitleName();
		DebugArea.setDebugText("(Creating Dockable Window - " + title + ")",
				"green");
		DockableWindow docWindow = layoutManager.createDockableWindow(name,
				title, icon);
		docWindow.setBounds(new Rectangle(200, 100));
		docWindow.addComponent(comp, comp.getName());
		docWindow.setInitialDockSide(dockSide);
		if (side == BOTTOM) {
			docWindow.setInitialDockState(DockableWindow.DOCK_STATE_AUTOHIDDEN);
		}
		if (show) {
			DebugArea.setDebugText("(Showing Dockable Window - " + title + ")",
					"lightGreen");
			layoutManager.showLayoutWindow(docWindow);
		}
		return docWindow;
	}

	public void addWindowSet(String setName, Set windowSet) {
		if (!showWindowSetMenu.isVisible())
			showWindowSetMenu.setVisible(true);
		layoutManager.addWindowSet(setName, windowSet);
		JMenuItem menuItem = new JMenuItem();
		menuItem.setName(setName);
		menuItem.setText(setName);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JMenuItem menuItem = (JMenuItem) evt.getSource();
				String setName = menuItem.getName();
				DebugArea.setDebugText(
						"(Showing Window Set - " + setName + ")", "brown");
				layoutManager.showWindowSet(setName);
			}
		});
		showWindowSetMenu.add(menuItem);
	}

	public static final String getOperatingSystem() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows"))
			return "windows";
		else if ("Solaris".equals(osName))
			return "solaris";
		else if (osName.startsWith("SunOS"))
			return "solaris";
		else if (osName.endsWith("Linux"))
			return "linux";
		else if (osName.equals("Mac OS X"))
			return "mac";
		return "unknown";
	}

	public void layoutWindowActivated(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		activeWindow = layoutWindow;
		DebugArea.setDebugText("Layout Window Activated - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent));
	}

	public void layoutWindowChanged(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText(
				"Layout Window Component Changed - "
						+ layoutWindow.getTitle()
						+ " Active Component: "
						+ activeComponentName(layoutWindowEvent)
						+ " Previous Component: "
						+ componentName(layoutWindowEvent
								.getPreviousComponent()), "purple");
	}

	public void layoutWindowComponentAdded(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Component Added - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent) + " Added: "
				+ componentName(layoutWindowEvent.getPreviousComponent()),
				"purple");
	}

	public void layoutWindowComponentRemoved(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Component Removed - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent) + " Removed: "
				+ componentName(layoutWindowEvent.getPreviousComponent()),
				"purple");
	}

	public void layoutWindowHiding(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Hiding - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent));
		if (shallAsk) {
			if (JOptionPane.showConfirmDialog(this,
					"Do you really want to close?", "Request",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				layoutWindow.setClosable(false);
			}
		}
	}

	public void layoutWindowClosed(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Closed - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent));
		removeFromMenu(layoutWindow, showWindowMenu, showMenuItems);
		removeFromMenu(layoutWindow, hideWindowMenu, hideMenuItems);
		removeFromMenu(layoutWindow, activateWindowMenu, activateMenuItems);
		removeFromMenu(layoutWindow, closeWindowMenu, closeMenuItems);
	}

	public void layoutWindowOpened(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Opened - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent));

		if (!menusShown) {
			activateWindowMenu.setVisible(true);
			showWindowMenu.setVisible(true);
			hideWindowMenu.setVisible(true);
			closeWindowMenu.setVisible(true);
			menusShown = true;
		}
		addToMenu(layoutWindow, showWindowMenu, showMenuItems, SHOW);
		addToMenu(layoutWindow, hideWindowMenu, hideMenuItems, HIDE);
		addToMenu(layoutWindow, activateWindowMenu, activateMenuItems, ACTIVATE);
		addToMenu(layoutWindow, closeWindowMenu, closeMenuItems, CLOSE);
	}

	public void layoutWindowShown(LayoutWindowEvent layoutWindowEvent) {
		final LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText(
				"Layout Window Shown - " + layoutWindow.getTitle()
						+ " Active Component: "
						+ activeComponentName(layoutWindowEvent), "lightBlue");
	}

	public void layoutWindowHidden(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText(
				"Layout Window Hidden - " + layoutWindow.getTitle()
						+ " Active Component: "
						+ activeComponentName(layoutWindowEvent), "lightBrown");
	}

	public void layoutWindowSaveNeeded(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText(
				"Layout Window Save Needed - " + layoutWindow.getTitle()
						+ " Active Component: "
						+ activeComponentName(layoutWindowEvent), "orange");
	}

	private String activeComponentName(LayoutWindowEvent layoutWindowEvent) {
		return componentName(layoutWindowEvent.getActiveComponent());
	}

	private String componentName(JComponent comp) {
		return comp == null ? "none" : comp.getName();
	}

	public static void main(String args[]) {
		String laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		String ui = null;
		if ((args.length > 0) && (args[0].startsWith("-ui")))
			ui = args[1];
		if (ui != null) {
			if ("windows".equals(ui)) {
				laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			} else if ("motif".equals(ui)) {
				laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			} else if ("gtk".equals(ui)) {
				laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			} else if ("metal".equals(ui)) {
				laf = "javax.swing.plaf.metal.MetalLookAndFeel";
			}
		} else {
			String os = getOperatingSystem();
			if ("windows".equals(os)) {
				laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			} else if ("solaris".equals(os)) {
				laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			} else if ("linux".equals(os)) {
				laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			} else if ("mac".equals(os)) {
				laf = "apple.laf.AquaLookAndFeel";
			}
		}
		try {
			UIManager.setLookAndFeel(laf);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		TestFrame testFrame = new TestFrame();
		if (!unitTestMode) {
			testFrame.addDocumentWindow();
			testFrame.addDockableWindow(LEFT);
			testFrame.addDockableWindow(RIGHT);
			testFrame.addDockableWindow(TOP);
			testFrame.addDockableWindow(BOTTOM);

			Set windowSet1 = new HashSet();
			windowSet1.add(testFrame.addDockableWindow(TOP, false).getName());
			windowSet1.add(testFrame.addDockableWindow(TOP, false).getName());
			windowSet1
					.add(testFrame.addDockableWindow(BOTTOM, false).getName());
			windowSet1.add(testFrame.addDockableWindow(RIGHT, false).getName());
			windowSet1.add(testFrame.addDockableWindow(RIGHT, false).getName());
			windowSet1.add(testFrame.addDocumentWindow(false).getName());

			Set windowSet2 = new HashSet();
			windowSet2.add(testFrame.addDockableWindow(LEFT, false).getName());
			windowSet2.add(testFrame.addDockableWindow(LEFT, false).getName());
			windowSet2
					.add(testFrame.addDockableWindow(BOTTOM, false).getName());
			windowSet2
					.add(testFrame.addDockableWindow(BOTTOM, false).getName());
			windowSet2.add(testFrame.addDockableWindow(RIGHT, false).getName());
			windowSet2.add(testFrame.addDocumentWindow(false).getName());

			testFrame.addWindowSet("Window Set 1", windowSet1);
			testFrame.addWindowSet("Window Set 2", windowSet2);
		}
		testFrame.show();
	}
}
