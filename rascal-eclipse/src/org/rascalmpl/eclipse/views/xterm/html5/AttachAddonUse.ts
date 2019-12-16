import { Terminal } from 'xterm';
import { AttachAddon } from 'xterm-addon-attach';

const term = new Terminal();
const socket = new WebSocket('wss://docker.example.com/containers/mycontainerid/attach/ws');
const attachAddon = new AttachAddon(socket);

// Attach the socket to term
term.loadAddon(attachAddon);
