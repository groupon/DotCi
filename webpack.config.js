module.exports = {
    entry: "src/main/webapp/js/main.js",
    output: {
        path: "src/main/webapp/js",
        filename: "dotci.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style!css" }
        ]
    }
};
