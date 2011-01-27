package com.nayaware.jdockers.test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.LayoutFactory;
import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.LayoutManagerException;
import com.nayaware.jdockers.LayoutWindow;
import com.nayaware.jdockers.impl.RLayoutManager;
import com.nayaware.jdockers.impl.RLayoutPane;
import com.nayaware.jdockers.util.Lookup;

import junit.framework.TestCase;

public class WindowTests extends TestCase {

	LayoutManager manager;
	JFrame f;

	static ImageIcon icon = new ImageIcon(
			WindowTests.class.getResource("icon.png"));

	protected void setUp() throws Exception {
		Lookup.getDefault().bind(LayoutManager.class, new RLayoutManager());
	}

	protected void tearDown() throws Exception {
		if (f != null) {
			f.dispose();
			f = null;
		}
		// forcefully reset cached Layout Manager in factory class
		Field f = LayoutFactory.class.getDeclaredField("lmInstance");
		f.setAccessible(true);
		f.set(null, null);
	}

	public void test01() {
		manager = LayoutFactory.getLayoutManager();

		// test: there should be a Layout Manager with Layout Pane
		assertNotNull(manager);
		assertNotNull(manager.getLayoutPane());
	}

	public void test02() {
		manager = LayoutFactory.getLayoutManager();

		f = new JFrame("Test 02");
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.show();

		// test: there should be an empty Layout Pane
		assertEquals(0, openComponents());
	}

	public void test03() {
		manager = LayoutFactory.getLayoutManager();

		f = new JFrame("Test 03");
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.show();

		// test: create dockable window
		DockableWindow w = manager.createDockableWindow("d", "A Window", icon);
		assertNotNull(w);
		assertEquals("d", w.getName());
		assertEquals("A Window", w.getTitle());
		assertEquals(icon, w.getIcon());

		// test: add window (should succeed)
		assertSame(w, manager.findDockableWindow("d"));
		assertSame(null, manager.findDockableWindow("dd"));

		// test: add window again (should fail)
		try {
			manager.openLayoutWindow(w);
			fail();
		} catch (LayoutManagerException e) {
		}
		try {
			manager.createDockableWindow("d", null, null);
			fail();
		} catch (LayoutManagerException e) {
		}
		try {
			manager.createDocumentWindow("d", null, null);
			fail();
		} catch (LayoutManagerException e) {
		}

		// test: show window
		w.show();
		assertEquals(1, openComponents());

		// test: show it again (should be ignored)
		w.show();
		assertEquals(1, openComponents());
	}

	public void test04() {
		manager = LayoutFactory.getLayoutManager();

		f = new JFrame("Test 04");
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.show();

		DockableWindow w = manager.createDockableWindow("d", "A Window", icon);
		w.show();

		// test: hide window
		w.hide();
		assertEquals(0, openComponents());

		// test: hide window again (should be ignored)
		w.hide();
		assertEquals(0, openComponents());

		// test: close window (should succeed)
		w.close();
		try {
			manager.openLayoutWindow(w);
			manager.closeLayoutWindow(w);
		} catch (LayoutManagerException e) {
			fail();
		}

		// test: close window again (should be ignored)
		manager.closeLayoutWindow(w);
	}

	public void test05() {
		manager = LayoutFactory.getLayoutManager();

		f = new JFrame("Test 05");
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.show();

		LayoutWindow w1 = manager.createLayoutWindow("d1", "A Window", icon,
				LayoutManager.TYPE_DOCKABLE);
		LayoutWindow w2 = manager.createLayoutWindow("d2", "A Window", icon,
				LayoutManager.TYPE_DOCKABLE);
		LayoutWindow w3 = manager.createLayoutWindow("d3", "A Window", icon,
				LayoutManager.TYPE_DOCUMENT);

		assertEquals(3, manager.getAllLayoutWindows().length);
		assertEquals(2, manager.getAllDockableWindows().length);
		assertEquals(1, manager.getAllDocumentWindows().length);

		assertSame(w1, manager.findLayoutWindow("d1"));
		assertSame(w2, manager.findLayoutWindow("d2"));
		assertSame(w3, manager.findLayoutWindow("d3"));
		assertSame(w2, manager.findDockableWindow("d2"));
		assertSame(w3, manager.findDocumentWindow("d3"));
		assertSame(null, manager.findDocumentWindow("d2"));
		assertSame(null, manager.findDockableWindow("d3"));

		w1.show();
		w2.show();
		w3.show();
		assertEquals(3, openComponents());
		manager.hideAllLayoutWindows();
		assertEquals(0, openComponents());

		w1.show();
		w2.show();
		w3.show();
		assertEquals(3, openComponents());
		manager.hideAllDockableWindows();
		assertEquals(1, openComponents());

		w1.show();
		w2.show();
		w3.show();
		assertEquals(3, openComponents());
		manager.hideAllDocumentWindows();
		assertEquals(2, openComponents());
	}

	static Set set(String s) {
		return new HashSet(Arrays.asList(s.split(",")));
	}

	public void test06() {
		manager = LayoutFactory.getLayoutManager();

		f = new JFrame("Test 06");
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.show();

		LayoutWindow w1 = manager.createLayoutWindow("d1", "A Window", icon,
				LayoutManager.TYPE_DOCKABLE);
		LayoutWindow w2 = manager.createLayoutWindow("d2", "A Window", icon,
				LayoutManager.TYPE_DOCKABLE);
		LayoutWindow w3 = manager.createLayoutWindow("d3", "A Window", icon,
				LayoutManager.TYPE_DOCUMENT);

		manager.addWindowSet("w1", set("d1,d2,d3"));
		manager.addWindowSet("w2", set("d1,d2,d4"));

		manager.showWindowSet("w1");
		assertEquals(3, openComponents());
		manager.hideAllLayoutWindows();
		assertEquals(0, openComponents());

		manager.showWindowSet("w2");
		assertEquals(2, openComponents());
		manager.hideAllLayoutWindows();
		assertEquals(0, openComponents());

		manager.removeWindowSet("w1");
		manager.showWindowSet("w1");
		assertEquals(0, openComponents());

		manager.showLayoutWindow(w1);
		manager.showLayoutWindow(w2);
		manager.showLayoutWindow(w3);
		manager.hideWindowSet("w2");
		assertEquals(1, openComponents());
	}

	private int openComponents() {
		return ((RLayoutPane) manager.getLayoutPane()).componentCountForTest();
	}

}
