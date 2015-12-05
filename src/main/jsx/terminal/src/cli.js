#!/usr/bin/env node
//--server http://localhost:8080/jenkins  --repo suryagaddipati/DotCi
import terminal from './index.js';
var program = require('commander');
program
.option('-s, --server <server>', 'Server Url(eg: http://www.myci.com).')
.option('-r, --repo <repo>', 'Repo eg: surya/mycoolapp, defaults to current git repo if not specified.')
.parse(process.argv);

if(!program.server){
  program.help();
}else{
  terminal(program.server,program.repo);
}
