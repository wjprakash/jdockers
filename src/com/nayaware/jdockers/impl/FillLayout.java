package com.nayaware.jdockers.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * This LayoutManager knows how to arrange a number of components in one row or
 * column, respecting the preferred height resp. width and setting the other
 * dimension to the parent's width resp. height. For horizontal layout, it
 * respects the component's left to right orientation.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class FillLayout implements LayoutManager {

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	public int direction;
	public int gap;

	public FillLayout(int direction) {
		this.direction = direction;
	}

	public FillLayout(int direction, int gap) {
		this.direction = direction;
		this.gap = gap;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public void layoutContainer(Container parent) {
		int count = parent.getComponentCount();
		if (count == 0) {
			return;
		}
		Insets insets = parent.getInsets();
		int x = insets.left;
		int y = insets.top;
		int w = parent.getWidth() - x - insets.right;
		int h = parent.getHeight() - y - insets.bottom;
		boolean ltr = parent.getComponentOrientation().isLeftToRight();
		if (!ltr && direction == HORIZONTAL) {
			x = parent.getWidth() + gap - insets.right;
		}
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension d = c.getPreferredSize();
			if (direction == HORIZONTAL) {
				if (!ltr) {
					x -= d.width - gap;
				}
				c.setBounds(x, y, d.width, h);
				if (ltr) {
					x += d.width + gap;
				}
			} else {
				c.setBounds(x, y, w, d.height);
				y += d.height + gap;
			}
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		return layoutSize(parent, false);
	}

	public Dimension preferredLayoutSize(Container parent) {
		return layoutSize(parent, true);
	}

	protected Dimension layoutSize(Container parent, boolean preferredSize) {
		int w = 0, h = 0;
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			Dimension d = preferredSize ? c.getPreferredSize() : c
					.getMinimumSize();
			if (direction == HORIZONTAL) {
				w += d.width;
				h = Math.max(h, d.height);
			} else {
				w = Math.max(w, d.width);
				h += d.height;
			}
		}
		if (parent.getComponentCount() > 0) {
			int g = gap * (parent.getComponentCount() - 1);
			if (direction == HORIZONTAL) {
				w += g;
			} else {
				h += g;
			}
		}
		Insets insets = parent.getInsets();
		w += insets.left + insets.right;
		h += insets.top + insets.bottom;
		return new Dimension(w, h);
	}
}
