package org.rascalmpl.eclipse.pages;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.result.IRascalResult;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.utils.LimitedResultWriter.IOLimitReachedException;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.repl.LimitedLineWriter;
import org.rascalmpl.repl.LimitedWriter;
import org.rascalmpl.values.RascalValueFactory;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.TreeAdapter;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.parser.IModelListener;
import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.IEditorService;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IListWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.StandardTextWriter;
import io.usethesource.vallang.type.Type;

public class EvalAndPatch implements IModelListener, IEditorService {

	private final static int LINE_LIMIT = 200;
	private final static int CHAR_LIMIT = LINE_LIMIT * 20;

	public EvalAndPatch() {
		// TODO Auto-generated constructor stub
	}

	private String printResult(IRascalResult result) throws IOException {
		if (result == null) {
			return "";
		}

		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		IValue value = result.getValue();

		if (value == null) {
			return "";
		}

		Type type = result.getStaticType();

		StandardTextWriter indentedPrettyPrinter = new StandardTextWriter();

		if (type.isAbstractData() && type.isSubtypeOf(RascalValueFactory.Tree)) {
			out.print(type.toString());
			out.print(": ");
			// we first unparse the tree
			out.print("`");
			TreeAdapter.yield((IConstructor) result.getValue(), true, out);
			out.println("`");
			// write parse tree out one a single line for reference
			out.print("Tree: ");

			StandardTextWriter singleLinePrettyPrinter = new StandardTextWriter(false);
			try (Writer wrt = new LimitedWriter(out, CHAR_LIMIT)) {
				singleLinePrettyPrinter.write(value, wrt);
			}
	    	catch (IOLimitReachedException e) {
	    	    // ignore since this is what we wanted
	    	}
		} else {
			out.print(type.toString());
			out.print(": ");
			// limit both the lines and the characters
			try (Writer wrt = new LimitedWriter(new LimitedLineWriter(out, LINE_LIMIT), CHAR_LIMIT)) {
				indentedPrettyPrinter.write(value, wrt);
			}
	    	catch (IOLimitReachedException e) {
	    	    // ignore since this is what we wanted
	    	}
		}
		out.flush();
		return sw.toString();
	}

	private IList evalCommands(IValueFactory values, IList commands, ISourceLocation loc, Evaluator eval) {
		StringWriter out = new StringWriter();
		StringWriter err = new StringWriter();
		IListWriter result = values.listWriter();
		int outOffset = 0;
		int errOffset = 0;

		for (IValue v : commands) {
			ITree cmd = (ITree)v;
			if (TreeAdapter.getConstructorName(cmd) != null && TreeAdapter.getConstructorName(cmd).equals("output")) {
				continue;
			}
			String errOut = "";
			boolean exc = false;
			Result<IValue> x = null;
			try {
				eval.overrideDefaultWriters(eval.getInput(), eval.getStdOut(), eval.getStdErr());
				x = new ASTBuilder().buildValue(cmd).interpret(eval);
			} catch (Throwable e) {
				errOut = err.getBuffer().substring(errOffset);
				errOffset += errOut.length();
				errOut += e.getMessage();
				exc = true;
			}
			finally {
				eval.overrideDefaultWriters(eval.getInput(), eval.getStdOut(),  eval.getStdErr());
			}
			String output = out.getBuffer().substring(outOffset);
			outOffset += output.length();
			if (!exc) {
				errOut += err.getBuffer().substring(errOffset);
				errOffset += errOut.length();
			}
			String s;
			try {
				s = printResult(x);
				ITuple tuple = values.tuple(values.string(s), values.string(output), values.string(errOut));
				result.append(tuple);
			} catch (IOException e) {
				continue;
			}
		}
		IList results = result.done();
		return results;
	}

	private String resultSource(IValueFactory vf, ITuple output, boolean[] addedSpace) {
		addedSpace[0] = false;
		IString val = (IString) output.get(0);
		IString out = (IString) output.get(1);
		IString err = (IString) output.get(2);
		
		if (val.length() == 0 && out.length() == 0 && err.length() == 0) {
			return "";
		}
		
		String code = "";
		if (val.length() > 0) {
			String x = val.getValue();
			if (x.contains("origin=")) {
				x = x.substring(0, x.indexOf(":"));
			}
			if (x.contains("`") && x.contains("Tree: ")) {
				x = x.substring(0, x.indexOf("Tree: "));
			}
			code += " ⇨ " + x.replaceAll("\n", " ") + "\n";
			addedSpace[0] = true;
		}
		
		if (out.length() > 0) {			
			if (!code.endsWith("\n")) {
				code += "\n";
			}
			String txt = out.getValue().replaceAll("\n", "\n≫ ");
			int ind = txt.lastIndexOf("\n≫ ");
			if (ind == txt.length() - 3) {
				txt = txt.substring(0, ind) + txt.substring(ind + 2, txt.length());
			}
			code += "≫ " + txt;
		}
		
		if (err.length() > 0 ) {
			if (!code.endsWith("\n")) {
				code += "\n";
			}
			String x = err.getValue();
			if (x.contains("")) {
				x = x.substring(0, x.indexOf(""));
			}
			code += "⚠ " + x.trim().replaceAll("\n", "\n⚠ ");
		}
		return code;
	}

	private IList patch(IValueFactory vf, IList args /* non ast */, IList results) {
		IListWriter patch = vf.listWriter();

		int delta = 0; // maintain where we are in the results list.
		boolean[] addedSpace = new boolean[1]; // whether a leading space was added as part of output.

		boolean change = false;
		for (int i = 0; i < args.length(); i++) {

			if (i % 2 == 0) { // a non-layout node
				if (!(TreeAdapter.getConstructorName((ITree) args.get(i)).equals("output"))) { // a proper command
					String src = resultSource(vf, (ITuple) results.get((i / 2) - delta), addedSpace);

					// collect all subsequent layout and outputs following
					// the current command to determine whether the computed
					// output is different from the output in the previous run.
					String old = "";
					int j = i + 2;
					while (j < args.length() && TreeAdapter.getConstructorName((ITree) args.get(j)).equals("output")) {
						// TODO: do not eliminate comments!
						old += TreeAdapter.yield((IConstructor) args.get(j - 1)) + TreeAdapter.yield((IConstructor) args.get(j));
						j += 2;
					}

					// if there's a change in output, add a tuple
					// to the patch.        
					if (!src.isEmpty()  && !old.trim().equals(src.trim())) {
						ISourceLocation org = TreeAdapter.getLocation((ITree) args.get(i));
						int at = org.getOffset() + org.getLength();
						ISourceLocation l = vf.sourceLocation(org, at, 0); // insert
						patch.append(vf.tuple(l, vf.string(src)));
						change = true;
					}
					else {
						// signal to following iterations that there was no change.
						change = false;
					}
				}
				else {
					if (change) {
						// only remove previous output nodes if there was a change
						patch.append(vf.tuple(TreeAdapter.getLocation((ITree) args.get(i)), vf.string("")));
					}

					// output commands are not evaluated by evalCommands above;
					// delta maintains the difference for indexing.
					delta += 1;
				}
			}
			else {
				String l = TreeAdapter.yield((IConstructor) args.get(i));
				if (addedSpace[0] && change && l.startsWith(" ")) {
					// if a leading space was added in the case of changed output,
					// remove it here. Otherwise leave the layout unchanged.
					ISourceLocation org = TreeAdapter.getLocation((ITree) args.get(i));
					org = vf.sourceLocation(org, org.getOffset(), 1);
					patch.append(vf.tuple(org, vf.string("")));
					addedSpace[0] = false;
				}
			}
		}

		return patch.done();
	}
	
	
	class Job extends UIJob {

		private IList patch;
		private IDocument doc;

		public Job(IList patch, IDocument doc) {
			super("updating editor");
			this.patch = patch;
			this.doc = doc;
		}
		
		
		

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			DocumentRewriteSession session = ((IDocumentExtension4) doc)
					.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
			try {
				int offset = 0;
				for (IValue v : patch) {
					ITuple subst = (ITuple) v;
					ISourceLocation loc = (ISourceLocation) subst.get(0);
					IString txt = (IString) subst.get(1);
					doc.replace(loc.getOffset() + offset, loc.getLength(), txt.getValue());
					offset += txt.length() - loc.getLength();
				}
				String lastChar = doc.get(doc.getLength() - 1, 1);
				if (!lastChar.equals("\n")) {
					doc.replace(doc.getLength(), 0, "\n");
				}
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			} catch (BadLocationException e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			} finally {
				((IDocumentExtension4) doc).stopRewriteSession(session);
			}
			return Status.OK_STATUS;
		}

	}

	@Override
	public String getName() {
		return "EvalAndPatch";
	}

	@Override
	public void setEditor(UniversalEditor editor) {
		// unused
	}

	@Override
	public void update(IParseController parseController, IProgressMonitor monitor) {
		ITree pt = (ITree) parseController.getCurrentAst();
		if (pt == null) {
			return;
		}

		IList commands = TreeAdapter.getASTArgs((ITree) TreeAdapter.getASTArgs(TreeAdapter.getArg(pt, "top")).get(0));
		Evaluator evaluator = ((ParseController)parseController).getEvaluator();
		IList results = null;
		synchronized (evaluator) {
			Environment env = evaluator.getCurrentEnvt();
			try {
				evaluator.setCurrentEnvt(new ModuleEnvironment("Scrapbook", evaluator.getHeap()));
				results = evalCommands(evaluator.getValueFactory(), commands, TreeAdapter.getLocation(pt), evaluator);
			}
			finally {
				evaluator.setCurrentEnvt(env);
			}
		}
		if (results == null) {
			return;
		}
		IList patch = patch(evaluator.getValueFactory(), TreeAdapter.getArgs((ITree) TreeAdapter.getASTArgs(TreeAdapter.getArg(pt, "top")).get(0)), results);

		if (patch.isEmpty()) {
			return;
		}

		Job job = new Job(patch, parseController.getDocument());
		job.schedule();
	}

}
