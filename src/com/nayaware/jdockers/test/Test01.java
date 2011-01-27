package com.nayaware.jdockers.test;

import com.nayaware.jdockers.LayoutWindow;

/**
 * This test demonstrates adding and removing of components to a Document
 * Window. (Should also work for Dockable Windows, see Test04)
 */
public class Test01 extends TestBase {

	public static void main(String[] args) {
		LayoutWindow w = manager.createDocumentWindow("doc1", "Matrix1", icon);
		w.addComponent(new TestComponent("This is Neo"), "Neo");
		// 1 manager.openLayoutWindow(w);
		manager.showLayoutWindow(w);

		setup("Test 01");

		space();
		w.addComponent(new TestComponent("This is Trinity"), "Trinity");
		space();
		w.addComponent(new TestComponent("This is Morpheus"), "Morpheus");
		space();
		w.removeComponent("Neo");
		space();
		w.removeAllComponents();
		space();
		end();
	}
}
