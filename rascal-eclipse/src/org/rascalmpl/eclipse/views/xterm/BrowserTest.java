package org.rascalmpl.eclipse.views.xterm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BrowserTest {
    public static void main(String[] args) {
        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Snippet 128");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        shell.setLayout(gridLayout);
        ToolBar toolbar = new ToolBar(shell, SWT.NONE);
        ToolItem itemBack = new ToolItem(toolbar, SWT.PUSH);
        itemBack.setText("Back");
        ToolItem itemForward = new ToolItem(toolbar, SWT.PUSH);
        itemForward.setText("Forward");
        ToolItem itemStop = new ToolItem(toolbar, SWT.PUSH);
        itemStop.setText("Stop");
        ToolItem itemRefresh = new ToolItem(toolbar, SWT.PUSH);
        itemRefresh.setText("Refresh");
        ToolItem itemGo = new ToolItem(toolbar, SWT.PUSH);
        itemGo.setText("Go");

        GridData data = new GridData();
        data.horizontalSpan = 3;
        toolbar.setLayoutData(data);

        Label labelAddress = new Label(shell, SWT.NONE);
        labelAddress.setText("Address");

        final Text location = new Text(shell, SWT.BORDER);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        location.setLayoutData(data);

        final Browser browser;
        try {
            browser = new Browser(shell, SWT.NONE);
            for (Listener l : browser.getListeners(SWT.KeyDown)) {
                browser.removeListener(SWT.KeyDown, l);
            }
            for (Listener l : browser.getListeners(SWT.KeyUp)) {
                browser.removeListener(SWT.KeyUp, l);
            }
        } catch (SWTError e) {
            System.out.println("Could not instantiate Browser: " + e.getMessage());
            display.dispose();
            return;
        }
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.horizontalSpan = 3;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        browser.setLayoutData(data);

        final Label status = new Label(shell, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        status.setLayoutData(data);

        final ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = GridData.END;
        progressBar.setLayoutData(data);

        /* event handling */
        Listener listener = event -> {
            ToolItem item = (ToolItem) event.widget;
            String string = item.getText();
            if (string.equals("Back"))
                browser.back();
            else if (string.equals("Forward"))
                browser.forward();
            else if (string.equals("Stop"))
                browser.stop();
            else if (string.equals("Refresh"))
                browser.refresh();
            else if (string.equals("Go"))
                browser.setUrl(location.getText());
        };
        browser.addProgressListener(new ProgressListener() {
            @Override
            public void changed(ProgressEvent event) {
                    if (event.total == 0) return;
                    int ratio = event.current * 100 / event.total;
                    progressBar.setSelection(ratio);
            }
            @Override
            public void completed(ProgressEvent event) {
                progressBar.setSelection(0);
            }
        });
        browser.addStatusTextListener(event -> status.setText(event.text));
        browser.addLocationListener(LocationListener.changedAdapter(event -> {
                if (event.top) location.setText(event.location);
            }
        ));
        itemBack.addListener(SWT.Selection, listener);
        itemForward.addListener(SWT.Selection, listener);
        itemStop.addListener(SWT.Selection, listener);
        itemRefresh.addListener(SWT.Selection, listener);
        itemGo.addListener(SWT.Selection, listener);
        location.addListener(SWT.DefaultSelection, e -> browser.setUrl(location.getText()));

        shell.open();
        browser.setUrl("http://localhost:9787/index.html?project=rascal");

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
