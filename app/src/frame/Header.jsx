import React, {useEffect} from 'react';
import './frame.less';
import {Layout, Row, Col, Button} from 'antd';
const {Header} = Layout;
import {observer} from 'mobx-react';
import Menu from './Menu';
import Declaration from '@src/components/declaration';
import {setDeclarationFlag, getDeclarationFlag} from '@utils/commonFun';
import logo from '@src/assets/img/logo.png';

const MyHeader = ({globalStore}) => {
  useEffect(() => {
    const flag = getDeclarationFlag();
    if (!flag) {
      setDeclarationFlag();
      globalStore.setDeclarationVisible(true);
    }
  }, []);

  const showDeclaration = () => {
    globalStore.setDeclarationVisible(true);
  };
  const hideDeclaration = () => {
    globalStore.setDeclarationVisible(false);
  };

  return (
    <Header className="root_layout_header">
      <div>
        <a href="/">
          <img src={logo} style={{width: '40px'}} />
        </a>
        <span style={{paddingLeft: '10px'}}>PolarDB MySQL 大版本迁移/RDS迁移兼容性检测工具</span>
      </div>
      {/* <Menu globalStore={globalStore} /> */}
      <div>
        <Button type="primary" style={{marginRight: '10px'}} onClick={showDeclaration} size="small" ghost>
          免责声明
        </Button>
      </div>
      <Declaration visible={globalStore.declarationVisible} onClose={hideDeclaration} />
    </Header>
  );
};

export default observer(MyHeader);
