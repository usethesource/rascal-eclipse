package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class XtermView extends ViewPart {
    private Browser browser;
    private XtermServer server;
    
    private static final int BASE_PORT = 9787;
    private static final int ATTEMPTS = 100;

    @Override
    public void createPartControl(Composite parent) {
        browser = new Browser(parent, SWT.WEBKIT);
        browser.setText("<html><body>XTerm is now loading: <progress max=\"100\"></progress></body></html>");
        
        for (int port = BASE_PORT; port < BASE_PORT+ATTEMPTS; port++) {
            try {
                server = new XtermServer(port);
                System.err.println("Xterm port: " + port);
                break;
            }
            catch (IOException e) {
                continue;
            }
        }
        
        if (server != null) {
            int p = server.getPort();
            browser.setUrl("http://localhost:" + p + "/index.html?socket=" + p);
        }
        else {
            browser.setText("<html><body>failed to load XTerm server</body></html>");
        }
    }

    @Override
    public void dispose() {
        server.stop();
    }
    
    @Override
    public void setFocus() {
        browser.setFocus();
    }
}
