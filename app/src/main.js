import React, {StrictMode} from 'react';
import * as ReactDom from 'react-dom';
import {Provider} from 'mobx-react';
import {configure} from 'mobx';
import GlobalStore from '@src/store/globalStore';

// 默认语言为 en-US，如果你需要设置其他语言，推荐在入口文件全局设置 locale
import zh_CN from 'antd/es/locale/zh_CN';
import {ConfigProvider} from 'antd';
import moment from 'moment';
import 'moment/locale/zh-cn';
moment.locale('zh-cn');
import {HashRouter as Router} from 'react-router-dom';

configure({enforceActions: process.env.NODE_ENV === 'development' ? 'always' : 'never'}); // 关闭严格模式
import '@src/assets/style/common.less';
import Frame from '@src/frame';

const globalStore = new GlobalStore();
const stores = {globalStore};

const App = () => {
  return (
    <Router>
      <Frame />
    </Router>
  );
};

ReactDom.render(
  <StrictMode>
    <ConfigProvider locale={zh_CN}>
      <Provider {...stores}>
        <App />
      </Provider>
    </ConfigProvider>
  </StrictMode>,
  document.getElementById('root'),
);
