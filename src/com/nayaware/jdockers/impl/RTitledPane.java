package com.nayaware.jdockers.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * An abstract class providing a title bar for other panes. Titled Panes can be
 * hidden, auto-hidden, floated and docked.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public abstract class RTitledPane extends RWindowPane {

	private static boolean xpStyle = RSwing.isWindows();

	/**
	 * Flag whether the the title bar shall be displayed in a way denoting the
	 * active pane
	 */
	private boolean active;

	/**
	 * Reference to the title bar panel
	 */
	JPanel caption = new JPanel(new GridBagLayout()) {
		protected void paintComponent(Graphics g) {
			if (xpStyle) {
				g.drawImage(active ? RSwing.title0 : RSwing.title1, 0, 0,
						getWidth(), getHeight(), 0, 0, 30, 30, this);
				return;
			}
			Color gradient;
			if ((gradient = UIManager
					.getColor(active ? "InternalFrame.activeTitleGradient"
							: "InternalFrame.inactiveTitleGradient")) != null) {
				int x1, x2, y2;
				if (RSwing.isGTK()) {
					x1 = title.getPreferredSize().width;
					x2 = x1 + 30;
					y2 = getHeight() / 2;
				} else {
					x1 = 0;
					x2 = getWidth();
					y2 = 0;
				}
				((Graphics2D) g).setPaint(new GradientPaint(x1, 0,
						getBackground(), x2, y2, gradient));
				g.fillRect(0, 0, getWidth(), getHeight());
			} else {
				super.paintComponent(g);
			}
		}
	};

	/**
	 * Reference to the label that displays the caption text
	 */
	JLabel title = new JLabel();

	/**
	 * Reference to the "hide" action button
	 */
	JButton close = new JButton();

	/**
	 * Reference to the "auto-hide" action button
	 */
	JButton pin = new JButton();

	/**
	 * Reference to the "float" action button
	 */
	JButton floatb = new JButton();

	Action hideAction = new AbstractAction("Hide") {
		public void actionPerformed(ActionEvent e) {
			hideRequest();
		}
	};

	Action floatAction = new AbstractAction("Floating") {
		public void actionPerformed(ActionEvent e) {
			floatRequest();
		}
	};

	Action autoHideAction = new AbstractAction("Auto Hide") {
		public void actionPerformed(ActionEvent e) {
			pinRequest();
		}
	};

	RTitledPane() {
		// this is really strange but required because otherwise the global
		// event listener
		// will never get mouse events for this component.
		addMouseListener(new MouseAdapter() {
		});
	}

	/**
	 * Creates a new title bar panel with a title and three buttons "float",
	 * "pin" and "close" and a context menu with Hide, Floating and Auto Hide
	 * actions.
	 */
	JPanel createTitleBar() {
		return createTitleBar(true);
	}

	JPanel createTitleBar(boolean normal) {
		caption.setName("caption");
		caption.setBackground(UIManager
				.getColor("InternalFrame.inactiveTitleBackground"));

		title.setName("title");
		title.setOpaque(false);
		title.setForeground(getInactiveCaptionText());
		title.setMinimumSize(new Dimension());

		close.setName("close");
		close.setOpaque(false);
		close.setBorder(BorderFactory.createEmptyBorder());
		close.setIcon(RSwing.close0);
		close.setPressedIcon(RSwing.close1);
		close.addActionListener(hideAction);
		close.setFocusable(false);

		if (normal) {
			pin.setName("pin");
			pin.setOpaque(false);
			pin.setBorder(BorderFactory.createEmptyBorder());
			pin.setIcon(RSwing.pin0);
			pin.setPressedIcon(RSwing.pin3);
			pin.addActionListener(autoHideAction);
			pin.setFocusable(false);
		}
		floatb.setName("float");
		floatb.setOpaque(false);
		floatb.setBorder(BorderFactory.createEmptyBorder());
		floatb.setIcon(RSwing.float0);
		floatb.setPressedIcon(RSwing.float1);
		floatb.addActionListener(floatAction);
		floatb.setFocusable(false);

		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(1, 2, 1, 2);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		caption.add(title, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
		caption.add(floatb, gbc);
		gbc.gridx++;
		if (normal) {
			caption.add(pin, gbc);
			gbc.gridx++;
		}
		caption.add(close, gbc);

		final JPopupMenu menu = new JPopupMenu();
		menu.add(hideAction);
		menu.add(floatAction);
		if (normal) {
			menu.add(autoHideAction);
		}

		MouseListener l = new DelegatingMouseAdapter(this) {
			public void mousePressed(MouseEvent e) {
				activateRequest();
				if (e.getClickCount() == 2) {
					floatRequest();
				}
				super.mousePressed(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				} else {
					super.mouseReleased(e);
				}
			}
		};
		caption.addMouseListener(l);
		title.addMouseListener(l);

		dnd.addSource(caption);
		dnd.addSource(title);

		return caption;
	}

	public boolean isActive() {
		return active;
	}

	void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;
			caption.setBackground(UIManager
					.getColor(active ? "InternalFrame.activeTitleBackground"
							: "InternalFrame.inactiveTitleBackground"));
			title.setForeground(active ? getActiveCaptionText()
					: getInactiveCaptionText());
		}
	}

	Color getInactiveCaptionText() {
		return xpStyle ? Color.darkGray : UIManager
				.getColor("InternalFrame.inactiveTitleForeground");
	}

	Color getActiveCaptionText() {
		return xpStyle ? Color.white : UIManager
				.getColor("InternalFrame.activeTitleForeground");
	}

	String getTitle() {
		return title.getText();
	}

	void setTitle(String text) {
		title.setText(text);
	}

	/**
	 * Invoked if the title bar is clicked.
	 */
	void activateRequest() {
	}

	/**
	 * Invoked if close button is clicked or "Hide" action is chosen.
	 */
	void hideRequest() {
	}

	/**
	 * Invoked if title bar is double-clicked or "Floating" action is chosen.
	 */
	void floatRequest() {
	}

	/**
	 * Invoked if pin button is clicked or "Auto Hide" or "Dock" action is
	 * chosen.
	 */
	void pinRequest() {
	}

	/* Required for DnDPane */
	protected int getTitleHeight() {
		return caption.getHeight();
	}

	/*
	 * This MouseListener can be added to any component and all mouse events are
	 * automatically delegated to the specified "delegatee" component instead.
	 * This deals with the fact that in AWT/Swing only the topmost component
	 * with a listener gets events.
	 */
	static class DelegatingMouseAdapter implements MouseListener {
		private Component delegatee;

		public DelegatingMouseAdapter(Component delegatee) {
			this.delegatee = delegatee;
		}

		private void delegate(MouseEvent e) {
			delegatee.dispatchEvent(SwingUtilities.convertMouseEvent(
					e.getComponent(), e, delegatee));
		}

		public void mouseClicked(MouseEvent e) {
			delegate(e);
		}

		public void mousePressed(MouseEvent e) {
			delegate(e);
		}

		public void mouseReleased(MouseEvent e) {
			delegate(e);
		}

		public void mouseEntered(MouseEvent e) {
			delegate(e);
		}

		public void mouseExited(MouseEvent e) {
			delegate(e);
		}
	}

}