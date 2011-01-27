package com.nayaware.jdockers.test;

import com.nayaware.jdockers.LayoutWindow;

/**
 * This test demonstrates changing document window properties (title and icon).
 */
public class Test03 extends TestBase {

	public static void main(String[] args) {
		LayoutWindow w;
		w = manager.createDocumentWindow("doc1", "Old Title", icon);
		// 1 manager.openLayoutWindow(w);
		manager.showLayoutWindow(w);

		setup("Test 03");

		space();
		w.setTitle("New Title");
		space();
		w.setIcon(icon2);
		w.addComponent(new TestComponent("Look! A new icon!"), "");
		space();
		end();
	}
}
