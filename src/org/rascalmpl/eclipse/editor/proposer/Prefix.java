package org.rascalmpl.eclipse.editor.proposer;

import org.eclipse.jface.text.IDocument;

public class Prefix {
	public static Prefix getPrefix(IDocument document, int offset, int length, String allowedChars) {
		if (length > 0) {
			return new Prefix(document.get().substring(offset, offset + length), offset);
		}

		String prefix = "";
		String content = document.get();
		int prefixOffset = offset;

		if (!allowedChars.isEmpty()) {
			if (offset > 0) {
				for (int cur = offset - 1; cur >= 0 && allowedChars.indexOf(content.charAt(cur)) != -1; cur--) {
					prefix = content.charAt(cur) + prefix;
				}
			}
	
			for (int cur = offset; cur != content.length() && allowedChars.indexOf(content.charAt(cur)) != -1; cur++) {
				prefix += content.charAt(cur);
				prefixOffset = cur + 1;
			}
		}

		return new Prefix(prefix, prefixOffset);
	}
	
	private String text = "";

	private int offset = 0;
	
	public Prefix(String text, int offset) {
		this.text = text;
		this.offset = offset;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public String getText() {
		return text;
	}
}
