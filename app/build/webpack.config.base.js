const path = require('path');
const config = require('../config/webpackConfig');

const chalk = require('chalk');

const getRealPath = (temPath) => {
  return path.resolve(__dirname, temPath);
};

module.exports = (env = 'production') => {
  // env = 'production' || 'development'
  const isProd = env === 'production';
  console.info(chalk.yellow(`logging: env = ${env}`));

  let configuration = {
    // entry: ['babel-polyfill', getRealPath('../src/main.js')],
    entry: getRealPath('../src/main.js'),
    output: {
      path: getRealPath('../dist'),
      filename: isProd ? '[name].[contenthash].js' : '[name].js',
      publicPath: !isProd ? config.dev.publicPath : config.prod.publicPath,
    },
    module: {
      rules: [
        {
          test: /\.(png|svg|jpg|gif|woff|woff2|eot|ttf|otf)$/,
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: '1024',
                name: '[name].[ext]',
                outputPath: 'img/',
              },
            },
          ],
        },
      ],
    },
    resolve: {
      extensions: ['.js', '.jsx', '.json'],
      alias: {
        '@src': path.resolve(__dirname, '../src'),
        '@utils': path.resolve(__dirname, '../utils'),
      },
    },
    plugins: [],
    performance: {
      hints: 'warning', //入口起点的最大体积
      maxEntrypointSize: 50000000,
      maxAssetSize: 50000000,
      assetFilter: function (assetFilename) {
        return assetFilename.endsWith('.js');
      },
    },
  };
  return configuration;
};
