package com.nayaware.jdockers.test;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.impl.RLayoutManager;

public abstract class TestBase {

	static {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	static LayoutManager manager = new RLayoutManager();

	static ImageIcon icon = new ImageIcon(
			WindowTests.class.getResource("icon.png"));
	static ImageIcon icon2 = new ImageIcon(
			WindowTests.class.getResource("icon2.png"));

	static class TestComponent extends JLabel {

		public TestComponent(String text) {
			super(text);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			setOpaque(true);
			setBorder(new BevelBorder(BevelBorder.RAISED));
			setBackground(new Color((float) Math.random() * 0.8f + 0.2f,
					(float) Math.random() * 0.6f + 0.4f, 0.6f));
		}
	}

	static JFrame f;

	static void setup(String name) {
		f = new JFrame(name);
		f.setIconImage(new ImageIcon(TestBase.class.getResource("frame.png"))
				.getImage());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(manager.getLayoutPane());
		f.setSize(320, 200);
		f.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				synchronized (f) {
					f.notify();
				}
			}
		});
		f.show();
		f.requestFocus();
	}

	static void space() {
		try {
			synchronized (f) {
				f.wait();
			}
		} catch (InterruptedException e) {
		}
	}

	static void end() {
		f.dispose();
	}
}
