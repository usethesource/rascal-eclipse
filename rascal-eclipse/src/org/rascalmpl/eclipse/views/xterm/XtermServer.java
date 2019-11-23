package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;

public class XtermServer extends NanoHTTPD {
    IValueFactory vf = ValueFactoryFactory.getValueFactory();
    StringWriter outWriter;
    PrintWriter outPrintWriter;
    StringWriter errWriter;
    PrintWriter errPrintWriter;
    private final int port;

    public XtermServer(int port, ISourceLocation root) throws IOException {
        super(port);
        this.port = port;
    }


    public int getPort() {
        return port;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        try {
            ISourceLocation file = URIUtil.correctLocation("plugin", "rascal_eclipse", uri);
            
            return newChunkedResponse(Status.OK, "text/plain", URIResolverRegistry.getInstance().getInputStream(file));
        } catch (IOException e) {
            return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", uri + " not found.\n" + e);
        }
    }


    public OutputStream getRemoteToTerminalOutputStream() {
        return null;
    }
}
