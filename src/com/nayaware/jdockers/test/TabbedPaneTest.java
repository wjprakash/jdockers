package com.nayaware.jdockers.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.nayaware.jdockers.custom.JClosableTabbedPane;

public class TabbedPaneTest {

	static {
		System.setProperty("swing.gtkthemefile",
				"C:/temp/Bumblebee/gtk-2.0/gtkrc");

		try {
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	static ImageIcon icon = new ImageIcon(
			TabbedPaneTest.class.getResource("icon.png"));

	public static void main(String[] args) {
		final JFrame f = new JFrame("TabbedPane Test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JClosableTabbedPane t = new JClosableTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		for (int i = 1; i < 5; i++) {
			t.addTab("Tab No " + i, new JLabel("Component " + i));
			t.setIconAt(i - 1, icon);
		}
		t.setEnabledAt(1, false);
		t.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				t.removeTabAt(e.getID());
			}
		});
		/*
		 * t.addMouseListener(new MouseListener() { private void e(String s,
		 * MouseEvent e) { System.out.println("Mouse " + s + " at " + e.getX() +
		 * "x" + e.getY()); } public void mouseClicked(MouseEvent e) {
		 * e("click", e); } public void mouseEntered(MouseEvent e) {
		 * //e("enter", e); } public void mouseExited(MouseEvent e) { //e("exit"
		 * , e); } public void mousePressed(MouseEvent e) { e("pressed", e); }
		 * public void mouseReleased(MouseEvent e) { e("released", e); } });
		 * t.addMouseMotionListener(new MouseMotionListener() { public void
		 * mouseDragged(MouseEvent e) { System.out.println("MD"); } public void
		 * mouseMoved(MouseEvent e) { System.out.println("MM"); } });
		 */
		f.getContentPane().add(t);
		f.getContentPane().add(new JButton("A Button") {
			int i = 0;

			protected void fireActionPerformed(ActionEvent event) {
				String[] s = { "javax.swing.plaf.metal.MetalLookAndFeel",
						"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
						"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
						"com.sun.java.swing.plaf.gtk.GTKLookAndFeel" };
				try {
					i = (i + 1) % s.length;
					UIManager.setLookAndFeel(s[i]);
					SwingUtilities.updateComponentTreeUI(f);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				t.setClosable(!t.isCloasable());
			}
		}, BorderLayout.SOUTH);
		f.setSize(200, 200);
		f.show();
	}
}
