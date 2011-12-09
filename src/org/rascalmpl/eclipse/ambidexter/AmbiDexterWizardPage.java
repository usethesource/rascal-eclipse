package org.rascalmpl.eclipse.ambidexter;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.AmbiguityDetector.DetectionMethod;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class AmbiDexterWizardPage extends WizardPage {
	private final static String title = "AmbiDexter ambiguity detection";
	private final static String description = "How would you like to have your grammar checked?";
	private Group filterGroup;
	private Group sentenceGenGroup;

	private Combo precision;
	private Combo startSymbol;
	private Button unfoldLayoutCheck;
	private Button unfoldFiniteCheck;
	private Button priorityCheck;
	private Button followCheck;
	private Button rejectCheck;
	private Button sentencegenCheck;
	private Button filterCheck;
	private Spinner minlen;
	private Spinner maxlen;
	private Spinner threads;
	
	public String[] startSymbols;
	
	public AmbiDexterWizardPage() {		
		super(title);

		setTitle(title);
		setDescription(description);
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Composite container1 = new Composite(parent, SWT.NULL);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		container1.setLayout(rowLayout);

		// ============== Setup of composites and groups ============================
				
		Composite generalGroup = new Composite(container1, SWT.NULL);
		GridLayout generalLayout = new GridLayout();
		generalLayout.numColumns = 2;
		generalLayout.horizontalSpacing = 16;
		generalGroup.setLayout(generalLayout);

		filterCheck = createCheckBox(container1, "Filter harmless productions");
		
		filterGroup = new Group(container1, SWT.NULL);
		filterGroup.setText("Filtering settings");
		GridLayout filterLayout = new GridLayout();
		filterLayout.numColumns = 1;
		filterGroup.setLayout(filterLayout);
		
		filterCheck.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				setFilteringEnabled(filterCheck.getSelection());
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {	}
		});

		sentencegenCheck = createCheckBox(container1, "Generate sentences");

		sentenceGenGroup = new Group(container1, SWT.NULL);
		sentenceGenGroup.setText("Sentence generation settings");
		GridLayout senteceGenLayout = new GridLayout();
		senteceGenLayout.numColumns = 2;
		generalLayout.horizontalSpacing = 16;
		sentenceGenGroup.setLayout(senteceGenLayout);

		sentencegenCheck.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				setSentencegenEnabled(sentencegenCheck.getSelection());
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {	}
		});
		
		// ============== General settings ============================
		
		new Label(generalGroup, SWT.NULL).setText("Check non-terminal");
		startSymbol = new Combo(generalGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		startSymbol.setItems(startSymbols);
		startSymbol.select(0);
		
		new Label(generalGroup, SWT.NULL).setText("Accuracy");		
		precision = new Combo(generalGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		precision.add("LR0");
		precision.add("SLR1");
		precision.add("LALR1");
		precision.add("LR1");

		// ============== Filtering settings ============================

		unfoldLayoutCheck = createCheckBox(filterGroup, "Unfold layout and literals");
		unfoldFiniteCheck = createCheckBox(filterGroup, "Unfold non-terminals with finite languages");
		priorityCheck = createCheckBox(filterGroup, "Do priority and associativity");
		followCheck = createCheckBox(filterGroup, "Propagate follow restrictions");
		rejectCheck = createCheckBox(filterGroup, "Do rejects");
		
		// ============== Sentence gen settings ============================

		new Label(sentenceGenGroup, SWT.NULL).setText("Minimum sentence length");
		minlen = new Spinner(sentenceGenGroup, SWT.NULL);
		minlen.setMinimum(0);
		
		new Label(sentenceGenGroup, SWT.NULL).setText("Maximum sentence length");
		maxlen = new Spinner(sentenceGenGroup, SWT.NULL);
		maxlen.setMinimum(0);
		
		new Label(sentenceGenGroup, SWT.NULL).setText("Number of threads");
		threads = new Spinner(sentenceGenGroup, SWT.NULL);
		threads.setMinimum(0);
		
		// ============== set values ============================
		
		//startSymbol.select(0);
		precision.select(1);
		
		filterCheck.setSelection(true);
		unfoldLayoutCheck.setSelection(true);
		unfoldFiniteCheck.setSelection(true);
		priorityCheck.setSelection(true);
		followCheck.setSelection(true);
		rejectCheck.setSelection(true);		
		
		sentencegenCheck.setSelection(true);
		minlen.setSelection(0);
		maxlen.setSelection(1);
		threads.setSelection(2);
		
		
		// ============== wrap up ============================
		
		//container1.pack();
		setControl(container1);
		setPageComplete(true);
	}
	
	private Button createCheckBox(Composite container, String text) {
		Button b = new Button(container, SWT.CHECK);
		b.setText(text);
		return b;
	}
	
	private void setFilteringEnabled(boolean b) {
		filterGroup.setEnabled(b);
		unfoldLayoutCheck.setEnabled(b);
		unfoldFiniteCheck.setEnabled(b);
		priorityCheck.setEnabled(b);
		followCheck.setEnabled(b);
		rejectCheck.setEnabled(b);
	}
	
	private void setSentencegenEnabled(boolean b) {
		sentenceGenGroup.setEnabled(b);
		minlen.setEnabled(b);
		maxlen.setEnabled(b);
		threads.setEnabled(b);
	}
	
	public AmbiDexterConfig getSettings() {
		AmbiDexterConfig cfg = new AmbiDexterConfig();
		
		cfg.alternativeStartSymbol = startSymbol.getText();
		cfg.precision = precision.getSelectionIndex();
		
		if (filterCheck.getSelection()) {
			cfg.filterMethod = DetectionMethod.NU;
			cfg.findHarmlessProductions = true;
			cfg.filterUnmatchedDerivesReduces = true;
			cfg.unfoldLayout = cfg.unfoldLexical = unfoldLayoutCheck.getSelection();
			cfg.unfoldNonRecursiveTails = unfoldFiniteCheck.getSelection();
			cfg.doPriorities = priorityCheck.getSelection();
			cfg.doFollowRestrictions = followCheck.getSelection();
			cfg.doRejects = rejectCheck.getSelection();
		}
		
		if (sentencegenCheck.getSelection()) {
			cfg.derivGenMethod = DetectionMethod.PG;
			cfg.incrementalDerivGen = true;
			cfg.derivGenMinDepth = minlen.getSelection();
			cfg.derivGenMaxDepth = maxlen.getSelection();
			cfg.threads = threads.getSelection();
		}
		
		return cfg;
	}
}
