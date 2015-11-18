'use strict';

exports.__esModule = true;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var _blessed = require('blessed');

var _blessed2 = _interopRequireDefault(_blessed);

var request = require('request');

function buildWidget(onBack) {
  var build = _blessed2['default'].form({
    mouse: true,
    border: {
      type: 'line'
    },
    style: {
      fg: 'black',
      // bg: 'white',
      border: {
        fg: 'blue'
      }
    }
  });
  var backButton = _blessed2['default'].button({
    parent: build,
    height: 1,
    width: 15,
    content: '<--- Back',
    align: 'center',
    bg: 'blue',
    hoverBg: 'green',
    mouse: true
  });
  backButton.on('press', onBack);
  return build;
}
function logWidget(parent) {
  return _blessed2['default'].log({
    scrollable: true,
    parent: parent,
    top: 2,
    alwaysScroll: true,
    tags: true,
    keys: true,
    vi: true,
    mouse: true,
    border: {
      type: 'line'
    },
    style: {
      fg: 'green',
      bg: 'black',
      border: {
        fg: 'blue'
      },
      scrollbar: {
        bg: 'blue'
      }
    }
  });
}

exports['default'] = function (url, screen, buildNumber, onBack) {
  request({
    uri: url + buildNumber + '/logTail'
  }, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var buildScreen = buildWidget(onBack);
      var logScreen = logWidget(buildScreen);
      logScreen.setContent(body);
      screen.append(buildScreen);
      screen.render();
    }
  });
};

module.exports = exports['default'];