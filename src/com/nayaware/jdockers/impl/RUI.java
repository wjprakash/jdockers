
package com.nayaware.jdockers.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import com.nayaware.jdockers.custom.XPStyle;
import com.nayaware.jdockers.custom.XPStyle.Skin;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

/**
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */

public class RUI {

	public static class WindowsBorderlessTabbedPaneUI extends
			WindowsTabbedPaneUI {

		class MouseHandler extends BasicTabbedPaneUI.MouseHandler implements
				MouseMotionListener {
			private boolean suck;

			private void delegate(MouseEvent e) {
				if (!suck) {
					suck = true;
					((Component) e.getSource()).addMouseMotionListener(this);
				}
				if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
					tabPane.dispatchEvent(SwingUtilities.convertMouseEvent(
							(Component) e.getSource(), e, tabPane));
				}
			}

			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (!e.isConsumed()) {
					delegate(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				delegate(e);
			}

			public void mouseClicked(MouseEvent e) {
				delegate(e);
			}

			public void mouseEntered(MouseEvent e) {
				delegate(e);
			}

			public void mouseExited(MouseEvent e) {
				delegate(e);
			}

			public void mouseDragged(MouseEvent e) {
				delegate(e);
			}

			public void mouseMoved(MouseEvent e) {
				delegate(e);
			}
		}

		/*
		 * Overwritten to use a special MouseListener that fixes the problem
		 * that a JTabbedPane in SCROLL_TAB_LAYOUT doesn't receive MouseEvents
		 * and therefore cannot do Drag&Drop.
		 */
		protected MouseListener createMouseListener() {
			return new MouseHandler();
		}

		/*
		 * Overridden to remove the border insets of the unpainted content
		 * borders.
		 */
		protected void installDefaults() {
			super.installDefaults();
			contentBorderInsets = new Insets(0, 0, 0, 0);
		}

		/*
		 * Overridden to avoid painting content borders.
		 */
		protected void paintContentBorder(Graphics g, int tabPlacement,
				int selectedIndex) {
			int width = tabPane.getWidth();
			int height = tabPane.getHeight();
			Insets insets = tabPane.getInsets();

			int x = insets.left;
			int y = insets.top;
			int w = width - insets.right - insets.left;
			int h = height - insets.top - insets.bottom;

			switch (tabPlacement) {
			case LEFT:
				x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
				w -= (x - insets.left);
				break;
			case RIGHT:
				w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
				break;
			case BOTTOM:
				h -= calculateTabAreaHeight(tabPlacement, runCount,
						maxTabHeight);
				break;
			case TOP:
			default:
				y += calculateTabAreaHeight(tabPlacement, runCount,
						maxTabHeight);
				h -= (y - insets.top);
			}

			// Fill region behind content area

			// if (tabPane.getTabCount() == 0) {
			// return;
			// }

			XPStyle xp = XPStyle.getXP();
			if (xp != null) {
				xp.getSkin("tab.pane").paintSkin(g, x, y - 1, w, h + 1, 0);
			} else {
				g.setColor(tabPane.getBackground());
				g.setColor(Color.red);
				g.fillRect(x, y, w, h);
			}
		}

		protected void paintTabBackground(Graphics g, int tabPlacement,
				int tabIndex, int x, int y, int w, int h, boolean isSelected) {
			if (XPStyle.getXP() == null) {
				super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h,
						isSelected);
			} else {
				/*
				 * g.setColor( !isSelected ? tabPane.getBackgroundAt(tabIndex) :
				 * XPStyle.getXP().getColor("tab.pane.fillcolorhint", null));
				 */
				Skin skin = XPStyle.getXP().getSkin("tab.pane");
				switch (tabPlacement) {
				case LEFT:
					skin.paintSkin(g, x + 1, y + 1, w - 2, h - 3, 0);
					break;
				case RIGHT:
					skin.paintSkin(g, x, y + 1, w - 2, h - 3, 0);
					break;
				case BOTTOM:
					skin.paintSkin(g, x + 1, y, w - 3, h - 1, 0);
					break;
				case TOP:
				default:
					skin.paintSkin(g, x + 1, y + 1, w - 3, h - 1, 0);
				}
			}
		}

		protected void paintTabBorder(Graphics g, int tabPlacement,
				int tabIndex, int x, int y, int w, int h, boolean isSelected) {
			XPStyle xp = XPStyle.getXP();
			if (xp != null) {

				Skin skin = xp.getSkin("tab.tabitem");

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

	public static class MetalBorderlessTabbedPaneUI extends MetalTabbedPaneUI {

		/**
		 * Overridden to avoid insets
		 */
		protected void installDefaults() {
			super.installDefaults();
			contentBorderInsets = new Insets(0, 0, 0, 0);
		}

		/**
		 * Overridden to avoid painting TabbedPane bottom borders
		 */
		protected void paintContentBorderBottomEdge(Graphics g,
				int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
		}

		/**
		 * Overridden to avoid painting TabbedPane left borders
		 */
		protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
				int selectedIndex, int x, int y, int w, int h) {
		}

		/**
		 * Overridden to avoid painting TabbedPane right borders
		 */
		protected void paintContentBorderRightEdge(Graphics g,
				int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
		}

		/**
		 * Overridden to avoid painting TabbedPane top borders
		 */
		protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
				int selectedIndex, int x, int y, int w, int h) {
		}
	}

	public static class MetalSlantlessTabbedPaneUI extends
			MetalBorderlessTabbedPaneUI {

		/**
		 * Paints tab without its default slant
		 */
		protected void paintTabBorder(Graphics g, int tabPlacement,
				int tabIndex, int x, int y, int w, int h, boolean isSelected) {
			int bottom = y + (h - 1);
			int right = x + (w - 1);

			switch (tabPlacement) {
			case LEFT:
				paintLeftTabBorder(tabIndex, g, x, y, w, h, bottom, right,
						isSelected);
				break;
			case BOTTOM:
				paintBottomTabBorder(tabIndex, g, x, y, w, h, bottom, right,
						isSelected);
				break;
			case RIGHT:
				paintRightTabBorder(tabIndex, g, x, y, w, h, bottom, right,
						isSelected);
				break;
			case TOP:
			default:
				paintTopTabBorder(tabIndex, g, x, y, w, h, bottom, right,
						isSelected);
			}
		}

		protected void paintTopTabBorder(int tabIndex, Graphics g, int x,
				int y, int w, int h, int btm, int rght, boolean isSelected) {
			int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
			int lastIndex = lastTabInRun(tabPane.getTabCount(), currentRun);
			int firstIndex = tabRuns[currentRun];
			int bottom = h - 1;
			int right = w - 1;

			//
			// Paint Gap
			//

			if (shouldFillGap(currentRun, tabIndex, x, y)) {
				g.translate(x, y);

				g.setColor(getColorForGap(currentRun, x, y + 1));
				g.fillRect(1, 0, 5, 3);
				g.fillRect(1, 3, 2, 2);

				g.translate(-x, -y);
			}

			g.translate(x, y);

			//
			// Paint Border
			//

			g.setColor(darkShadow);

			// Paint top
			g.drawLine(0, 0, right + 1, 0);

			// Paint right
			g.drawLine(right + 1, 0, right + 1, bottom);

			// Paint left
			g.drawLine(0, 1, 0, bottom);

			//
			// Paint Highlight
			//

			g.setColor(isSelected ? selectHighlight : highlight);

			// Paint top
			g.drawLine(1, 1, right, 1);

			// Paint left
			g.drawLine(1, 1, 1, bottom);

			g.translate(-x, -y);
		}

		protected void paintLeftTabBorder(int tabIndex, Graphics g, int x,
				int y, int w, int h, int btm, int rght, boolean isSelected) {
			int tabCount = tabPane.getTabCount();
			int currentRun = getRunForTab(tabCount, tabIndex);
			int lastIndex = lastTabInRun(tabCount, currentRun);
			int firstIndex = tabRuns[currentRun];

			g.translate(x, y);

			int bottom = h - 1;
			int right = w - 1;

			//
			// Paint Highlight
			//

			g.setColor(isSelected ? selectHighlight : highlight);

			// Paint top
			g.drawLine(1, 1, right, 1);

			// Paint left
			g.drawLine(1, 1, 1, bottom);

			//
			// Paint Border
			//

			g.setColor(darkShadow);

			// Paint top
			g.drawLine(0, 0, right + 1, 0);

			// Paint left
			g.drawLine(0, 0, 0, bottom);

			// Paint bottom
			if (tabIndex == lastIndex) {
				g.drawLine(0, bottom, right + 1, bottom);
			}

			g.translate(-x, -y);
		}

		protected void paintBottomTabBorder(int tabIndex, Graphics g, int x,
				int y, int w, int h, int btm, int rght, boolean isSelected) {
			int tabCount = tabPane.getTabCount();
			int currentRun = getRunForTab(tabCount, tabIndex);
			int lastIndex = lastTabInRun(tabCount, currentRun);
			int firstIndex = tabRuns[currentRun];

			int bottom = h - 1;
			int right = w - 1;

			//
			// Paint Gap
			//

			if (shouldFillGap(currentRun, tabIndex, x, y)) {
				g.translate(x, y);

				g.setColor(getColorForGap(currentRun, x, y));
				g.fillRect(1, bottom - 4, 3, 5);
				g.fillRect(4, bottom - 1, 2, 2);

				g.translate(-x, -y);
			}

			g.translate(x, y);

			//
			// Paint Border
			//

			g.setColor(darkShadow);

			// Paint bottom
			g.drawLine(0, bottom, right + 1, bottom);

			// Paint right
			g.drawLine(right + 1, 0, right + 1, bottom);

			// Paint left
			g.drawLine(0, 0, 0, bottom);

			//
			// Paint Highlight
			//

			g.setColor(isSelected ? selectHighlight : highlight);

			// Paint left
			g.drawLine(1, 0, 1, bottom - 1);

			// Paint bottom
			g.drawLine(1, bottom - 1, right, bottom - 1);

			g.translate(-x, -y);
		}

		protected void paintRightTabBorder(int tabIndex, Graphics g, int x,
				int y, int w, int h, int btm, int rght, boolean isSelected) {
			int tabCount = tabPane.getTabCount();
			int currentRun = getRunForTab(tabCount, tabIndex);
			int lastIndex = lastTabInRun(tabCount, currentRun);
			int firstIndex = tabRuns[currentRun];

			g.translate(x, y);

			int bottom = h - 1;
			int right = w - 1;

			//
			// Paint Highlight
			//

			g.setColor(isSelected ? selectHighlight : highlight);

			// Paint top
			g.drawLine(0, 1, right, 1);

			// Paint right
			g.drawLine(right, 1, right, bottom);

			//
			// Paint Border
			//

			g.setColor(darkShadow);

			// Paint top
			g.drawLine(0, 0, right + 1, 0);

			// Paint right
			g.drawLine(right + 1, 0, right + 1, bottom);

			// Paint bottom
			if (tabIndex == lastIndex) {
				g.drawLine(0, bottom, right + 1, bottom);
			}

			g.translate(-x, -y);
		}

		/**
		 * Paints FocusIndicator to fit in the new slantless tabs
		 */
		protected void paintFocusIndicator(Graphics g, int tabPlacement,
				Rectangle[] rects, int tabIndex, Rectangle iconRect,
				Rectangle textRect, boolean isSelected) {
			if (tabPane.hasFocus() && isSelected) {
				Rectangle tabRect = rects[tabIndex];
				g.setColor(focus);
				g.translate(tabRect.x, tabRect.y);
				int right = tabRect.width - 1;
				int bottom = tabRect.height - 1;
				switch (tabPlacement) {
				case RIGHT:
					g.drawLine(1, 2, right - 1, 2); // top
					g.drawLine(right - 1, 2, right - 1, bottom - 1);
					// right
					g.drawLine(1, 2, 1, bottom - 1); // left
					g.drawLine(1, bottom - 1, right - 2, bottom - 1);
					// bottom
					break;
				case BOTTOM:
					g.drawLine(2, bottom - 2, right, bottom - 2);
					// bottom
					g.drawLine(2, 0, 2, bottom - 2); // left
					g.drawLine(2, 0, right, 0); // top
					g.drawLine(right, 0, right, bottom - 2); // right
					break;
				case LEFT:
					g.drawLine(2, 2, 2, bottom - 1); // left
					g.drawLine(2, 2, right, 2); // top
					g.drawLine(right, 2, right, bottom - 1); // right
					g.drawLine(2, bottom - 1, right, bottom - 1);
					// bottom
					break;
				case TOP:
				default:
					g.drawLine(2, 2, 2, bottom - 1); // left
					g.drawLine(2, 2, right, 2); // top
					g.drawLine(right, 2, right, bottom - 1); // right
					g.drawLine(2, bottom - 1, right, bottom - 1);
					// bottom
				}
				g.translate(-tabRect.x, -tabRect.y);
			}
		}

		/**
		 * Paints TabBackgroud to fit in the new slantless tabs
		 */
		protected void paintTabBackground(Graphics g, int tabPlacement,
				int tabIndex, int x, int y, int w, int h, boolean isSelected) {
			if (isSelected) {
				g.setColor(selectColor);
			} else {
				g.setColor(tabPane.getBackgroundAt(tabIndex));
			}

			switch (tabPlacement) {
			case LEFT:
				g.fillRect(x + 2, y + 1, w - 2, h - 1);
				break;
			case BOTTOM:
				g.fillRect(x + 2, y, w - 2, h - 2);
				break;
			case RIGHT:
				g.fillRect(x + 1, y + 1, w - 2, h - 1);
				break;
			case TOP:
			default:
				g.fillRect(x + 2, y + 2, (w - 1) - 2, (h - 1) - 1);
			}
		}

	}
}
