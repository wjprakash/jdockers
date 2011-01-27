package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;

/**
 * This test demonstrates de/activating window.
 */
public class Test06 extends TestBase {

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

		setup("Test 06");

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
		space();

		for (int i = 1; i <= 6; i++) {
			manager.activateLayoutWindow(manager.findLayoutWindow(String
					.valueOf(i)));
			space();
		}

		end();
	}
}
