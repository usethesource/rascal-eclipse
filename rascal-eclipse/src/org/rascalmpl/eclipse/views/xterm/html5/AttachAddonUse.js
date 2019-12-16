"use strict";
exports.__esModule = true;
var xterm_1 = require("xterm");
var xterm_addon_attach_1 = require("xterm-addon-attach");
var term = new xterm_1.Terminal();
var socket = new WebSocket('wss://docker.example.com/containers/mycontainerid/attach/ws');
var attachAddon = new xterm_addon_attach_1.AttachAddon(socket);
// Attach the socket to term
term.loadAddon(attachAddon);
