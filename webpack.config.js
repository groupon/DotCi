var AUTOPREFIXER_LOADER = 'autoprefixer-loader?{browsers:[' +
    '"Android 2.3", "Android >= 4", "Chrome >= 20", "Firefox >= 24", ' +
    '"Explorer >= 8", "iOS >= 6", "Opera >= 12", "Safari >= 6"]}';

module.exports = {
    entry: "./src/main/jsx/app.jsx",
    output: {
        path: "src/main/webapp/js",
        filename: "dotci.js"
    },
    module: {
        loaders: [
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
    devtool: "#inline-source-map"

};
