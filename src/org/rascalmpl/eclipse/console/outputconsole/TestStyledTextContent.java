package org.rascalmpl.eclipse.console.outputconsole;

import java.util.ArrayList;

import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.widgets.Display;

public class TestStyledTextContent  implements StyledTextContent{

	int nrLines;
	int nrChars;
	int off = 0;
	ArrayList<TextChangeListener> listeners;
	
	public TestStyledTextContent() {
		nrLines = 100;
		nrChars = 5;
		listeners = new ArrayList<TextChangeListener>();
	}
	
	@Override
	public void addTextChangeListener(TextChangeListener listener) {
		listeners.add(listener);
	}
	
	public void next(){
		off+=1;
		final StyledTextContent me = this;
		
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					for(TextChangeListener e : listeners){
					TextChangingEvent textDest = new TextChangingEvent(me);
					textDest.newText = "";
					textDest.replaceCharCount = nrChars;
					textDest.replaceLineCount = 1;
					textDest.start = 0;
					TextChangingEvent textAdd = new TextChangingEvent(me);
					textAdd.newText = getLine(getLineCount()-1);
					textAdd.newCharCount = nrChars;
					textAdd.newLineCount = 1;
					textAdd.start = nrLines-1 * nrChars;
					textAdd.replaceCharCount = 0;
					textAdd.replaceLineCount = 0;
					TextChangedEvent tc = new TextChangedEvent(me);
					e.textChanging(textDest);
					e.textChanging(textAdd);
					e.textChanged(tc);
				}
				}
			});
			
	}

	@Override
	public int getCharCount() {
		return nrLines * nrChars; 
	}

	@Override
	public String getLine(int lineIndex) {
		return getTextRange(lineIndex * 5, 5);
		
	}

	@Override
	public int getLineAtOffset(int offset) {
		return offset / nrChars;
	}

	@Override
	public int getLineCount() {
		return nrLines;
	}

	@Override
	public String getLineDelimiter() {
		return "\n";
	}

	@Override
	public int getOffsetAtLine(int lineIndex) {
		return lineIndex*5;
	}

	@Override
	public String getTextRange(int start, int length) {
		StringBuffer b = new StringBuffer();
		for(int i = 0 ; i < length ; i++){
			b.append(i + start + off % 10);
		}
		return b.toString();
	}

	@Override
	public void removeTextChangeListener(TextChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replaceTextRange(int start, int replaceLength, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
		
	}

}
