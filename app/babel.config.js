module.exports = function (api) {
  api.cache(true);

  const presets = [
    [
      '@babel/preset-env',
      {
        useBuiltIns: 'entry',
        corejs: '3.22',
      },
    ],
  ]; // 类似.babelrc.js中的presets
  const plugins = [
    ['@babel/plugin-transform-react-jsx'],
    ['@babel/plugin-proposal-decorators', {version: 'legacy'}],
    ['@babel/plugin-transform-class-properties', {loose: false}],
    [
      'import',
      {
        libraryName: 'antd',
        libraryDirectory: 'lib',
        style: true,
      },
    ],
  ]; // 类似.babelrc.js中的plugins

  return {
    presets,
    plugins,
  };
};
