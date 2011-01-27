package com.nayaware.jdockers.custom;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/*
 * This is a reformatted copy of <code>com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI</code>
 * from JDK 1.4.2 with the following changes:
 * 
 * createUI - return an instance of the new class
 * Replaced XPStyle with a new public accessible implementation 
 * 
 * TODO: add mouse over effect for tabs on XP
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class WindowsClosableTabbedPaneUI extends BasicClosableTabbedPaneUI {

	public static ComponentUI createUI(JComponent c) {
		return new WindowsClosableTabbedPaneUI();// sma
	}

	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		if (XPStyle.getXP() == null) {
			super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h,
					isSelected);
		}
	}

	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
			int x, int y, int w, int h, boolean isSelected) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			XPStyle.Skin skin = xp.getSkin("tab.tabitem");

			int rotate = 0;
			int tx = 0;
			int ty = 0;

			switch (tabPlacement) {
			case RIGHT:
				rotate = 90;
				tx = w;
				break;
			case BOTTOM:
				rotate = 180;
				tx = w;
				ty = h;
				break;
			case LEFT:
				rotate = 270;
				ty = h;
				break;
			}

			g.translate(x + tx, y + ty);
			if (rotate != 0 && (g instanceof Graphics2D)) {
				((Graphics2D) g).rotate(Math.toRadians((double) rotate));
			}
			if (rotate == 90 || rotate == 270) {
				skin.paintSkin(g, 0, 0, h, w, isSelected ? 2 : 0);
			} else {
				skin.paintSkin(g, 0, 0, w, h, isSelected ? 2 : 0);
			}
			if (rotate != 0 && (g instanceof Graphics2D)) {
				((Graphics2D) g).rotate(-Math.toRadians((double) rotate));
			}
			g.translate(-x - tx, -y - ty);
		} else {
			super.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h,
					isSelected);
		}
	}
}
