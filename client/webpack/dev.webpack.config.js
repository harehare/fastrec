const { merge } = require("webpack-merge");
const path = require("path");
const generatedConfig = require("./scalajs.webpack.config");
const commonConfig = require("./common.webpack.config.js");

module.exports = merge(generatedConfig, commonConfig, {
    devServer: {
        inline: true,
        hot: true,
        contentBase: path.join(__dirname, "../../../../assets"),
        historyApiFallback: true,
        compress: false,
    },
});
