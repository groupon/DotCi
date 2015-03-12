var webpack = require('webpack');
var AUTOPREFIXER_LOADER = 'autoprefixer-loader?{browsers:[' +
    '"Android 2.3", "Android >= 4", "Chrome >= 20", "Firefox >= 24", ' +
    '"Explorer >= 8", "iOS >= 6", "Opera >= 12", "Safari >= 6"]}';
var argv = require('minimist')(process.argv.slice(2));
var DEBUG = !argv.release;
module.exports = {
    entry:{
       dotci:  [
           'webpack-dev-server/client?http://localhost:3000',
           'webpack/hot/only-dev-server',
        "./src/main/jsx/app.jsx"
    ]},
    output: {
        filename: "dotci.js",
        publicPath: "http://localhost:3000/assets/"
    },
    stats: {
        colors: true,
        reasons: DEBUG
    },
    resolve: {
        extensions: ['', '.js', '.jsx']
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
            test: /\.less$/,
            loader: 'style-loader!css-loader!' + AUTOPREFIXER_LOADER + '!less-loader'
          },
          {
            test: /\.css$/,
            loader: 'style-loader!css-loader!' + AUTOPREFIXER_LOADER
          },
          { test: /\.jsx?$/, loaders: ['react-hot', 'babel-loader?experimental'], exclude: /node_modules/ }
        ]
    },
    cache: DEBUG,
    debug: DEBUG,
    devtool: 'eval',
    plugins: DEBUG ? [
        new webpack.NoErrorsPlugin()
    ] : [
            new webpack.optimize.DedupePlugin(),
            new webpack.optimize.UglifyJsPlugin(),
            new webpack.optimize.AggressiveMergingPlugin()
        ]

};
