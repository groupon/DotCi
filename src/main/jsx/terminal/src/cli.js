#!/usr/bin/env node
//--server http://localhost:8080/jenkins  --repo suryagaddipati/DotCi
import terminal from './index.js';
let {server,repo} =  require('minimist')(process.argv.slice(2));
terminal(server,repo);
