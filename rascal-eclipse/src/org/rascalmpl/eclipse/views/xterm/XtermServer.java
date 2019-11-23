package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.usethesource.vallang.ISourceLocation;

public class XtermServer extends NanoHTTPD {
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
            ISourceLocation file = URIUtil.getChildLocation(URIUtil.correctLocation("plugin", "rascal_eclipse", "org/rascalmpl/eclipse/views/xterm/html5"), uri);
            if (!URIResolverRegistry.getInstance().exists(file)) {
                file = URIUtil.getChildLocation(URIUtil.changePath(file, "target/classes/org/rascalmpl/eclipse/views/xterm/html5"), uri);
            }
            
            return newChunkedResponse(Status.OK, getMimeType(uri), URIResolverRegistry.getInstance().getInputStream(file));
        } catch (IOException | URISyntaxException e) {
            return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", uri + " not found.\n" + e);
        }
    }

    private String getExtension(String uri){
        int n = uri.lastIndexOf(".");
        if(n >= 0){
            return uri.substring(n + 1);
        }
        return "";
    }
    
    private String getMimeType(String uri){
        switch(getExtension(uri)){
            case "css":     return "text/css";
            case "ico":     return "image/x-icon";
            case "html":    return "text/html";
            case "jpeg":    return "image/jpeg";
            case "png":     return "image/png";
            case "txt":     return "text/plain";
            default:        return "text/html"; 
        }
    }
    
    public OutputStream getRemoteToTerminalOutputStream() {
        return null;
    }
}
