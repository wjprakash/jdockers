package com.nayaware.jdockers.custom;

import java.awt.Graphics;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simulated XP Style window decoration
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class XPStyle {

	private static boolean initialized;
	private static Class xpStyleClass;
	private static XPStyle instance;

	public static XPStyle getXP() {
		if (!initialized) {
			initialized = true;
			// try once to load the XPStyle class which is available in JDK
			// 1.4.2 and provide
			// public access to its static getStyle() method, call it and
			// provide a wrapped
			// XPStyle object. If this isn't possible, fail silently. The user
			// of class will
			// hopefully think then that is is a pre-XP Windows.
			try {
				xpStyleClass = Class
						.forName("com.sun.java.swing.plaf.windows.XPStyle");

				Method getXPMethod = xpStyleClass.getDeclaredMethod("getXP",
						null);
				getXPMethod.setAccessible(true);
				Object xpStyle = getXPMethod.invoke(null, null);

				if (xpStyle != null) {
					instance = new XPStyle(xpStyle);
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return instance;
	}

	private Object style;
	private Method getSkinMethod;
	private Method paintSkin2Method;
	private Method paintSkin4Method;

	private XPStyle(Object style) throws Exception {
		// we found the class and got the XPStyle object. Now make some more
		// methods accessible.
		this.style = style;
		getSkinMethod = xpStyleClass.getDeclaredMethod("getSkin",
				new Class[] { String.class });
		getSkinMethod.setAccessible(true);
		Class skinClass = Class
				.forName("com.sun.java.swing.plaf.windows.XPStyle$Skin");
		paintSkin2Method = skinClass.getDeclaredMethod("paintSkin",
				new Class[] { Graphics.class, Integer.TYPE, Integer.TYPE,
						Integer.TYPE });
		paintSkin2Method.setAccessible(true);
		paintSkin4Method = skinClass.getDeclaredMethod("paintSkin",
				new Class[] { Graphics.class, Integer.TYPE, Integer.TYPE,
						Integer.TYPE, Integer.TYPE, Integer.TYPE });
		paintSkin4Method.setAccessible(true);
	}

	public Skin getSkin(String category) {
		try {
			return new Skin(getSkinMethod.invoke(style,
					new Object[] { category }));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public class Skin {

		private Object skin;

		private Skin(Object skin) {
			this.skin = skin;
		}

		public void paintSkin(Graphics g, int dx, int dy, int index) {
			try {
				paintSkin4Method.invoke(skin, new Object[] { g,
						new Integer(dx), new Integer(dy), new Integer(index) });
			} catch (Exception e) {
				throw new Error(e);
			}
		}

		public void paintSkin(Graphics g, int dx, int dy, int dw, int dh,
				int index) {
			try {
				paintSkin4Method.invoke(skin, new Object[] { g,
						new Integer(dx), new Integer(dy), new Integer(dw),
						new Integer(dh), new Integer(index) });
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	/**
	 * In case you are as curious as I am and want to know what catagories are
	 * available, call this method...
	 */
	void listCategories() throws Exception {
		Field mapField = xpStyleClass.getDeclaredField("map");
		mapField.setAccessible(true);
		Map map = (Map) mapField.get(style);
		List keys = new ArrayList(map.keySet());
		Collections.sort(keys);
		for (Iterator i = keys.iterator(); i.hasNext();) {
			Object key = i.next();
			System.out.print(key);
			System.out.print(" = ");
			System.out.println(map.get(key));
		}
	}

	public static void main(String[] args) throws Exception {
		getXP().listCategories();
	}
}
