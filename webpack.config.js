module.exports = {
    output: {
        path: "src/main/webapp/js",
        filename: "dotci.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style!css" },
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'babel-loader?experimental'
            }
        ]
    },
    devtool: "#inline-source-map"

};
