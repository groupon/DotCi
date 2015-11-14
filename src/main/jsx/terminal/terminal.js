import React, {Component} from 'react';
import blessed from 'blessed';
import buildHistoryTable from './buildHistory.js';
var simpleGit = require('simple-git')('.');
export default function(server,repo){
  if(repo){
    terminal(server,repo);
  }else{
    simpleGit._run(['config', '--get', 'remote.origin.url'],function(x,repoName){
      const repoFull = repoName.trim().split(':')[1].split('.git')[0];
      terminal(server,repoFull);
    })
  }

}
function terminal(server,repoName){
  const org =  repoName.split('/')[0];
  const repo =  repoName.split('/')[1];
  const url =`${server}/job/${org}/job/${repo}/`;
  let screen = getScreen(repoName);
  function onBack(){
    screen.realloc();
    buildHistoryTable(repoName,url,screen,onBack);
  }
  buildHistoryTable(repoName,url,screen,onBack);
}
function getScreen(title){
  const screen = blessed.screen({
    autoPadding: true,
    smartCSR: true,
    title,
    fullUnicode: true,
    dockBorders: true,
    ignoreDockContrast: true
  });
  screen.key(['escape', 'q', 'C-c'], function(ch, key) {
    return process.exit(0);
  });
  return screen
}
