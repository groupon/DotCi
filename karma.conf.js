var webpack = require('webpack');
var webpack_config = require('./make-webpack-config.js')(
  { 
    debug: true 
  }
);

module.exports = function (config) {
  config.set({

    browsers: [ process.env.CONTINUOUS_INTEGRATION ? 'Firefox' : 'Chrome' ],

    singleRun: process.env.CONTINUOUS_INTEGRATION === 'true',

    frameworks: [ 'mocha' ],

    files: [
      'tests.webpack.js'
    ],

    preprocessors: {
      'tests.webpack.js': [ 'webpack', 'sourcemap' ]
    },

    reporters: [ 'dots' ],

    webpack: webpack_config,

    webpackServer: {
      noInfo: true
    }

  });
};
