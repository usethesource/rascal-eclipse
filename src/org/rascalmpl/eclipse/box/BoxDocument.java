package org.rascalmpl.eclipse.box;

import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TypedPosition;

public class BoxDocument extends Document {

	enum TAG {
		it, nm, bf, df;
	}

	class Frame {
		final TAG tag;
		final int start;

		Frame(TAG tag, int start) {
			this.tag = tag;
			this.start = start;
		}
	}

//	public BoxDocument() {
//		for (TAG t : TAG.values()) {
//			this.addPositionCategory(t.name());
//		}
//	}

	private void addString(String s) {
		try {
			this.replace(this.getLength(), 0, s);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void computeDocument(String textToPrint) {
		final Stack<Frame> stack = new Stack<Frame>();
		TAG current = TAG.df;
		StringTokenizer t = new StringTokenizer(textToPrint, "\n\b", true);
		while (t.hasMoreTokens()) {
			String c = t.nextToken();
			if (c.equals("\n")) {
				addString("\n");
			} else if (c.equals("\b")) {
				c = t.nextToken();
				if (c.charAt(0) == '{') {
					String key = c.substring(1, 3);
					TAG tag = TAG.valueOf(key);
					stack.push(new Frame(current, this.getLength()));
					current = tag;
				} else if (c.charAt(0) == '}') {
					Frame frame = stack.pop();
					TypedPosition p = new TypedPosition(frame.start, this.getLength()
							- frame.start, current.name());
					try {
						this.addPosition(p);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					current = frame.tag;
				}
				addString(c.substring(3));
			} else {
				addString(c);
			}
		}
	}

}
