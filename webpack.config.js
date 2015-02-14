module.exports = {
    entry: "./target/js/app.js",
    output: {
        path: "src/main/webapp/js",
        filename: "dotci.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style!css" },
            { test: /\.js$/, exclude: /node_modules/, loader: '6to5-loader'}
        ]
    }
};
