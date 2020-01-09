

import { Terminal } from 'xterm';
import { AttachAddon } from 'xterm-addon-attach';
import { FitAddon } from 'xterm-addon-fit';

const term = new Terminal({cursorBlink: true, cursorStyle: 'bar', scrollback: 10000});

const fitAddon = new FitAddon();
term.loadAddon(fitAddon);

const urlParams = new URLSearchParams(window.location.search)
const socket = new WebSocket('ws://localhost:' + urlParams.get('socket'));
const attachAddon = new AttachAddon(socket);
term.loadAddon(attachAddon);

term.open(document.getElementById('xterm-container'));

var viewport = document.querySelector('.xterm-viewport');

/* TODO: get viewport dimensions from URL parameters */
