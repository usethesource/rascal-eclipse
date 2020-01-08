

import { Terminal } from 'xterm';
import { AttachAddon } from 'xterm-addon-attach';
import { FitAddon } from 'xterm-addon-fit';

const term = new Terminal({cursorStyle: 'block', cursorBlink: true});

const fitAddon = new FitAddon();
term.loadAddon(fitAddon);

const urlParams = new URLSearchParams(window.location.search)
const socket = new WebSocket('ws://localhost:' + urlParams.get('socket'));
const attachAddon = new AttachAddon(socket);
term.loadAddon(attachAddon);
