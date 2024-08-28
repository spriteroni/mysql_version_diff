const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const config = require('../config/webpackConfig');
const webpackBaseConfig = require('./webpack.config.base');

let baseConfig = webpackBaseConfig('development');
let options = {
  sourceMap: true,
};
const getRealPath = (temPath) => {
  return path.resolve(__dirname, temPath);
};

module.exports = merge(baseConfig, {
  devServer: {
    static: {
      directory: path.resolve(__dirname, '../dist'),
    },
    port: config.dev.port,
    open: config.dev.openBrowser,
    historyApiFallback: true,

    // proxy: {
    //   '/outline': {
    //     target: 'http://30.205.78.142:8080/outline',
    //     ws: false,
    //     secure: false,
    //     changeOrigin: true,
    //     pathRewrite: {'^/outline': ''},
    //   },
    // },
  },
  devtool: 'cheap-module-source-map',
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: [
          'thread-loader',
          {
            loader: 'babel-loader',
            options: {
              cacheDirectory: true, // When set, the given directory will be used to cache the results of the loader
            },
          },
        ],
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
            options: options,
          },
        ],
      },
      {
        test: /\.less$/,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
            options: options,
          },
          {
            loader: 'less-loader',
            options: {
              lessOptions: {
                paths: [path.resolve(__dirname, 'node_modules')],
                javascriptEnabled: true,
                modifyVars: config.antdThemeConfig || {}, // 改变antd的默认颜色
              },
              sourceMap: options.sourceMap,
            },
          },
        ],
      },
    ],
  },
  optimization: {
    noEmitOnErrors: true,
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('development'), // 一定要用json.stringify，如果是单引号的'development',不正确，是定义不了process.env.NODE_ENV的
    }),
    new HtmlWebpackPlugin({
      title: 'HybridCluster',
      template: getRealPath('../index.html'),
      filename: 'index.html',
      favicon: path.resolve('favicon.ico'),
      hash: true,
    }),
  ],
});
