package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.LayoutWindow;
import com.nayaware.jdockers.LayoutWindowEvent;
import com.nayaware.jdockers.LayoutWindowListener;
import com.nayaware.jdockers.impl.RLayoutManager;

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
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/*
 * @author Winston Prakash
 * @version 1.0
*/
public class LayoutTestFrame extends JFrame implements LayoutWindowListener {

	private JPanel layoutContainer;
	private JMenuBar menuBar = new JMenuBar();

	private Map windowMap = new HashMap();
	private Map topComponentMap = new HashMap();

	private LayoutWindow activeWindow;

	private static final String DOCKABLE = "dockable";
	private static final String DOCUMENT = "document";

	private static final ImageIcon WINICON = new ImageIcon(
			LayoutTestFrame.class.getResource("icon.png"));

	private String[] windowNames = { "navigator", "palette", "properties",
			"projects", "welcome", "designer", "output" };
	private String[] windowTitles = { "Server Navigator", "Component Palette",
			"Property Sheet", "Project Manager", "Welcome", "Designer",
			"Output" };
	private ImageIcon[] windowIcons = { WINICON, WINICON, WINICON, WINICON,
			WINICON, WINICON, WINICON };
	private String[] windowTypes = { DOCKABLE, DOCKABLE, DOCKABLE, DOCKABLE,
			DOCUMENT, DOCUMENT, DOCKABLE };
	private String[] windowSides = { DockableWindow.DOCK_SIDE_LEFT,
			DockableWindow.DOCK_SIDE_LEFT, DockableWindow.DOCK_SIDE_RIGHT,
			DockableWindow.DOCK_SIDE_RIGHT, DockableWindow.DOCK_SIDE_TOP,
			DockableWindow.DOCK_SIDE_TOP, DockableWindow.DOCK_SIDE_BOTTOM };

	private JScrollPane scrollPane;
	private JPanel debugAreaContainer;

	boolean shallAsk = false;

	LayoutManager layoutManager = new RLayoutManager();
	DebugArea DebugArea = new DebugArea();

	// ------------------------------------------ Test Component BEGIN
	// -------------------------------------

	public static class RaveTopComponent extends JPanel {
		private static int count = 0;

		private String displayName;
		private String name;
		private String type;
		private String initDockSide;
		private ImageIcon icon;
		private boolean persist = true;

		JLabel jLabel1 = new JLabel();

		public RaveTopComponent() {
			initComponents();
			int red = (int) (Math.random() * 200) + 50;
			int green = (int) (Math.random() * 200) + 50;
			int blue = (int) (Math.random() * 200) + 50;
			setBackground(new Color(red, green, blue));
		}

		private void initComponents() {
			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(40, 40));
			setMinimumSize(new Dimension(10, 10));
			jLabel1.setFont(new Font("Times New Roman", 1, 16));
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setBorder(new EtchedBorder());
			add(jLabel1, BorderLayout.CENTER);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setPersistance(boolean persist) {
			this.persist = persist;
		}

		public boolean canPersist() {
			return persist;
		}

		public void setDisplayName(final String dispName) {
			displayName = dispName;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jLabel1.setText(dispName);
				}
			});
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public void setInitDockSide(String initDockSide) {
			this.initDockSide = initDockSide;
		}

		public String getInitDockSide() {
			return initDockSide;
		}

		public void setIcon(ImageIcon icon) {
			this.icon = icon;
		}

		public ImageIcon getIcon() {
			return icon;
		}
	}

	// ------------------------------------------ Test Component END
	// -------------------------------------

	// ------------------------------------------ DebugArea BEGIN
	// -------------------------------------

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

	// ------------------------------------------ DebugArea END
	// -------------------------------------

	public LayoutTestFrame() {
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

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				saveTopComponents();
				saveLayout();
				System.exit(0);
			}

			public void windowOpened(WindowEvent e) {
				loadTopComponents();
				loadLayout();
			}

		});

		layoutContainer.setLayout(new BorderLayout());

		layoutContainer.setPreferredSize(new Dimension(700, 700));
		getContentPane().add(layoutContainer, BorderLayout.CENTER);

		debugAreaContainer.setLayout(new BorderLayout());
		debugAreaContainer.setPreferredSize(new Dimension(700, 200));
		debugAreaContainer.add(scrollPane, BorderLayout.CENTER);

		getContentPane().add(debugAreaContainer, BorderLayout.SOUTH);

		final JMenu fileMenu = new JMenu("File");

		JMenuItem redeployMenuItem = new JMenuItem();
		redeployMenuItem.setText("Redeploy");
		redeployMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				redeployTopComponents();
			}
		});

		fileMenu.add(redeployMenuItem);

		JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setText("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveTopComponents();
				saveLayout();
				System.exit(0);
			}
		});

		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);

		menuBar.add(createViewMenu());
		setJMenuBar(menuBar);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 700) / 2, (screenSize.height - 700) / 2,
				700, 700);
	}

	public JMenu createViewMenu() {
		JMenu viewMenu = new JMenu();
		viewMenu.setText("View");
		for (int i = 0; i < windowNames.length; i++) {
			JMenuItem menuItem = new JMenuItem();
			menuItem.setText(windowTitles[i]);
			menuItem.setActionCommand(windowNames[i]);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					RaveTopComponent topComp = getTopComponent(evt
							.getActionCommand());
					addTopComponent(topComp);
				}
			});
			viewMenu.add(menuItem);
		}
		return viewMenu;
	}

	public RaveTopComponent getTopComponent(String topCompName) {
		RaveTopComponent topComp = (RaveTopComponent) topComponentMap
				.get(topCompName);
		if (topComp == null) {
			for (int i = 0; i < windowNames.length; i++) {
				if (windowNames[i].equals(topCompName)) {
					topComp = new RaveTopComponent();
					topComp.setName(windowNames[i]);
					topComp.setDisplayName(windowTitles[i]);
					topComp.setType(windowTypes[i]);
					topComp.setInitDockSide(windowSides[i]);
					topComp.setIcon(windowIcons[i]);
					if (topComp.getName().equals("output"))
						topComp.setPersistance(false);
					topComponentMap.put(topCompName, topComp);
					return topComp;
				}
			}
		}
		return topComp;
	}

	public void saveTopComponents() {
		if (!topComponentMap.isEmpty()) {
			String userHome = System.getProperty("user.home");
			File tcsFile = new File(userHome, "tcsList.xml");
			System.out.println(tcsFile);
			try {
				XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
						new FileOutputStream(tcsFile)));
				Set keySet = windowMap.keySet();
				Iterator iter = keySet.iterator();
				int count = 0;
				while (iter.hasNext()) {
					RaveTopComponent topComp = (RaveTopComponent) iter.next();
					if (topComp.canPersist())
						count++;
				}
				encoder.writeObject(new Integer(count));
				iter = keySet.iterator();
				while (iter.hasNext()) {
					RaveTopComponent topComp = (RaveTopComponent) iter.next();
					if (topComp.canPersist())
						encoder.writeObject(topComp);
				}
				encoder.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public boolean loadTopComponents() {
		String userHome = System.getProperty("user.home");
		File tcsFile = new File(userHome, "tcsList.xml");
		if (tcsFile.exists()) {
			try {
				XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
						new FileInputStream(tcsFile)));
				int tcsNums = ((Integer) decoder.readObject()).intValue();
				for (int i = 0; i < tcsNums; i++) {
					try {
						RaveTopComponent topComp = (RaveTopComponent) decoder
								.readObject();
						topComponentMap.put(topComp.getName(), topComp);
						addTopComponent(topComp);
					} catch (Exception exc) {
						exc.printStackTrace();
						decoder.close();
					}
				}
				decoder.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} else {
			return false;
		}
		return true;
	}

	public void saveLayout() {
		String userHome = System.getProperty("user.home");
		File layoutFile = new File(userHome, "test.layout");
		try {
			layoutManager.saveLayout(layoutFile);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void loadLayout() {
		String userHome = System.getProperty("user.home");
		File layoutFile = new File(userHome, "test.layout");
		if (layoutFile.exists()) {
			try {
				layoutManager.loadLayout(layoutFile);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	private void addTopComponent(RaveTopComponent topComp) {
		// LayoutWindow window = (LayoutWindow) windowMap.get(topComp);
		LayoutWindow window = layoutManager.findLayoutWindow(topComp.getName());
		if (window == null) {
			ImageIcon tabIcon = new ImageIcon(this.getClass().getResource(
					"frame.png"));
			if (DOCUMENT.equals(topComp.getType())) {
				System.out.println("Creating: " + topComp.getName());
				window = layoutManager.createDocumentWindow(topComp.getName(),
						topComp.getDisplayName(), topComp.getIcon());
			} else {
				window = layoutManager.createDockableWindow(topComp.getName(),
						topComp.getDisplayName(), topComp.getIcon());
				((DockableWindow) window).setInitialDockSide(topComp
						.getInitDockSide());
			}
			window.addComponent(topComp, topComp.getName());
			window.setBounds(new Rectangle(200, 200));
			layoutManager.showLayoutWindow(window);
			windowMap.put(topComp, window);
		}
		layoutManager.showLayoutWindow(window);
		layoutManager.activateLayoutWindow(window);
	}

	private void hideTopComponent(RaveTopComponent topComp) {
		// LayoutWindow window = (LayoutWindow) windowMap.get(topComp);
		LayoutWindow window = layoutManager.findLayoutWindow(topComp.getName());
		if (window != null) {
			System.out.println("Hiding - " + topComp.getName());
			layoutManager.hideLayoutWindow(window);
		}
	}

	private void removeTopComponent(RaveTopComponent topComp) {
		LayoutWindow window = (LayoutWindow) windowMap.get(topComp);
		if (window != null) {
			System.out.println("Removing - " + topComp.getName());
			layoutManager.closeLayoutWindow(window);
			windowMap.remove(topComp);
		}
	}

	private void redeployTopComponents() {
		if (!windowMap.keySet().isEmpty()) {
			Vector removedTopComps = new Vector();
			Iterator iter = windowMap.keySet().iterator();
			while (iter.hasNext()) {
				RaveTopComponent topComp = (RaveTopComponent) iter.next();
				removedTopComps.add(topComp);
			}
			for (int i = 0; i < removedTopComps.size(); i++) {
				RaveTopComponent topComp = (RaveTopComponent) removedTopComps
						.get(i);
				hideTopComponent(topComp);
				removeTopComponent(topComp);
			}
			for (int i = 0; i < removedTopComps.size(); i++) {
				RaveTopComponent topComp = (RaveTopComponent) removedTopComps
						.get(i);
				addTopComponent(topComp);
			}
		}

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
	}

	public void layoutWindowOpened(LayoutWindowEvent layoutWindowEvent) {
		LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText("Layout Window Opened - "
				+ layoutWindow.getTitle() + " Active Component: "
				+ activeComponentName(layoutWindowEvent));
	}

	public void layoutWindowShown(LayoutWindowEvent layoutWindowEvent) {
		final LayoutWindow layoutWindow = layoutWindowEvent.getLayoutWindow();
		DebugArea.setDebugText(
				"Layout Window Shown - " + layoutWindow.getTitle()
						+ " Active Component: "
						+ activeComponentName(layoutWindowEvent), "lightBlue");
	}

	public void layoutWindowHidden(LayoutWindowEvent layoutWindowEvent) {
		// Thread.dumpStack();
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
		LayoutTestFrame testFrame = new LayoutTestFrame();
		testFrame.show();
	}
}
