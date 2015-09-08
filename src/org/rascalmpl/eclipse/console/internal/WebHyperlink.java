package org.rascalmpl.eclipse.console.internal;

import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

public class WebHyperlink implements IHyperlink {

	private String link;

	public WebHyperlink(String link) {
		this.link = link;
	}
	
	
	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}

	@Override
	public void linkActivated() {
    EditorUtil.openWebURI(ValueFactoryFactory.getValueFactory().sourceLocation(URIUtil.assumeCorrect(link)));
	}

}
