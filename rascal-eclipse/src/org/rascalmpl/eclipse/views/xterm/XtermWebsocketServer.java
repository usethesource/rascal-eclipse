package org.rascalmpl.eclipse.views.xterm;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class XtermWebsocketServer extends WebSocketServer {
    private Exception error = null;

    public XtermWebsocketServer(int port) {
        super(new InetSocketAddress(port));
        System.err.println("websocket address is " + getAddress());
    }
    
    @Override
    public void onClose(WebSocket ws, int arg1, String arg2, boolean arg3) {
        System.err.println("socket closed");
    }

    @Override
    public void onError(WebSocket ws, Exception e) {
        System.err.println("error in websocket: " + e);
        this.error = e;
    }
    
    public Exception getError() {
        return error;
    }

    @Override
    public void onMessage(WebSocket ws, String msg) {
        System.err.println("websocket says: " + msg);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hs) {
        System.err.println("websocket opened with " + hs);
        ws.send("Hello Xterm.js>");
    }

    @Override
    public void onStart() {
        System.err.println("websocket started");
    }
}
