package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;

/**
 * This test demonstrates adding and removing both Document and Dockable
 * Windows. Also demonstrates auto-hiding windows.
 * 
 * TODO: un-auto-hide windows by calling show ?! TODO: auto-hide to all four
 * edges TODO: auto-hide tabbed dingens
 */
public class Test05 extends TestBase {

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

		manager.showLayoutWindow(dw("/home/sma/document1.txt", "doc1"));

		setup("Test 05");

		space();
		w = win("Neo", "The hacker");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_LEFT);
		manager.showLayoutWindow(w);
		space();

		manager.showLayoutWindow(dw("/home/sma/document2.txt", "doc2"));
		space();

		w = win("Trinity", "The tough girl");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();

		manager.showLayoutWindow(dw("/home/sma/document3.txt", "doc3"));
		space();

		w = win("Morpheus", "The father figure");
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
