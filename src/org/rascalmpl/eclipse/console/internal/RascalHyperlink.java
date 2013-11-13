package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
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
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.uri.URIResourceResolver;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.uri.URIUtil;

public class RascalHyperlink implements IHyperlink {
	private static final int INVALID_OFFSET = -1;
	private String target;
	private PrintWriter err;
	private IEvaluatorContext ctx;
	private String projectName;
	private final InteractiveInterpreterConsole console;
	private final int srcOffset;
	private final int srcLen;

	public int getSrcOffset() {
		return srcOffset;
	}

	public int getSrcLength() {
		return srcLen;
	}

	public RascalHyperlink(InteractiveInterpreterConsole console, int srcOffset, int srcLen, String target, IEvaluatorContext ctx, String projectName, PrintWriter err) {
		this.ctx = ctx;
		this.srcOffset = srcOffset;
		this.srcLen = srcLen;
		this.console = console;
		this.target = target;
		this.err = err;
		this.projectName = projectName;
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

		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			IResource res = URIResourceResolver.getResource(getURIPart(), projectName);
			if (res != null && res instanceof IFile) {
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
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(getURIPart());

				if (fileStore.fetchInfo().exists()) {
					IDE.openEditorOnFileStore( page, fileStore );
				}
				else {
					IEvaluatorContext eval = ctx;
					if (projectName != null) {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project);
					}

					if (eval != null) {
						URI resourceURI = eval.getResolverRegistry().getResourceURI(getURIPart());

						if (resourceURI.getScheme().equals("project")) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(resourceURI.getAuthority());
							IFile file = project.getFile(resourceURI.getPath());
							IEditorPart part = IDE.openEditor(page, file);
							if (getOffsetPart() > -1 && part instanceof ITextEditor) {
								((ITextEditor)part).selectAndReveal(getOffsetPart(), getLength());
							}
							return;
						}

						if (resourceURI != null) {
							URL find = FileLocator.resolve(resourceURI.toURL());
							fileStore = EFS.getLocalFileSystem().getStore(find.toURI());

							if (fileStore != null && fileStore.fetchInfo().exists()) {
								IEditorPart part = IDE.openEditorOnFileStore( page, fileStore );
								if (getOffsetPart() > -1 && part instanceof ITextEditor) {
									((ITextEditor)part).selectAndReveal(getOffsetPart(), getLength());
								}
								return;
							}
						}
					}
				}

				Activator.log("can not open link", null);
			}
		} catch (IOException | CoreException e) {
			Activator.log("Cannot follow link", e);
		} catch (URISyntaxException e) {
			Activator.log("Cannot follow link", e);
		} catch (Throw e) {
			Activator.log("Cannot follow link", e);
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
