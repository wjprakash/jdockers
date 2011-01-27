package com.nayaware.jdockers.impl;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * This class references a bunch of icons used by the window panes.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RSwing {

	// Motif has special (uglier ;-) icons. All other looks use the XP style
	// look.
	static String prefix = isMotif() ? "motif-" : "";

	// default icon for Autohide Items
	static ImageIcon pane = new ImageIcon(RSwing.class.getResource("pane.png"));

	// close button, normal and pressed (an X)
	static ImageIcon close0 = new ImageIcon(RSwing.class.getResource(prefix
			+ "close0.png"));
	static ImageIcon close1 = new ImageIcon(RSwing.class.getResource(prefix
			+ "close1.png"));

	// pin/unpin button, normal and pressed (a pin needle)
	static ImageIcon pin0 = new ImageIcon(RSwing.class.getResource(prefix
			+ "pin0.png"));
	static ImageIcon pin1 = new ImageIcon(RSwing.class.getResource(prefix
			+ "pin1.png"));
	static ImageIcon pin2 = new ImageIcon(RSwing.class.getResource(prefix
			+ "pin2.png"));
	static ImageIcon pin3 = new ImageIcon(RSwing.class.getResource(prefix
			+ "pin3.png"));

	// float button (there's no unfloat button), normal and pressed (two frames)
	static ImageIcon float0 = new ImageIcon(RSwing.class.getResource(prefix
			+ "float0.png"));
	static ImageIcon float1 = new ImageIcon(RSwing.class.getResource(prefix
			+ "float1.png"));

	// blue titlebar background - used only on windows
	static Image title0 = new ImageIcon(RSwing.class.getResource("title0.png"))
			.getImage();
	static Image title1 = new ImageIcon(RSwing.class.getResource("title1.png"))
			.getImage();

	public static boolean isMotif() {
		return "Motif".equals(UIManager.getLookAndFeel().getID());
	}

	public static boolean isGTK() {
		return "GTK".equals(UIManager.getLookAndFeel().getID());
	}

	public static boolean isWindows() {
		return "Windows".equals(UIManager.getLookAndFeel().getID());
	}
}
