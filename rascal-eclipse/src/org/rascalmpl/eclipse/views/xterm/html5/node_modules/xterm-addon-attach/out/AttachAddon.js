"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class AttachAddon {
    constructor(socket, options) {
        this._disposables = [];
        this._socket = socket;
        this._socket.binaryType = 'arraybuffer';
        this._bidirectional = (options && options.bidirectional === false) ? false : true;
    }
    activate(terminal) {
        this._disposables.push(addSocketListener(this._socket, 'message', ev => {
            const data = ev.data;
            terminal.write(typeof data === 'string' ? data : new Uint8Array(data));
        }));
        if (this._bidirectional) {
            this._disposables.push(terminal.onData(data => this._sendData(data)));
            this._disposables.push(terminal.onBinary(data => this._sendBinary(data)));
        }
        this._disposables.push(addSocketListener(this._socket, 'close', () => this.dispose()));
        this._disposables.push(addSocketListener(this._socket, 'error', () => this.dispose()));
    }
    dispose() {
        this._disposables.forEach(d => d.dispose());
    }
    _sendData(data) {
        if (this._socket.readyState !== 1) {
            return;
        }
        this._socket.send(data);
    }
    _sendBinary(data) {
        if (this._socket.readyState !== 1) {
            return;
        }
        const buffer = new Uint8Array(data.length);
        for (let i = 0; i < data.length; ++i) {
            buffer[i] = data.charCodeAt(i) & 255;
        }
        this._socket.send(buffer);
    }
}
exports.AttachAddon = AttachAddon;
function addSocketListener(socket, type, handler) {
    socket.addEventListener(type, handler);
    return {
        dispose: () => {
            if (!handler) {
                return;
            }
            socket.removeEventListener(type, handler);
        }
    };
}
//# sourceMappingURL=AttachAddon.js.map