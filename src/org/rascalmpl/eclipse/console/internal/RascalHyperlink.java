package org.rascalmpl.eclipse.console.internal;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.uri.URIResourceResolver;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.uri.URIUtil;

public class RascalHyperlink implements IHyperlink {
	private static final int INVALID_OFFSET = -1;
  private String target;
	private PrintWriter err;
  private IEvaluatorContext ctx;

	public RascalHyperlink(String target, IEvaluatorContext ctx, PrintWriter err) {
	  this.ctx = ctx;
		this.target = target;
		this.err = err;
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
		try {
			IResource res = URIResourceResolver.getResource(getURIPart());
			if (res != null && res instanceof IFile) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart part = IDE.openEditor(page, (IFile)res);
				if (getOffsetPart() > -1 && part instanceof ITextEditor) {
					((ITextEditor)part).selectAndReveal(getOffsetPart(), getLength());
				}
			}
			else if (getURIPart().getScheme().equals("http")) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(getURIPart().toURL());
				} catch (PartInitException e) {
					Activator.log("Cannot get editor part", e);
				} catch (MalformedURLException e) {
					err.println("Cannot open link " + target);
					Activator.log("Cannot resolve link", e);
				}
			}
			else {
				err.println("Cannot open link " + target);
			}
		} catch (PartInitException e) {
			Activator.log("Cannot get editor part", e);
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
			  loc = ctx.getHeap().resolveSourceLocation(vf.sourceLocation(uri, offset, length));
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
