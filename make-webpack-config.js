/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
var webpack = require('webpack');
var postcss_nested = require('postcss-nested');
module.exports = function(config){
return {
    entry: config.entry,
    output: config.output,
    module: {
        loaders: [
            {
                  test: /\.(json)$/,
                  loader: 'json-loader'
            },
            {
                  test: /\.(svg)$/,
                  loader: 'url-loader'
            },
            {
                test: /\.less$/,
                loader: 'style-loader!css-loader!less-loader'
            },
            {
                test: /\.css$/,
                loader: 'style-loader!css-loader!postcss-loader!cssnext-loader' 
            },
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loaders: config.debug? ['react-hot', 'babel-loader']: ['babel-loader']
            },
            { test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "url-loader?limit=10000&minetype=application/font-woff" },
            { test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "file-loader" }
        ]
    },
    postcss: {
      defaults: [postcss_nested]
    },
    devtool: config.debug ? '#inline-source-map' : false,
    plugins: config.debug ? [] : [
            new webpack.EnvironmentPlugin('NODE_ENV'),
            new webpack.optimize.DedupePlugin(),
            // new webpack.optimize.UglifyJsPlugin(),
            new webpack.optimize.AggressiveMergingPlugin()
        ]

};
};
