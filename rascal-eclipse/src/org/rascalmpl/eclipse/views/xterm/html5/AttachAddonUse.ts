

import { Terminal } from 'xterm';
import { AttachAddon } from 'xterm-addon-attach';

const urlParams = new URLSearchParams(window.location.search)
const term = new Terminal();
const socket = new WebSocket('ws://localhost:' + urlParams.get('socket'));
const attachAddon = new AttachAddon(socket);

// Attach the socket to term
term.loadAddon(attachAddon);
