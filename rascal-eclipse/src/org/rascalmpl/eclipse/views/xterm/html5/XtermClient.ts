

import { Terminal } from 'xterm';
import { AttachAddon } from 'xterm-addon-attach';

const term = new Terminal();

const urlParams = new URLSearchParams(window.location.search)
const socket = new WebSocket('ws://localhost:' + urlParams.get('socket'));
const attachAddon = new AttachAddon(socket);
term.loadAddon(attachAddon);
