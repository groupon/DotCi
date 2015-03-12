var webpack = require('webpack');
var AUTOPREFIXER_LOADER = 'autoprefixer-loader?{browsers:[' +
    '"Android 2.3", "Android >= 4", "Chrome >= 20", "Firefox >= 24", ' +
    '"Explorer >= 8", "iOS >= 6", "Opera >= 12", "Safari >= 6"]}';
var argv = require('minimist')(process.argv.slice(2));
var DEBUG = !argv.release;
module.exports = {
    entry: "./src/main/jsx/app.jsx",
    output: {
        path: "src/main/webapp/js",
        filename: "dotci.js"
    },
    stats: {
        colors: true,
        reasons: DEBUG
    },
    module: {
        preLoaders: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                loader: 'jshint'
            }
        ],
        loaders: [
          {
            test: [
              /BuildMetrics\.jsx$/ 
            ],
            loader: "react-proxy"
          },

            {
                test: /\.less$/,
                loader: 'style-loader!css-loader!' + AUTOPREFIXER_LOADER + '!less-loader'
            },
            {
                test: /\.css$/,
                loader: 'style-loader!css-loader!' + AUTOPREFIXER_LOADER
            },
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'babel-loader?experimental'
            }
        ]
    },
    cache: DEBUG,
    debug: DEBUG,
    devtool: DEBUG ? '#inline-source-map' : false,
    plugins: DEBUG ? [] : [
            new webpack.optimize.DedupePlugin(),
            new webpack.optimize.UglifyJsPlugin(),
            new webpack.optimize.AggressiveMergingPlugin()
        ]

};
