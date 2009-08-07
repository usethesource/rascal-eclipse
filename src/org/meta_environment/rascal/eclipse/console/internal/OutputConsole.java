package org.meta_environment.rascal.eclipse.console.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;

public class OutputConsole extends TextConsole{
	private final static String CONSOLE_TYPE = OutputConsole.class.getName();
	
	private final OutputConsolePartitioner partitioner;
	
	private volatile TextConsolePage page;
	
	public OutputConsole(String name){
		super(name, CONSOLE_TYPE, null, false);
		
		partitioner = new OutputConsolePartitioner();
	}
	
	public void initializeConsole(){
		// This stinks, but works.
		new Thread(){
			public void run(){
				do{
					Thread.yield();
				}while(page == null);
				
				disableEditing();
				
				Display.getDefault().asyncExec(new Runnable(){
					public void run(){
						IActionBars actionBars = page.getSite().getActionBars();
						IToolBarManager toolBarManager = actionBars.getToolBarManager();
						
						// Removed default stuff.
						IContributionItem[] ci = toolBarManager.getItems();
						for(int i = 0; i < ci.length; i++){
							toolBarManager.remove(ci[i]);
						}
						
						// Add custom stuff.
						toolBarManager.add(new RemoveAction(OutputConsole.this));
						
						actionBars.updateActionBars();

					}
				});
			}
		}.start();
	}
	
	private void disableEditing(){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				page.getViewer().setEditable(false);
			}
		});
	}
	
	public void writeToConsole(final String line){
		final IDocument doc = getDocument();

		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				try{
					doc.replace(doc.getLength(), 0, line);
					
					int endOfDocument = doc.getLength();
					moveCaretTo(endOfDocument);
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}
			}
		});
	}
	
	private void moveCaretTo(int index){
		TextConsoleViewer consoleViewer = page.getViewer();
		StyledText styledText = consoleViewer.getTextWidget();
		
		styledText.setCaretOffset(index);
		consoleViewer.revealRange(index, 0);
	}
	
	private static class RemoveAction extends Action{
		private final OutputConsole console;
		
		public RemoveAction(OutputConsole console){
			super("Remove");
			
			this.console = console;
		}
		
		public void run(){
			OutputConsole.close(console);
		}
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}
	
	private static class OutputConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner{
		
		public OutputConsolePartitioner(){
			super(new RuleBasedPartitionScanner(), new String[]{});
		}

		public StyleRange[] getStyleRanges(int offset, int length){
			return new StyleRange[]{new StyleRange(offset, length, null, null, SWT.NORMAL)};
		}

		public boolean isReadOnly(int offset){
			return false;
		}
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new OutputConsolePage(this, view));
	}
	
	private static class OutputConsolePage extends TextConsolePage{
		private final OutputConsole console;
		
		public OutputConsolePage(OutputConsole console, IConsoleView view){
			super(console, view);
			
			this.console = console;
		}
		
		public TextConsoleViewer createViewer(Composite parent){
			return new TextConsoleViewer(parent, console);
		}
	}
	
	public static OutputConsole open(String name){
		IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
		
		OutputConsole console = new OutputConsole(name);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		
		return console;
	}
	
	public static void close(OutputConsole console){
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
	}
}
