package com.nayaware.jdockers.test;

import java.io.IOException;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;

/**
 * This test demonstrates adding and removing of Dockable Windows. Also
 * demonstrates changing title property and activating windows.
 */
public class Test09 extends TestBase {

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
				title, icon);
		w.addComponent(new TestComponent(message), "");
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	static void hideAndShow() throws IOException {
		String layout = manager.getLayout();
		manager.hideAllLayoutWindows();
		space();
		manager.setLayout(layout);
		space();
	}

	public static void main(String[] args) throws IOException {
		DockableWindow w;
		w = win("Ship", "Osiris");
		manager.showLayoutWindow(w);

		setup("Test 09");

		space();

		hideAndShow();

		w = win("Ship", "Nebukatneza");
		manager.showLayoutWindow(w);
		space();

		hideAndShow();

		w = win("Agent", "Smith");
		w.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(w);
		space();

		hideAndShow();

		w = dw("Matric", "Virtual World");
		manager.showLayoutWindow(w);
		space();

		hideAndShow();

		end();
	}
}
