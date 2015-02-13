module.exports = {
    entry: "./main.js",
    output: {
        path: __dirname,
        filename: "dotci.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style!css" }
        ]
    }
};
