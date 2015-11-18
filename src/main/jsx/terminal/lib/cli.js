#!/usr/bin/env node

//--server http://localhost:8080/jenkins  --repo suryagaddipati/DotCi
'use strict';

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _indexJs = require('./index.js');

var _indexJs2 = _interopRequireDefault(_indexJs);

var _require = require('minimist')(process.argv.slice(2));

var server = _require.server;
var repo = _require.repo;

_indexJs2['default'](server, repo);