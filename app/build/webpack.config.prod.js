const path = require('path')
const webpack = require('webpack')
const merge = require('webpack-merge')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin')
const TerserPlugin = require('terser-webpack-plugin')
const {CleanWebpackPlugin} = require('clean-webpack-plugin')
const webpackBaseConfig = require('./webpack.config.base')
const config = require('../config/webpackConfig')

let baseConfig = webpackBaseConfig('production')
let options = {
  sourceMap: false,
}

const getRealPath = (temPath) => {
  return path.resolve(__dirname, temPath)
}

let plugins = [
  new CleanWebpackPlugin({
    root: path.resolve(__dirname, '../'),
    verbose: true,
    dry: false,
    cleanOnceBeforeBuildPatterns: [path.resolve(__dirname, '../dist')],
  }),
  new webpack.DefinePlugin({
    'process.env.NODE_ENV': JSON.stringify('production'),
  }),
  new HtmlWebpackPlugin({
    title: 'HybridCluster',
    template: getRealPath('../index.html'),
    filename: 'index.html',
    hash: true,
    minify: true,
    favicon: path.resolve('favicon.ico'),
  }),
  new MiniCssExtractPlugin({
    filename: '[name].[contenthash].css',
    chunkFilename: '[name].chunk.[contenthash].css',
  }),
  new webpack.optimize.LimitChunkCountPlugin({
    maxChunks: 10, // Must be greater than or equal to one
    minChunkSize: 1000,
  }),
  new webpack.optimize.OccurrenceOrderPlugin(),
  new webpack.HashedModuleIdsPlugin(),
]

module.exports = merge(baseConfig, {
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
              cacheDirectory: false, // When set, the given directory will be used to cache the results of the loader
            },
          },
        ],
      },
      {
        test: /\.js$/,
        include: /[\\/]node_modules[\\/](sql-formatter)[\\/]/,
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
          {
            loader: MiniCssExtractPlugin.loader,
          },
          {
            loader: 'css-loader',
            options: options,
          },
        ],
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
          },
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
    minimizer: [
      new TerserPlugin({
        terserOptions: {
          format: {
            comments: false,
          },
          compress: {
            drop_console: true,
          },
          parse: {
            html5_comments: false,
          },
          sourceMap: false,
        },
        extractComments: false,
      }),
      new OptimizeCssAssetsPlugin(),
    ],
    runtimeChunk: 'single',
    splitChunks: {
      chunks: 'all', // initial all async
      minSize: 30000,
      maxSize: 0,
      minChunks: 1,
      maxAsyncRequests: 10,
      maxInitialRequests: Infinity,
      automaticNameDelimiter: '-',
      name: true,
      cacheGroups: {
        commons: {
          name: 'commons',
          chunks: 'initial',
          minChunks: 2,
        },
        highlight: {
          test: /[\\/]node_modules[\\/](highlight\.js|codemirror)[\\/]/,
          name: 'vender-highlight',
          priority: 2,
        },
        antd: {
          test: /[\\/]node_modules[\\/](@ant-design|antd)[\\/]/,
          name: 'vender-antd',
          priority: 2,
        },
        vendors: {
          // 和 CommonsChunkPlugin 里的 minChunks 非常像，用来决定提取哪些模块
          // 可以接受字符串，正则表达式，或者函数，函数的一个参数是 module，第二个参数为引用这个 module 的 chunk（数组）
          test: /[\\/]node_modules[\\/]/,
          // 优先级高的 Chunk 为被优先选择，优先级一样的话，`size` 大的优先被选择
          priority: 1,
          reuseExistingChunk: true,
        },
      },
    },
  },
  plugins: plugins,
  devtool: false,
});
