package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.service.DirectRouter;
import org.simpleframework.http.socket.service.Router;
import org.simpleframework.http.socket.service.RouterContainer;
import org.simpleframework.http.socket.service.Service;
import org.simpleframework.transport.connect.SocketConnection;

import io.usethesource.vallang.ISourceLocation;

public class XtermServer {
    private static final int FILE_BUFFER_SIZE = 8 * 1024;
    
    private final int port;
    private final SocketConnection connection;

    public XtermServer(int port) throws IOException {
        this.port = port;
        XtermService xtermService = new XtermService();
        Router router = new DirectRouter(xtermService);
        RouterContainer routerContainer = new RouterContainer(new XtermContainer(), router, 1);
        ContainerSocketProcessor server = new ContainerSocketProcessor(routerContainer, 1);

        connection = new SocketConnection(server);
        connection.connect(new InetSocketAddress(port));
    }
    
    public int getPort() {
        return port;
    }
    
    /** 
     * Handles new terminal connections
     */
    private class XtermService implements Service {
        private QueuedInputStream input;
        private OutputStream output;
        
        @Override
        public void connect(Session session) {
            try {
                this.input = new QueuedInputStream();
                this.output = new SocketOutputStream(session.getChannel());
                session.getChannel().register(new XtermFrameListener(session, input));
                
                System.err.println("Session connection parameters: " + session.getRequest().getAttributes());
                
                // TODO: here we can inject different kinds of connectors
                // TODO: receive project config parameters from session URL
                new RascalXtermConnector().connect(input, output);
            } catch (IOException e) {
                Activator.log("failed to connect xterm server", e);
            }
        }
        
        @Override
        protected void finalize() throws Throwable {
            input.close();
            output.close();
        }
    }
    
    /**
     * This handles input frames on the websocket and queues the input
     * on a QueuedInputStream.
     */
    private class XtermFrameListener implements FrameListener {
        private final QueuedInputStream inputStream;
        
        public XtermFrameListener(Session s, QueuedInputStream queue) {
            this.inputStream = queue;
        }

        @Override
        public void onFrame(Session session, Frame frame) {
            System.err.println("received frame: " + frame.getText());
            System.err.println("type = " + frame.getType());

            switch (frame.getType()) {
            case TEXT:
                byte[] bytes = frame.getText().getBytes();
                inputStream.queue(bytes);
                break;
            case BINARY:
                inputStream.queue(frame.getBinary());
                break;
            case CONTINUATION:
                // TODO: check if this is the right response
                inputStream.queue(frame.getBinary());
                break;
            case CLOSE:
                stop();
                break;
            case PONG:
            case PING:
                // do nothing
            }
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
    }

    /**
     * This handles the HTTP requests to load the HTML5 files into the client.
     */
    private static class XtermContainer implements Container {
        
        @Override
        public void handle(Request request, Response response) {
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
        
        private static void copy(InputStream from, OutputStream to) throws IOException {
            final byte[] buffer = new byte[FILE_BUFFER_SIZE];
            int read;
            while ((read = from.read(buffer, 0, buffer.length)) != -1) {
                to.write(buffer, 0, read);
            }
        }
    }

    public void stop() {
        try {
            connection.close();
        } catch (IOException e) {
            Activator.log("failed to close XTerm connection", e);
        }
    }
    
    /**
     * This wraps a websocket framechannel as an OutputStream 
     */
    private class SocketOutputStream extends OutputStream {
        private final FrameChannel channel;
        private final byte[] singleton = new byte[1];
        
        public SocketOutputStream(FrameChannel frameChannel) {
            this.channel = frameChannel;
        }
        
        @Override
        public void write(int b) throws IOException {
            // TODO check byte conversion
            singleton[0] = (byte) (b & 0xFF);
            channel.send(singleton);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (off == 0 && b.length == len) {
                channel.send(b);
            }
            else {
                // TODO: can we not avoid this copying?
                byte[] buf = new byte[len];
                System.arraycopy(b, off, buf, 0, len);
                channel.send(buf);
            }
        }
    }
    
    /**
     * This wraps a queue of byte[]'s which is asynchronously filled by the 
     * websocket framechannel as an InputStream. The stream is to be read
     * on a different thread.  
     */
    private class QueuedInputStream extends InputStream {
        private final BlockingQueue<byte[]> incomingBytes = new ArrayBlockingQueue<>(128);
        private volatile byte[] currentBlock = null;
        private volatile int consumed = 0;
        private volatile boolean closed = false;
        
        public void queue(byte[] chunk) {
            if (chunk.length > 0) {
                incomingBytes.add(chunk);
            }
        }

        @Override
        public int read() throws IOException {
            if (closed) {
                return -1;
            }
            
            waitForAvailable();
            // TODO: check if maybe & 0xFF is needed
            return currentBlock[consumed++];
        }
        

        private synchronized void waitForAvailable() throws IOException {
            if (closed) {
                throw new IOException("Closed stream");
            }
            if (currentBlock == null || consumed >= currentBlock.length) {
                try {
                    byte[] newBlock;
                    while ((newBlock = incomingBytes.poll(1, TimeUnit.SECONDS)) == null) {
                        if (closed) {
                            throw new IOException("Closed stream");
                        }
                    }
                    currentBlock = newBlock;
                    consumed = 0;
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            waitForAvailable();
            // TODO: check for off by one
            int availableBytes =  Math.min(len, currentBlock.length - consumed);
            System.arraycopy(currentBlock, consumed, b, off, availableBytes);
            consumed += availableBytes;
            return availableBytes;
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
        }
    }
}
