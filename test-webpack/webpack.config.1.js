const webpack = require('webpack');
const devMode = process.env.NODE_ENV !== 'production';
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const { CheckerPlugin } = require('awesome-typescript-loader')

const HTML_OPTIONS = {
    template: "./index.html",
    minify: {
        collapseWhitespace: true,
        removeAttributeQuotes: true
    }
};

console.log("env-->", process.env.NODE_ENV);

if(devMode){
    module.exports = {
        entry: ['./src/index.tsx'],
        devtool: 'inline-source-map',
        devServer: {
            historyApiFallback: true,
            hot: true,
            contentBase: './dist',
            before: function (app) {
                app.get('/forms/projects', function (req, res) {
                    res.json({
                        forms: {
                            projects: {
                                value: ''
                            }
                        }
                    });
                });
            }
        },
        module: {
            rules: [
                {
                    test: /\.tsx?$/,
                    use: 'awesome-typescript-loader',
                    exclude: /node_modules/
                },
                {
                    test: /\.(sa|sc|c)ss$/,
                    use: [
                        // fallback to style-loader in development
                        MiniCssExtractPlugin.loader,
                        "css-loader",
                        "sass-loader"
                    ]
                },
                { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'file-loader?mimetype=image/svg+xml' },
                { test: /\.woff(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader?mimetype=application/font-woff" },
                { test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader?mimetype=application/font-woff" },
                { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader?mimetype=application/octet-stream" },
                { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader" }
            ]
        },
        resolve: {
            extensions: ['.ts', '.tsx', '.js', '.jsx']
        },
        output: {
            filename: 'bundle.js',
            path: path.resolve(__dirname, 'dist')
        },
        plugins: [
            new CheckerPlugin(),
            new CleanWebpackPlugin(['dist']),
            new HtmlWebpackPlugin(HTML_OPTIONS),
            new webpack.HotModuleReplacementPlugin(),
            new MiniCssExtractPlugin({
                // Options similar to the same options in webpackOptions.output
                // both options are optional
                filename: "[name].css",
                chunkFilename: "[id].css"
            })
        ]
    };
}

