package org.rascalmpl.eclipse.console.internal;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

public class RascalHyperlink implements IHyperlink {
	private static final int INVALID_OFFSET = -1;
	private String target;
	private final InteractiveInterpreterConsole console;
	private final int srcOffset;
	private final int srcLen;

	public int getSrcOffset() {
		return srcOffset;
	}

	public int getSrcLength() {
		return srcLen;
	}

	public RascalHyperlink(InteractiveInterpreterConsole console, int srcOffset, int srcLen, String target, PrintWriter err) {
		this.srcOffset = srcOffset;
		this.srcLen = srcLen;
		this.console = console;
		this.target = target;
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
		console.setSelection(srcOffset - 1, srcLen + 1);
		if (getOffsetPart() > -1) {
			EditorUtil.openAndSelectURI(ValueFactoryFactory.getValueFactory().sourceLocation(getURIPart(), getOffsetPart(), getLength()));
		}
		else {
			EditorUtil.openAndSelectURI(getURIPart());
		}
	}


	private int length = -1;
	private int getLength() {
		makeSureLinkIsParsed();
		return length;
	}

	private int offset = INVALID_OFFSET;
	private int getOffsetPart() {
		makeSureLinkIsParsed();
		return offset;
	}

	private ISourceLocation uri = null;
	private ISourceLocation getURIPart() {
		makeSureLinkIsParsed();
		return uri;
	}

	private boolean linkParsed = false;
	private Pattern splitParts = Pattern.compile("\\|([^\\|]*)\\|(?:\\(\\s*([0-9]+)\\s*,\\s*([0-9]+))?");
	private void makeSureLinkIsParsed() {
		if (!linkParsed) {
			linkParsed = true;
			Matcher m = splitParts.matcher(target);
			if (m.find()) {
				uri = ValueFactoryFactory.getValueFactory().sourceLocation(URIUtil.assumeCorrect(m.group(1)));
				if (m.group(2) != null) {
					offset = Integer.parseInt(m.group(2));
					length = Integer.parseInt(m.group(3));
				}
			}
		}
	}
}
