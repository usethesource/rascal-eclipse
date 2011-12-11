package org.rascalmpl.eclipse.ambidexter;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.values.ValueFactoryFactory;

public class EditSentenceAction extends Action implements SelectionListener {
	private String sentence;
	private IConstructor nonterminal;

	public EditSentenceAction() {
		setText("Edit");
		setToolTipText("Open an editor for the selected sentence");
	}
	
	@Override
	public void run() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		new ValueUI(vf).text(vf.string(sentence), vf.integer(2));
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		Object selection = event.getSource();
	    
	    if (selection != null && selection instanceof Table) {
	    	Table table = (Table) selection;
	    	for (TableItem item : table.getSelection()) {
	    		String sentence = (String) item.getData("sentence");
	    		IConstructor nonterminal = (IConstructor) item.getData("nonterminal");
	    		System.err.println(sentence);
	    		this.sentence = sentence;
	    		this.nonterminal = nonterminal;
	    	}
	    }
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
		
	}
}
