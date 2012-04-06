/* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. */

//Contributors: Bogdan Onoiu (Genderal character encoding abstraction and UTF-8 Support)
package org.kxml2.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.xmlpull.v1.XmlSerializer;
// TODO: make some of the "direct" WBXML token writing methods public??

/**
 * A class for writing WBXML.
 * 
 */

@SuppressWarnings("unchecked")
public class WbxmlSerializer implements XmlSerializer {

	public static final HashMap HEXMAP = new HashMap();

	static {
		HEXMAP.put("0", new Integer(0));
		HEXMAP.put("1", new Integer(1));
		HEXMAP.put("2", new Integer(2));
		HEXMAP.put("3", new Integer(3));
		HEXMAP.put("4", new Integer(4));
		HEXMAP.put("5", new Integer(5));
		HEXMAP.put("6", new Integer(6));
		HEXMAP.put("7", new Integer(7));
		HEXMAP.put("8", new Integer(8));
		HEXMAP.put("9", new Integer(9));
		HEXMAP.put("A", new Integer(10));
		HEXMAP.put("B", new Integer(11));
		HEXMAP.put("C", new Integer(12));
		HEXMAP.put("D", new Integer(13));
		HEXMAP.put("E", new Integer(14));
		HEXMAP.put("F", new Integer(15));
	}
	Hashtable stringTable = new Hashtable();

	OutputStream out;

	ByteArrayOutputStream buf = new ByteArrayOutputStream();
	ByteArrayOutputStream stringTableBuf = new ByteArrayOutputStream();

	String pending;
	int depth;
	String name;
	String namespace;
	Vector attributes = new Vector();

	Hashtable attrStartTable = new Hashtable();
	Hashtable attrValueTable = new Hashtable();
	Hashtable tagTable = new Hashtable();

	private int attrPage;
	private int tagPage;

	private String encoding = null;

	public XmlSerializer attribute(String namespace, String name, String value) {
		attributes.addElement(name);
		attributes.addElement(value);
		return this;
	}

	public void cdsect(String cdsect) throws IOException {
		text(cdsect);
	}

	/* silently ignore comment */

	public void comment(String comment) {
	}

	public void docdecl(String docdecl) {
		throw new RuntimeException("Cannot write docdecl for WBXML");
	}

	public void entityRef(String er) {
		throw new RuntimeException("EntityReference not supported for WBXML");
	}

	public int getDepth() {
		return depth;
	}

	public boolean getFeature(String name) {
		return false;
	}

	public String getNamespace() {
		throw new RuntimeException("NYI");
	}

	public String getName() {
		throw new RuntimeException("NYI");
	}

	public String getPrefix(String nsp, boolean create) {
		throw new RuntimeException("NYI");
	}

	public Object getProperty(String name) {
		return null;
	}

	public void ignorableWhitespace(String sp) {
	}

	public void endDocument() throws IOException {
		writeInt(out, stringTableBuf.size());

		// write StringTable

		out.write(stringTableBuf.toByteArray());

		// write buf

		out.write(buf.toByteArray());

		// ready!

		out.flush();
	}

	/**
	 * ATTENTION: flush cannot work since Wbxml documents require need
	 * buffering. Thus, this call does nothing.
	 */

	public void flush() {
	}

	public void checkPending(boolean degenerated) throws IOException {
		if (pending == null || pending.length() == 0)
			return;

		int len = attributes.size();

		int[] idx = (int[]) this.tagTable.get(pending);
		// if no entry in known table, then add as literal
		if (idx == null) {
			buf.write(len == 0
					? (degenerated ? Wbxml.LITERAL : Wbxml.LITERAL_C)
					: (degenerated ? Wbxml.LITERAL_A : Wbxml.LITERAL_AC));

			writeStrT(pending);
		} else {
			if (idx[0] != tagPage) {
				tagPage = idx[0];
				buf.write(Wbxml.SWITCH_PAGE);
				buf.write(tagPage);
			}

			buf.write(len == 0
					? (degenerated ? idx[1] : idx[1] | 64)
					: (degenerated ? idx[1] | 128 : idx[1] | 192));
		}
		// adding attributes if there are some.
		// the orignal processing is not suitable for WV att table,
		// this is a modified one, but i can not promise this will
		// be right.
		for (int i = 0; i < len;) {
			int start = i;

			boolean matchInWhole = false;
			// first try to match the attributes as a whole.
			String k = (String) attributes.elementAt(i);
			String v = (String) attributes.elementAt(++i);
			String attr = k + "=" + v;

			for (Iterator it = attrStartTable.keySet().iterator(); it.hasNext();) {
				String attrInTable = (String) it.next();
				if (attr.toLowerCase().startsWith(attrInTable.toLowerCase())) {
					idx = (int[]) attrStartTable.get(attrInTable);
					// seems no need here, since we only have one
					// attrStartTable.
					// i don't know why the orignal author add this code
					// anywhere,
					// careless copy and paste?
					// if(idx[0] != attrPage){
					// attrPage = idx[0];
					// buf.write(0);
					// buf.write(attrPage);
					// }
					buf.write(idx[1]);
					// add in line string if needed
					if (attr.length() > attrInTable.length()) {
						String strInLine = attr.substring(attrInTable.length());
						buf.write(Wbxml.STR_I);
						writeStrI(buf, strInLine);
					}
					++i;
					matchInWhole = true;
					break;
				}
			}
			if (!matchInWhole) {
				i = start;

				idx = (int[]) attrStartTable.get(attributes.elementAt(i));
				if (idx == null) {
					buf.write(Wbxml.LITERAL);
					writeStrT((String) attributes.elementAt(i));
				} else {
					if (idx[0] != attrPage) {
						attrPage = idx[0];
						buf.write(0);
						buf.write(attrPage);
					}
					buf.write(idx[1]);
				}
				idx = (int[]) attrValueTable.get(attributes.elementAt(++i));
				if (idx == null) {
					buf.write(Wbxml.STR_I);
					writeStrI(buf, (String) attributes.elementAt(i));
				} else {
					if (idx[0] != attrPage) {
						attrPage = idx[0];
						buf.write(0);
						buf.write(attrPage);
					}
					buf.write(idx[1]);
				}
				++i;
			}
		}
		if (len > 0)
			buf.write(Wbxml.END);
		pending = null;
		attributes.removeAllElements();
	}

	public void processingInstruction(String pi) {
		throw new RuntimeException("PI NYI");
	}

	public void setFeature(String name, boolean value) {
		throw new IllegalArgumentException("unknown feature " + name);
	}

	public void setOutput(Writer writer) {
		throw new RuntimeException("Wbxml requires an OutputStream!");
	}

	public void setOutput(OutputStream out, String encoding) throws IOException {

		if (encoding != null)
			throw new IllegalArgumentException(
					"encoding not yet supported for WBXML");

		this.out = out;

		buf = new ByteArrayOutputStream();
		stringTableBuf = new ByteArrayOutputStream();

		// ok, write header
	}

	public void setPrefix(String prefix, String nsp) {
		throw new RuntimeException("NYI");
	}

	public void setProperty(String property, Object value) {
		throw new IllegalArgumentException("unknown property " + property);
	}

	public void startDocument(String s, Boolean b) throws IOException {
		out.write(0x03); // version 1.3
		// http://www.openmobilealliance.org/tech/omna/omna-wbxml-public-docid.htm
		out.write(0x01); // unknown or missing public identifier

		// default encoding is UTF-8
		String[] encodings = {"UTF-8", "ISO-8859-1"};
		if (s == null || s.toUpperCase().equals(encodings[0])) {
			encoding = encodings[0];
			out.write(106);
		} else if (true == s.toUpperCase().equals(encodings[1])) {
			encoding = encodings[1];
			out.write(0x04);
		} else {
			throw new UnsupportedEncodingException(s);
		}
	}

	public XmlSerializer startTag(String namespace, String name)
			throws IOException {

		if (namespace != null && !"".equals(namespace))
			throw new RuntimeException("NSP NYI");

		// current = new State(current, prefixMap, name);

		checkPending(false);
		pending = name;
		depth++;

		return this;
	}

	public XmlSerializer text(char[] chars, int start, int len)
			throws IOException {

		checkPending(false);

		buf.write(Wbxml.STR_I);
		writeStrI(buf, new String(chars, start, len));

		return this;
	}

	public XmlSerializer text(String text) throws IOException {

		checkPending(false);

		int[] idx = (int[]) this.attrValueTable.get(text);
		if (idx == null) {
			int value = -1;
			try {
				value = Integer.parseInt(text);
			} catch (NumberFormatException ex) {
				;
			}

			byte[] stream = buf.toByteArray();
			byte lastByte = stream[stream.length - 1];
			int tag = lastByte & 63;

			if (value > -1
					&& ((tagPage == 0x00 && (tag == 0x0B || tag == 0x0F || tag == 0x3C)) || (tagPage == 0x01 && (tag == 0x32 || tag == 0x1C)))) {
				// "Code[0x00, 0x0B]", "ContentSize[0x00, 0x0F]",
				// *"DateTime[0x00, 0x11]", "Validity[0x00, 0x3C]",
				// "TimeToLive[0x01, 0x32]", "KeepAliveTime[0x01, 0x1C]" is
				// integer value and need to
				// be opaque encoded ("*DateTime" is not realized, it is
				// somewhat so complex and can be substitude with inline
				// string according to the conformance document).
				buf.write(Wbxml.OPAQUE);
				byte[] opaqued = opaqueEncode(text);
				buf.write(opaqued.length);
				buf.write(opaqueEncode(text));
			} else {
				buf.write(Wbxml.STR_I);
				writeStrI(buf, text);
			}
		} else {
			buf.write(Wbxml.EXT_T_0);
			buf.write(idx[1]);
		}

		return this;
	}

	/**
	 * since I only encode a int value here, so no need to determine the page
	 * code and the tag code in the method.
	 * 
	 * but if it is a full realization, this is a must.
	 * 
	 * @param v
	 * @return
	 */
	public byte[] opaqueEncode(String v) {
		String hexString = "00";
		try {
			int iValue = Integer.parseInt(v);
			hexString = Integer.toHexString(iValue);
		} catch (NumberFormatException ex) {
			;
		}
		if (hexString.length() % 2 != 0)
			hexString = "0" + hexString;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < hexString.length(); i = i + 2) {
			baos.write(hex2Byte(hexString.substring(i, i + 2)));
		}
		return baos.toByteArray();
	}

	/**
	 * hex is a string with the length of two and holds 2 hex digits(big
	 * endian).
	 * 
	 * @param hexStr
	 *            string holds two hex digits.
	 * @return int value of the byte
	 */
	@SuppressWarnings("unchecked")
	public int hex2Byte(String hexStr) {
		if (hexStr.length() != 2) {
			throw new RuntimeException(
					"only two hex digits are valid, input unvalid");
		}
		hexStr = hexStr.toUpperCase();
		String hexH = hexStr.substring(0, 1);
		String hexL = hexStr.substring(1, 2);
		if (!HEXMAP.keySet().contains(hexH) || !HEXMAP.keySet().contains(hexL)) {
			throw new RuntimeException(
					"unvalid hex digits found, input unvalid");
		}
		int high = ((Integer) HEXMAP.get(hexH)).intValue() << 4;
		int low = ((Integer) HEXMAP.get(hexL)).intValue();
		return (high + low);
	}

	public XmlSerializer endTag(String namespace, String name)
			throws IOException {

		if (pending != null)
			checkPending(true);
		else
			buf.write(Wbxml.END);

		depth--;

		return this;
	}

	/** currently ignored! */

	public void writeLegacy(int type, String data) {
	}

	// ------------- internal methods --------------------------

	static void writeInt(OutputStream out, int i) throws IOException {
		byte[] buf = new byte[5];
		int idx = 0;

		do {
			buf[idx++] = (byte) (i & 0x7f);
			i = i >> 7;
		} while (i != 0);

		while (idx > 1) {
			out.write(buf[--idx] | 0x80);
		}
		out.write(buf[0]);
	}

	void writeStrI(OutputStream out, String s) throws IOException {
		/*
		 * below is the orginal implementation,
		 * it totally ignores the character set,
		 * but write the int unicode value of every
		 * single character in the string, which
		 * is not suitable here.
		for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
		*/
		byte[] bs = s.getBytes(encoding);
		out.write(bs);
		out.write(0x00);
	}

	void writeStrT(String s) throws IOException {

		Integer idx = (Integer) stringTable.get(s);

		if (idx == null) {
			idx = new Integer(stringTableBuf.size());
			stringTable.put(s, idx);
			writeStrI(stringTableBuf, s);
			stringTableBuf.flush();
		}

		writeInt(buf, idx.intValue());
	}

	/**
	 * Sets the tag table for a given page. The first string in the array
	 * defines tag 5, the second tag 6 etc.
	 */

	@SuppressWarnings("unchecked")
	public void setTagTable(int page, String[] tagTable) {
		if (page < 0 || tagTable.length == 0)
			return;

		for (int i = 0; i < tagTable.length; i++) {
			if (tagTable[i] != null) {
				Object idx = new int[]{page, i + 5};
				this.tagTable.put(tagTable[i], idx);
			}
		}
	}

	/**
	 * Sets the attribute start Table for a given page. The first string in the
	 * array defines attribute 5, the second attribute 6 etc. Please use the
	 * character '=' (without quote!) as delimiter between the attribute name
	 * and the (start of the) value
	 */
	@SuppressWarnings("unchecked")
	public void setAttrStartTable(int page, String[] attrStartTable) {

		for (int i = 0; i < attrStartTable.length; i++) {
			if (attrStartTable[i] != null) {
				Object idx = new int[]{page, i + 5};
				this.attrStartTable.put(attrStartTable[i], idx);
			}
		}
	}

	/**
	 * Sets the attribute value Table for a given page. The first string in the
	 * array defines attribute value 0x85, the second attribute value 0x86 etc.
	 * 
	 * !!!!! no need to add 0x85 to wml attrValueTable now, since I insert 85
	 * "null"s to the Wml attribute value table already, and by doing this, the
	 * method is compatible with WV attrValueTable.
	 */
	@SuppressWarnings("unchecked")
	public void setAttrValueTable(int page, String[] attrValueTable) {
		// clear entries in this.table!
		for (int i = 0; i < attrValueTable.length; i++) {
			if (attrValueTable[i] != null) {
				Object idx = new int[]{page, i};
				this.attrValueTable.put(attrValueTable[i], idx);
			}
		}
	}

}