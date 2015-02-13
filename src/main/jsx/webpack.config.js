module.exports = {
    entry: "../webapp/js/main.js",
    output: {
        path: "../webapp/js",
        filename: "dotci.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style!css" }
        ]
    }
};
