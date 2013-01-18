package org.rascalmpl.eclipse.console.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.perspective.views.Tutor;

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

	private static final String tutorPrefix = "http://tutor.rascal-mpl.org";
	@Override
	public void linkActivated() {
		try {
			
			if (link.startsWith(tutorPrefix)) {
				Tutor t = (Tutor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(Tutor.ID);
				t.gotoPage(link.substring(tutorPrefix.length()));
			}
			else {
				// open a link in an external browser
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(link));
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
