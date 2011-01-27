package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;

/**
 * This test demonstrates adding and removing of Dockable Windows. Also
 * demonstrates changing title property and activating windows.
 */
public class Test04 extends TestBase {

	static int id;

	static DockableWindow win(String title, String message) {
		DockableWindow w = manager.createDockableWindow(String.valueOf(++id),
				title, icon);
		w.addComponent(new TestComponent(message), "");
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	public static void main(String[] args) {
		DockableWindow w;
		w = win("Ship", "Osiris");
		manager.showLayoutWindow(w);

		setup("Test 04");

		space();
		w.setTitle("The last flight of the ...");
		space();
		w = win("Ship", "Nebukatneza");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_TOP);
		manager.showLayoutWindow(w);
		space();
		w = win("Sanctury", "Zion");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_LEFT);
		manager.showLayoutWindow(w);
		space();
		w = win("Agent 1", "Smith");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_BOTTOM);
		manager.showLayoutWindow(w);
		space();
		w = win("Agent 2", "Jones");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_BOTTOM);
		manager.showLayoutWindow(w);
		space();
		w = win("Agent 3", "Brown");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_BOTTOM);
		manager.showLayoutWindow(w);
		space();
		manager.findDockableWindow("4").setTabName("1");
		manager.findDockableWindow("5").setTabName("2");
		manager.findDockableWindow("6").setTabName("3");
		space();
		end();
	}
}
