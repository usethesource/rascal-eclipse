"use strict";
exports.__esModule = true;
var xterm_1 = require("xterm");
var xterm_addon_attach_1 = require("xterm-addon-attach");
var urlParams = new URLSearchParams(window.location.search);
var term = new xterm_1.Terminal();
var socket = new WebSocket('ws://localhost:' + urlParams.get('socket'));
var attachAddon = new xterm_addon_attach_1.AttachAddon(socket);
// Attach the socket to term
term.loadAddon(attachAddon);
term.open(document.getElementById('xterm-container'));
