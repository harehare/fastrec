const HTMLWebpackPlugin = require("html-webpack-plugin");
const PreloadWebpackPlugin = require("preload-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
    entry: {
        styles: "../../../../styles.scss",
    },
    output: {
        publicPath: "/",
    },
    resolve: {
        extensions: [".js", ".scss", ".css"],
    },
    plugins: [
        new HTMLWebpackPlugin({
            template: "../../../../web/index.html",
            inject: "body",
            inlineSource: ".css$",
            base: "/",
        }),
        new PreloadWebpackPlugin({
            rel: "preload",
            include: ["runtime", "vendors"],
        }),
        new MiniCssExtractPlugin({ filename: "[name]-[hash].css" }),
        new CopyPlugin({
            patterns: [{ from: "../../../../assets/icon.svg", to: "." }],
        }),
    ],
    module: {
        rules: [
            {
                test: /\.(jpe?g|png|gif|svg)$/i,
                use: [
                    {
                        loader: "file-loader",
                        options: {
                            hash: "sha512",
                            digest: "hex",
                            name: "[hash].[ext]",
                        },
                    },
                ],
            },
            {
                test: /\.scss$/,
                exclude: [/elm-stuff/, /node_modules/],
                use: [
                    "style-loader",
                    "css-loader?url=false",
                    {
                        loader: "sass-loader",
                        options: {
                            implementation: require("sass"),
                        },
                    },
                ],
            },
            {
                test: /\.css$/,
                loaders: ["style-loader", "css-loader?url=false"],
            },
            {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                loader: "url-loader",
                options: {
                    limit: 10000,
                },
            },
        ],
    },
};
