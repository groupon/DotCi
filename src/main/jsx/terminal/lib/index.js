'use strict';

exports.__esModule = true;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _blessed = require('blessed');

var _blessed2 = _interopRequireDefault(_blessed);

var _buildHistoryJs = require('./buildHistory.js');

var _buildHistoryJs2 = _interopRequireDefault(_buildHistoryJs);

var simpleGit = require('simple-git')('.');

exports['default'] = function (server, repo) {
  if (repo) {
    terminal(server, repo);
  } else {
    simpleGit._run(['config', '--get', 'remote.origin.url'], function (x, repoName) {
      var repoFull = repoName.trim().split(':')[1].split('.git')[0];
      terminal(server, repoFull);
    });
  }
};

function terminal(server, repoName) {
  var org = repoName.split('/')[0];
  var repo = repoName.split('/')[1];
  var url = server + '/job/' + org + '/job/' + repo + '/';
  var screen = getScreen(repoName);
  function onBack() {
    screen.realloc();
    _buildHistoryJs2['default'](repoName, url, screen, onBack);
  }
  _buildHistoryJs2['default'](repoName, url, screen, onBack);
}
function getScreen(title) {
  var screen = _blessed2['default'].screen({
    autoPadding: true,
    smartCSR: true,
    title: title,
    fullUnicode: true,
    dockBorders: true,
    ignoreDockContrast: true
  });
  screen.key(['escape', 'q', 'C-c'], function (ch, key) {
    return process.exit(0);
  });
  return screen;
}
module.exports = exports['default'];