package org.rascalmpl.eclipse.library.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.Prelude;

public class FastPrint {
	class PrintSwitching {
		private final Evaluator eval;
		private PrintWriter prevErr;
		private PrintWriter prevOut;
		private PrintWriter newOut;

		public PrintSwitching(IEvaluatorContext ctx) {
			if (ctx instanceof Evaluator) {
				eval = (Evaluator)ctx;
				prevErr = eval.getStdErr();
				prevOut = eval.getStdOut();
				try {
					newOut = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdOut(), "UTF16"), false);
				} catch (UnsupportedEncodingException e) {
					Activator.log("could not create a new writer", e);
				}
				eval.overrideDefaultWriters(newOut, prevErr);
			}
			else {
				eval = null;
			}
		}
		
		public void revert() {
			if (eval != null) {
				newOut.flush();
				eval.overrideDefaultWriters(prevOut, prevErr);
			}
		}
		
		
	}
	private IValueFactory VF;
	
	public FastPrint(IValueFactory vf) { this.VF = vf; }
	
	public void fprint(IValue arg, IEvaluatorContext ctx) {
		PrintSwitching ps = new PrintSwitching(ctx);
		try {
			new Prelude(VF).print(arg, ctx);
		}
		finally {
			ps.revert();
		}
	}
	public void fprintln(IValue arg, IEvaluatorContext ctx) {
		PrintSwitching ps = new PrintSwitching(ctx);
		try {
			new Prelude(VF).println(arg, ctx);
		}
		finally {
			ps.revert();
		}
	}
	public void fiprint(IValue arg, IEvaluatorContext ctx) {
		PrintSwitching ps = new PrintSwitching(ctx);
		try {
			new Prelude(VF).iprint(arg, ctx);
		}
		finally {
			ps.revert();
		}
	}
	public void fiprintln(IValue arg, IEvaluatorContext ctx) {
		PrintSwitching ps = new PrintSwitching(ctx);
		try {
			new Prelude(VF).iprintln(arg, ctx);
		}
		finally {
			ps.revert();
		}
	}
}
