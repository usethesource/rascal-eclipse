package org.rascalmpl.eclipse.console.outputconsole;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.MessageConsole;

public class TestStyledText extends Composite {


	StyledText t;
	int i;
	class PrintStuff implements Runnable{
		PrintStream content;
		
		public PrintStuff(CircularLines content) {
			try {
				this.content = new PrintStream(content, false, "UTF16");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			int i = 0;
			for(int j = 0 ; ; j++){
				try {
					content.printf("Hallo" + i + "\n");
					content.flush();
					i++;
					System.out.printf("Done writing!" + i + "\n");
					Thread.sleep(10);
				} catch (InterruptedException e) {
	
				}
				
		}
			
		}
		
	}
	
		TestStyledText(Composite parent) {
			super(parent,SWT.NONE);
			setLayout(new FillLayout(SWT.VERTICAL));
			t = new StyledText(this, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL );
			t.setEditable(false);
			CircularLines content = new CircularLines(200);
			t.setContent(content);
			Thread t = new Thread(new PrintStuff(content));
			t.start();
//			b = new BoxesElement(this, boxesB);
		}
		

		public static void main(String argv[]) throws UnsupportedEncodingException{
			final Display display = new Display ();
			final Shell shell = new Shell (display, SWT.SHELL_TRIM);
			shell.setLayout(new FillLayout ());
			shell.setSize (1000, 800);
			new TestStyledText(shell);
			shell.pack();
			shell.open ();
			while (!shell.isDisposed ()) {
				if (display != null && !display.readAndDispatch ())
					display.sleep ();
			}
			display.dispose ();
//			CircularLines content = new CircularLines(10);
//			PrintStream content2 = new PrintStream(content, false, "UTF16");
//			for(int i = 0 ; i < 15; i++){
//				content2.print("HALLO!!!" + i + "\n");
//				content2.flush();
////				System.out.printf("%d\n",content.nrLines);
//			}
//			content2.flush();
//			for(int i = 0 ; i < content.getLineCount()  ; i++){
//				System.out.printf("%d: %s\n", i, content.getLine(i));
//			}
		}



	
}
