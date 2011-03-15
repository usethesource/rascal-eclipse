package org.rascalmpl.eclipse.box;

import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TypedPosition;

public class BoxDocument extends Document {

	
	class Frame {
		final Box.TAG tag;
		final int start;

		Frame(Box.TAG tag, int start) {
			this.tag = tag;
			this.start = start;
		}
	}

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
		Box.TAG current = Box.TAG.DF;
		StringTokenizer t = new StringTokenizer(textToPrint, "\n\r", true);
		while (t.hasMoreTokens()) {
			String c = t.nextToken();
			if (c.equals("\n")) {
				addString("\n");
			} else if (c.equals("\r")) {
				c = t.nextToken();
				if (c.charAt(0) == '{') {
					String key = c.substring(1, 3);
					Box.TAG tag = Box.TAG.valueOf(key);
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
