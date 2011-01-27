package com.nayaware.jdockers.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class can store and retrieve its instances in a nice XML format.
 * 
 * @author Stefan Matthias Aust
 * @version 1.0
 */
public class RMemento {

	private String type;

	private Map data = new HashMap();

	private List children = new ArrayList();

	/**
	 * Constructs a new memento instance usable to store double/int/string
	 * values and childs.
	 * 
	 * @param type
	 *            used as the XML element qname
	 */
	public RMemento(String type) {
		this.type = type;
	}

	public RMemento createMemento(String type) {
		RMemento memento = new RMemento(type);
		putMemento(memento);
		return memento;
	}

	// retrieving data
	// ---------------------------------------------------------------------------

	/**
	 * Returns the first child of the given type or <code>null</code> if no such
	 * child exists.
	 */
	public RMemento getChild(String type) {
		for (int i = 0; i < children.size(); i++) {
			RMemento m = (RMemento) children.get(i);
			if (m.type.equals(type)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all children of the given type.
	 */
	public List getChildren(String type) {
		List result = new ArrayList();
		for (int i = 0; i < children.size(); i++) {
			RMemento m = (RMemento) children.get(i);
			if (m.type.equals(type)) {
				result.add(m);
			}
		}
		return result;
	}

	/**
	 * Returns an attribute value as Double or <code>null</code> if the given
	 * key is unknown.
	 */
	public Double getDouble(String key) {
		String s = getString(key);
		return s == null ? null : new Double(s);
	}

	/**
	 * Returns an attribute value as Integer or <code>null</code> if the given
	 * key is unknown.
	 */
	public Integer getInteger(String key) {
		String s = getString(key);
		return s == null ? null : new Integer(s);
	}

	/**
	 * Returns an attribute value as String or <code>null</code> if the given
	 * key is unknown.
	 */
	public String getString(String key) {
		return (String) data.get(key);
	}

	// storing data
	// ------------------------------------------------------------------------------

	/**
	 * Sets the value of the given key to the given floating point value.
	 */
	public void putDouble(String key, double value) {
		putString(key, String.valueOf(value));
	}

	/**
	 * Sets the value of the given key to the given integer value.
	 */
	public void putInteger(String key, int value) {
		putString(key, String.valueOf(value));
	}

	/**
	 * Adds the given memento as new child to the receiver.
	 */
	public void putMemento(RMemento memento) {
		children.add(memento);
	}

	/**
	 * Sets the value of the given key to the given string.
	 */
	public void putString(String key, String value) {
		data.put(key, value);
	}

	// remove children
	// ---------------------------------------------------------------------------

	public void removeChild(RMemento memento) {
		children.remove(memento);
	}

	// file I/O
	// ----------------------------------------------------------------------------------

	/**
	 * Stores this memento with all children into the specified stream.
	 */
	public void write(PrintWriter w) {
		write(w, 0);
	}

	private void write(PrintWriter w, int level) {
		indent(w, level);
		w.print('<');
		w.print(type);
		for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
			Entry e = (Entry) i.next();
			w.print(' ');
			w.print(e.getKey());
			w.print('=');
			writeQuoted(w, e.getValue().toString());
		}
		if (children.isEmpty()) {
			w.print('/');
		}
		w.println('>');
		if (!children.isEmpty()) {
			for (Iterator i = children.iterator(); i.hasNext();) {
				((RMemento) i.next()).write(w, level + 1);
			}
			indent(w, level);
			w.print('<');
			w.print('/');
			w.print(type);
			w.println('>');
		}
	}

	private void indent(PrintWriter w, int level) {
		for (int i = 0; i < level; i++) {
			w.print('\t');
		}
	}

	private void writeQuoted(PrintWriter w, String s) {
		w.print('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"') {
				w.print("&quot;");
			} else if (c < 32 || c >= 127) {
				w.print("&#");
				w.print((int) c);
				w.print(';');
			} else {
				w.print(c);
			}
		}
		w.print('"');
	}

	/**
	 * Retrieves a memento with all children from the specified stream.
	 * 
	 * @throws IOException
	 */
	static RMemento read(Reader r) throws IOException {
		try {
			final RMemento[] result = new RMemento[1];
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			reader.setContentHandler(new DefaultHandler() {
				private LinkedList stack = new LinkedList();

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					RMemento m = new RMemento(qName);
					for (int i = 0; i < attributes.getLength(); i++) {
						m.putString(attributes.getQName(i),
								attributes.getValue(i));
					}
					if (stack.isEmpty()) {
						result[0] = m;
					} else {
						((RMemento) stack.getFirst()).putMemento(m);
					}
					stack.addFirst(m);
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					stack.removeFirst();
				}
			});
			reader.parse(new InputSource(r));
			return result[0];
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(256);
		buf.append('{');
		buf.append(type);
		for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
			Entry e = (Entry) i.next();
			buf.append(' ');
			buf.append(e.getKey());
			buf.append('=');
			buf.append(e.getValue());
		}
		for (Iterator i = children.iterator(); i.hasNext();) {
			buf.append(' ');
			buf.append(i.next().toString());
		}
		buf.append('}');
		return buf.toString();
	}
}