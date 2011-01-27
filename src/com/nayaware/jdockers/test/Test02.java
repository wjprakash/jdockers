package com.nayaware.jdockers.test;

import com.nayaware.jdockers.LayoutWindow;

/**
 * This test demonstrates adding and removing Document Windows to the Document
 * Container. You can also close the Document Tabs using the mouse or pressing
 * Ctrl+F4.
 */
public class Test02 extends TestBase {

	public static void main(String[] args) {
		LayoutWindow w;
		w = manager.createDocumentWindow("doc1", "Matrix1", icon);
		w.addComponent(new TestComponent("This is Neo"), "Neo");
		// 1 manager.openLayoutWindow(w);

		w = manager.createDocumentWindow("doc2", "Matrix2", icon);
		w.addComponent(new TestComponent("This is Trinity"), "Trinity");
		// 1 manager.openLayoutWindow(w);

		w = manager.createDocumentWindow("doc3", "Matrix3", icon);
		w.addComponent(new TestComponent("This is Morpheus"), "Morpheus");
		// 1 manager.openLayoutWindow(w);

		setup("Test 02");

		space();
		manager.showLayoutWindow(manager.findLayoutWindow("doc1"));
		space();
		manager.showLayoutWindow(manager.findLayoutWindow("doc2"));
		space();
		manager.showLayoutWindow(w);
		space();
		manager.hideLayoutWindow(w);
		space();
		manager.hideAllDocumentWindows();
		space();
		manager.showWindowSet(WindowTests.set("doc1,doc2,doc3"));
		space();
		end();
	}
}
