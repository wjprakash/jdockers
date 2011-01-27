package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;

/**
 * Combining it all
 */
public class Test07 extends TestBase {

	static int id;

	static DocumentWindow dw(String title, String tabName) {
		DocumentWindow w = manager.createDocumentWindow(String.valueOf(++id),
				title, icon);
		w.setTabName(tabName);
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	static DockableWindow win(String title, String message) {
		DockableWindow w = manager.createDockableWindow(String.valueOf(++id),
				title, icon2);
		w.addComponent(new TestComponent(message), "");
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	public static void main(String[] args) {
		DockableWindow w;
		DocumentWindow doc;

		doc = dw("/home/sma/document1.txt", "Zion");
		doc.addComponent(new TestComponent("The sanctuary"), "Zion");
		manager.showLayoutWindow(doc);

		setup("Test 07");

		space();
		w = win("Neo", "The hacker");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();
		w = win("Trinity", "The tough girl");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();
		w = win("Morpheus", "The father figure");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);

		doc = dw("/home/sma/document2.txt", "The Matrix");
		doc.addComponent(new TestComponent("The unreal world"), "The Matrix");
		manager.showLayoutWindow(doc);
		space();

		w = win("Nebukatneza", "Morpheus' ship");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();
		w = win("Osiris", "The last flight of the...");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();

		doc = dw("/home/sma/document3.txt", "Agents");
		doc.addComponent(new TestComponent("Agent Smith"), "Smith");
		doc.addComponent(new TestComponent("Agent Jones"), "Jones");
		doc.addComponent(new TestComponent("Agent Brown"), "Brown");
		manager.showLayoutWindow(doc);
		space();

		w = win("Zypher", "The deceiver");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_TOP);
		manager.showLayoutWindow(w);
		System.out.println(manager.getLayout());
		space();

		manager.autohideDockableWindow(manager.findDockableWindow("2"));
		System.out.println(manager.getLayout());
		space();
		manager.autohideDockableWindow(manager.findDockableWindow("4"));
		manager.autohideDockableWindow(manager.findDockableWindow("6"));
		System.out.println(manager.getLayout());
		space();

		end();
	}

}
