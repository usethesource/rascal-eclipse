package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.service.DirectRouter;
import org.simpleframework.http.socket.service.Router;
import org.simpleframework.http.socket.service.RouterContainer;
import org.simpleframework.http.socket.service.Service;
import org.simpleframework.transport.connect.SocketConnection;

import io.usethesource.vallang.ISourceLocation;

public class XtermServer implements Container, Service, FrameListener {
    private static final int FILE_BUFFER_SIZE = 8 * 1024;
    private final int port;
    private final SocketConnection socket;
    private Session session;

    public XtermServer(int port, ISourceLocation root) throws IOException {
        this.port = port; 
        
        Router websocketRouter = new DirectRouter(this);
        RouterContainer routerContainer = new RouterContainer(this, websocketRouter, 1);

        ContainerSocketProcessor server = new ContainerSocketProcessor(routerContainer, 1);
        
        socket = new SocketConnection(server);
        socket.connect(new InetSocketAddress(port));
    }

    public int getPort() {
        return port;
    }

    
    public OutputStream getRemoteToTerminalOutputStream() {
        // TODO this is a stub
        
        if (session != null) {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    session.getChannel().send(new byte[(byte) b]);
                }
            };
        }
        
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("there is no open XTerm connection");
            }
        };
    }

    @Override
    public void onClose(Session arg0, Reason arg1) {
        System.err.println("closed the websocket: " + arg0);
        stop();
    }

    @Override
    public void onError(Session arg0, Exception e) {
        Activator.log("error in XTerm server: " + e.getMessage(), e);
    }

    @Override
    public void onFrame(Session session, Frame frame) {
        System.err.println("received frame: " + frame.getText());
        System.err.println("type = " + frame.getType());
        // echo it back
        try {
            session.getChannel().send(frame.getText());
        } catch (IOException e) {
            Activator.log("failed to echo to Xterm", e);
        }

    }

    @Override
    public void connect(Session session) {
        // websocket
        System.err.println("connecting websocket: " + session);

        try {
            session.getChannel().register(this);
            this.session = session;
        } catch (IOException e) {
            Activator.log("failed to connect xterm server", e);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        // HTTP

      
        try {
            ISourceLocation file = URIUtil.getChildLocation(URIUtil.correctLocation("plugin", "rascal_eclipse", "org/rascalmpl/eclipse/views/xterm/html5"), request.getAddress().getPath().getPath());
            if (!URIResolverRegistry.getInstance().exists(file)) {
                file = URIUtil.getChildLocation(URIUtil.changePath(file, "target/classes/org/rascalmpl/eclipse/views/xterm/html5"), request.getAddress().getPath().getPath());
            }

            response.setContentType(getMimeType(request.getAddress().getPath().getExtension()));
            copy(URIResolverRegistry.getInstance().getInputStream(file), response.getOutputStream());
            response.close();

        } catch (IOException | URISyntaxException e) {
            Activator.log("XTerm server request failed", e);
        } 
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        final byte[] buffer = new byte[FILE_BUFFER_SIZE];
        int read;
        while ((read = from.read(buffer, 0, buffer.length)) != -1) {
            to.write(buffer, 0, read);
        }
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            Activator.log("failed to close XTerm socket", e);
        }
    }
    
    private String getMimeType(String ext){
        switch(ext){
        case "css":     return "text/css";
        case "ico":     return "image/x-icon";
        case "html":    return "text/html";
        case "jpeg":    return "image/jpeg";
        case "png":     return "image/png";
        case "txt":     return "text/plain";
        default:        return "text/html"; 
        }
    }

}
