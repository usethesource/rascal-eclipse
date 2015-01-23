package org.rascalmpl.eclipse.console.internal;

import java.io.PrintWriter;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.uri.URIUtil;

public class RascalHyperlink implements IHyperlink {
	private static final int INVALID_OFFSET = -1;
	private String target;
	private IEvaluatorContext ctx;
	private final InteractiveInterpreterConsole console;
	private final int srcOffset;
	private final int srcLen;

	public int getSrcOffset() {
		return srcOffset;
	}

	public int getSrcLength() {
		return srcLen;
	}

	public RascalHyperlink(InteractiveInterpreterConsole console, int srcOffset, int srcLen, String target, IEvaluatorContext ctx, PrintWriter err) {
		this.ctx = ctx;
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
			EditorUtil.openAndSelectURI(getURIPart(), getOffsetPart(), getLength(), ctx.getResolverRegistry());
		}
		else {
			EditorUtil.openAndSelectURI(getURIPart(), ctx.getResolverRegistry());
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

	private URI uri = null;
	private URI getURIPart() {
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
				uri = URIUtil.assumeCorrect(m.group(1));
				if (m.group(2) != null) {
					offset = Integer.parseInt(m.group(2));
					length = Integer.parseInt(m.group(3));
				}
			}

			IValueFactory vf = ctx.getValueFactory();
			ISourceLocation loc;
			if (offset != INVALID_OFFSET) {
				loc = ctx.getHeap().resolveSourceLocation(vf.sourceLocation(vf.sourceLocation(uri), offset, length));
			}
			else {
				loc = ctx.getHeap().resolveSourceLocation(vf.sourceLocation(uri));
			}
			uri = loc.getURI();
			if (loc.hasOffsetLength()) {
				offset = loc.getOffset(); 
				length = loc.getLength();
			}
		}
	}
}
