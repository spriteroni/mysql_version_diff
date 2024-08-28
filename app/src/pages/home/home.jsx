import React, {useEffect, useMemo,} from 'react';
import {inject, observer} from 'mobx-react';
import HomeStore from './home-store';
import './home.less';
import HostShow from './hostShow';
import CheckResult from './checkResult';

const Home = ({globalStore}) => {
  const store = useMemo(() => {
    return new HomeStore();
  }, []);

  useEffect(() => {
    globalStore.setBreadcrumb([{name: '主页'}]);
  }, []);


  return (
    <div className="common_content home_content">
      {!store.resultPageParam.visible ? (
        <HostShow store={store} />
      ) : (
        <CheckResult store={store} />
      )}
    </div>
  );
};
export default inject('globalStore')(observer(Home));
