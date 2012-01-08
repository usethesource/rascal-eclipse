package org.rascalmpl.eclipse.console.outputconsole;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.widgets.Display;

import static org.rascalmpl.eclipse.console.outputconsole.NewLineIndexes.mod;

public class CircularByteArray extends OutputStream implements StyledTextContent{
	
	static final int DEFAULT_TMP_SIZE = 500;

	ArrayList<TextChangeListener> listeners;
	final byte[] elems;
	int start, end; // the first index to be written
	int nrElements;
	NewLineIndexes newLines;
	
	CircularByteArray(int size){
		elems = new byte[size];
		start = end =  0;
		newLines = new NewLineIndexes(Math.max(100, 100));
//		newLines.append(-1);
		listeners = new ArrayList<TextChangeListener>();
	}
	
	public int size() {
		return mod(end - start, elems.length);
	}
	
	void append(byte[] bytes, int off, int len){
		if (len > elems.length) {
			off += len - elems.length; 
			len = elems.length;
		}
		int nrLinesRemoved = 0;
		int nrCharsRemoved = 0;
		int nrLinesAppended = 0;
		for (int i = off; i < off + len ; i++) {
			if( elems[end] == '\n'){
				newLines.removeHead();
				nrLinesRemoved++;
			}
			elems[end] = bytes[i];
			if(elems[end] == '\n'){
				newLines.append(end);
				nrLinesAppended++;
			}
			end = (end + 1) % elems.length;
			nrElements = Math.min(elems.length, nrElements + 1);
			if(nrElements == elems.length){
				nrCharsRemoved++;
			}
		}
//		if(nrElements == elems.length){
//			start = (newLines.getRelIndex(0) + 1) % elems.length;
//			nrLinesRemoved++;
//			nrCharsRemoved = start - end;
//			newLines.removeHead();
//		}
		String newText = new String(bytes,off,len);
		int oldEnd = nrElements - nrCharsRemoved;
		for(int i = newLines.start ; i != newLines.end ; i= (i + 1) % newLines.elems.length){
			System.err.printf("%d ", newLines.elems[i]);
		}
		System.err.printf("\n");
		notifiyListeners(len, nrLinesRemoved, nrCharsRemoved, nrLinesAppended,
				oldEnd,newText);
	}

	private void notifiyListeners(final int len,final int nrLinesRemoved,
			final int nrCharsRemoved, final int nrLinesAppended, final int oldEnd, final String newText) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if(nrCharsRemoved > 0){
					notifyListenersTextRemovedHead(nrLinesRemoved,nrCharsRemoved);
				}
				notifyListenersTextLinesAppended(nrLinesAppended, len,oldEnd,newText);
				
				notifyListenersTextChanged();
			}
		});
		
	}

	@Override
	public void write(int b) throws IOException {
		append(new byte[]{(byte)b},0,1);
	}

	void notifyListenersTextRemovedHead(int nrLinesRemoved, int nrCharsRemoved){
		for(TextChangeListener listener : listeners){
			TextChangingEvent e = new TextChangingEvent(this);
			e.newLineCount = e.newCharCount = 0;
			e.replaceCharCount = nrCharsRemoved;
			e.replaceLineCount = nrLinesRemoved;
			e.start = 0;
			e.newText = "";
			listener.textChanging(e);
		}
	}
	
	void notifyListenersTextLinesAppended(int nrLinesAppended, int nrCharsAdded, int endIndex,String newText){
		for(TextChangeListener listener : listeners){
			TextChangingEvent e = new TextChangingEvent(this);
			e.replaceCharCount = e.replaceLineCount = 0;
			e.newCharCount = nrCharsAdded;
			e.newLineCount = nrLinesAppended;
			e.start = endIndex ;
			e.newText = newText;
			listener.textChanging(e);
		}
	}
	
	void notifyListenersTextChanged(){
		for(TextChangeListener listener : listeners){
			TextChangedEvent e = new TextChangedEvent(this);
			listener.textChanged(e);
		}
	}

	@Override
	public void addTextChangeListener(TextChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public int getCharCount() {
		return nrElements;
	}
	
	private int getEndOfLine(int lineIndex){
		if(newLines.size() == 0){
			return end;
		}
		if(lineIndex-1 == newLines.size()-1){
			return end;
		} else {
			return newLines.getRelIndex(lineIndex) % elems.length;
		}
	}
	
	private String getString(int start, int len){
		String res;

		if(start + len <= elems.length){
			res= new String(elems,start, len);
		} else {
			int firstLen = elems.length - start;
			res= 	getString(start,firstLen) +
					getString(0, len - firstLen);

		}
		return res;
	}
	
	@Override
	public String getLine(int lineIndex) {
		int startIndex ;
		if(lineIndex != 0){
			startIndex = newLines.getRelIndex(lineIndex-1);
		} else {
			startIndex = start;
		}
		int lineEnd = getEndOfLine(lineIndex);
		int len = mod(lineEnd - startIndex,elems.length);
//		System.out.printf("getLine(%d) = %s\n",lineIndex,getString(startIndex,len));
		return getString(startIndex,len);
	}
	
	private int relToAbsIndex(int relI){
		return mod(start + relI, elems.length);
	}
	
	private int absToRelIndex(int absI){
		return mod(absI -start, elems.length);
	}

	@Override
	public int getLineAtOffset(int offset) {
		int res;
		if(offset <= getEndOfLine(0)){
			res = 0;
		} else {
			res =  newLines.getLineContaining(relToAbsIndex(offset)) ;
		}
//		System.out.printf("getLineAtOffset(%d) = %d\n",offset,res);
		return res;
		
	}

	@Override
	public int getLineCount() {
//		System.out.printf("getLineCount() = %d\n",newLines.size() + 1);
		return newLines.size() + 1;
	}

	@Override
	public String getLineDelimiter() {
		return "\n";
	}

	@Override
	public int getOffsetAtLine(int lineIndex) {
		if(lineIndex == 0){
			return 0;
		} else {
			return absToRelIndex(newLines.getRelIndex(lineIndex -1));
		}
	}

	@Override
	public String getTextRange(int start, int length) {
		return getString(relToAbsIndex(start),length);
	}

	@Override
	public void removeTextChangeListener(TextChangeListener listener) {
		listeners.remove(listener);
		
	}

	@Override
	public void replaceTextRange(int start, int replaceLength, String text) {
		throw new Error("Replace not supported on Output console stream!");
	}

	@Override
	public void setText(String text) {
		throw new Error("Set text not supported on Output console stream!");
	}

}
