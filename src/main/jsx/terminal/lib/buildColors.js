'use strict';

exports.__esModule = true;
var colors = require('colors/safe');

exports['default'] = function (result) {
  switch (result) {
    case 'SUCCESS':
      return colors.green;
    case 'FAILURE':
      return colors.red;
    case 'ABORTED':
      return colors.grey;
    default:
      return colors.yellow;
  }
};

module.exports = exports['default'];