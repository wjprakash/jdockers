package com.nayaware.jdockers.test;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DocumentWindow;
import com.nayaware.jdockers.LayoutWindowEvent;
import com.nayaware.jdockers.LayoutWindowListener;

/**
 * This test demonstrates the Layout Window Listener.
 */
public class Test08 extends TestBase {

	static class Listener implements LayoutWindowListener {

		private void effect(String event, LayoutWindowEvent layoutWindowEvent) {
			System.out.print("window ");
			System.out.print(event);
			System.out.print(": ");
			System.out.println(layoutWindowEvent.getLayoutWindow().getName());
		}

		public void layoutWindowOpened(LayoutWindowEvent layoutWindowEvent) {
			effect("opened", layoutWindowEvent);
		}

		public void layoutWindowHiding(LayoutWindowEvent layoutWindowEvent) {
			effect("closing", layoutWindowEvent);
		}

		public void layoutWindowClosed(LayoutWindowEvent layoutWindowEvent) {
			effect("closed", layoutWindowEvent);
		}

		public void layoutWindowShown(LayoutWindowEvent layoutWindowEvent) {
			effect("shown", layoutWindowEvent);
		}

		public void layoutWindowHidden(LayoutWindowEvent layoutWindowEvent) {
			effect("hidden", layoutWindowEvent);
		}

		public void layoutWindowActivated(LayoutWindowEvent layoutWindowEvent) {
			effect("activated", layoutWindowEvent);
		}

		public void layoutWindowChanged(LayoutWindowEvent layoutWindowEvent) {
			effect("changed", layoutWindowEvent);
		}

		public void layoutWindowSaveNeeded(LayoutWindowEvent layoutWindowEvent) {
			effect("save request", layoutWindowEvent);
		}

		public void layoutWindowComponentAdded(
				LayoutWindowEvent layoutWindowEvent) {
		}

		public void layoutWindowComponentRemoved(
				LayoutWindowEvent layoutWindowEvent) {
		}
	}

	static int dock;
	static int doc;

	static DockableWindow dockwin(String title, String message) {
		DockableWindow w = manager.createDockableWindow(
				"dockable" + String.valueOf(++dock), title, icon2);
		w.addComponent(new TestComponent(message), "");
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	static DocumentWindow docwin(String title, String tabName) {
		DocumentWindow w = manager.createDocumentWindow(
				"document" + String.valueOf(++doc), title, icon);
		w.setTabName(tabName);
		// 1 manager.openLayoutWindow(w);
		return w;
	}

	public static void main(String[] args) {

		manager.addLayoutWindowListener(new Listener());
		DockableWindow dock;
		DocumentWindow doc;

		setup("Test 08");

		space();
		doc = docwin("Matrix1", "The Matrix");
		manager.showLayoutWindow(doc);

		space();
		dock = dockwin("Trinity", "This is Trinity");
		dock.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(dock);

		space();
		dock = dockwin("Neo", "This is Neo");
		dock.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(dock);

		space();
		dock = dockwin("Morpheus", "This is Morpheus");
		dock.setInitialDockSide(DockableWindow.DOCK_SIDE_RIGHT);
		manager.showLayoutWindow(dock);

		space();
		doc = docwin("Matrix2", "Zion");
		manager.showLayoutWindow(doc);

		space();
		dock = dockwin("Zypher", "This is Zypher");
		dock.setInitialDockSide(DockableWindow.DOCK_SIDE_LEFT);
		manager.showLayoutWindow(dock);

		space();
		dock = dockwin("???", "The Woman in red");
		dock.setInitialDockSide(DockableWindow.DOCK_SIDE_BOTTOM);
		manager.showLayoutWindow(dock);

		/*
		 * System.out.println("FLOAT!"); space();
		 * manager.activateLayoutWindow(manager
		 * .findDockableWindow("dockable1")); space();
		 * manager.activateLayoutWindow
		 * (manager.findDockableWindow("dockable2")); space();
		 * manager.activateLayoutWindow
		 * (manager.findDockableWindow("dockable3"));
		 */

		space();
		manager.hideLayoutWindow(manager.findDockableWindow("dockable4"));
		space();
		manager.hideLayoutWindow(manager.findDockableWindow("dockable5"));
		space();
		manager.closeLayoutWindow(manager.findDockableWindow("dockable5"));
		System.out
				.println("dockable5 should be closed now and may not be shown again");
		System.out
				.println("the next test tries to open dockable5 again - an exception should occur");
		space();
		try {
			manager.showLayoutWindow(manager.findDockableWindow("dockable5"));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		space();
		manager.hideLayoutWindow(manager.findDocumentWindow("document2"));
		space();
		manager.closeLayoutWindow(manager.findDocumentWindow("document2"));
		System.out
				.println("document2 should be closed now and may not be shown again");
		System.out
				.println("the next test tries to open document2 again - an exception should occur");
		space();
		try {
			manager.showLayoutWindow(manager.findDocumentWindow("document2"));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		space();
		manager.showLayoutWindow(manager.findDockableWindow("dockable4"));
		space();
		manager.activateLayoutWindow(manager.findDockableWindow("dockable1"));
		space();
		manager.activateLayoutWindow(manager.findDockableWindow("dockable2"));
		space();
		manager.hideAllDocumentWindows();
		space();
		manager.closeAllDocumentWindows();
		System.out.println("all document windows should be closed now");
		space();
		manager.hideAllDockableWindows();
		space();
		manager.closeAllDockableWindows();
		System.out.println("all dockable windows should be closed now");
		space();
		end();
	}
}
