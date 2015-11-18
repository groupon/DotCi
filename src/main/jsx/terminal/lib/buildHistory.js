'use strict';

exports.__esModule = true;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _buildJs = require('./build.js');

var _buildJs2 = _interopRequireDefault(_buildJs);

var _blessedContrib = require('blessed-contrib');

var _blessedContrib2 = _interopRequireDefault(_blessedContrib);

var _blessed = require('blessed');

var _blessed2 = _interopRequireDefault(_blessed);

var request = require('request');

function buildRow(build) {
  var number = build.number;
  var displayTime = build.displayTime;
  var result = build.result;
  var commit = build.commit;
  var committerName = commit.committerName;
  var message = commit.message;
  var shortSha = commit.shortSha;
  var branch = commit.branch;

  return [number + '', displayTime, result, branch, committerName, message, shortSha];
}
function header(title) {
  return {
    obj: _blessed2['default'].box,
    opts: {
      top: 'center',
      left: 'center',
      width: '100%',
      height: '100%',
      content: title,
      tags: true
    }
  };
}
function buildHistoryWidget(title, url, screen, inputBuilds, onBack) {

  var grid = new _blessedContrib2['default'].grid({ rows: 12, cols: 12, screen: screen });
  //grid.set(row, col, rowSpan, colSpan, obj, opts)
  var headerRow = header(title);
  var map = grid.set(0, 0, 2, 12, headerRow.obj, headerRow.opts);
  var parent = grid.set(2, 0, 12, 12, _blessed2['default'].box, {});

  var builds = [];
  builds.push(['number', 'Ago', 'Result', 'Branch', 'Commiter', 'Message', 'Sha']);
  inputBuilds.forEach(function (build) {
    return builds.push(buildRow(build));
  });
  var table = _blessed2['default'].ListTable({
    parent: parent,
    width: '100%',
    height: '100%',
    border: {
      type: 'line',
      left: true,
      top: true,
      right: false,
      bottom: false
    },
    align: 'center',
    tags: true,
    keys: true,
    vi: true,
    mouse: true,
    style: {
      header: {
        fg: 'blue',
        bold: true
      },
      cell: {
        fg: 'green',
        selected: {
          bg: 'blue'
        }
      }
    },
    rows: builds
  });
  table.on('select', function (item) {
    var number = item.parent.rows[item.parent.selected][0];
    _buildJs2['default'](url, screen, number, onBack);
  });
  table.focus();
}

exports['default'] = function (title, url, screen, onBack) {
  request({
    uri: url + 'appData/info/',
    qs: {
      tree: "buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",
      branchTab: 'All',
      count: 50
    }
  }, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var builds = JSON.parse(body)['builds'];
      buildHistoryWidget(title, url, screen, builds, onBack);
      screen.render();
    }
  });
};

module.exports = exports['default'];