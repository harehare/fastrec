const { merge } = require("webpack-merge");
const generatedConfig = require("./scalajs.webpack.config");
const commonConfig = require("./common.webpack.config.js");
const WorkboxWebpackPlugin = require("workbox-webpack-plugin");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");

module.exports = merge(generatedConfig, commonConfig, {
    plugins: [
        new WorkboxWebpackPlugin.GenerateSW({
            swDest: `${__dirname}/sw.js`,
            clientsClaim: true,
            skipWaiting: true,
        }),
    ],
});
