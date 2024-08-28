import React, {useEffect, useState} from 'react';
import {inject, observer} from 'mobx-react';
import {Card, Select, Checkbox, Space, Button, List, Tooltip, Popconfirm, message, Table} from 'antd';
import {EditOutlined, QuestionCircleOutlined, DeleteOutlined} from '@ant-design/icons';
import {ACTIONS} from '@utils/commonFun';
import dbIcon from '@src/assets/img/databaseIcon.png';
import './home.less';
import NewDbHost from './newDbHost';
import DestDBCheck from './destDBCheck';

const SRC = 'source';
const DEST = 'destination';

const GT = 'GT'; // 'LT', 'EQ';

const HostShow = ({store}) => {
  const [compareCheckLoading, setCompareCheckLoading] = useState(false);

  useEffect(() => {
    store.getDbHostList();
  }, []);

  const addDbHost = () => {
    store.setEditModalParams({open: true, action: ACTIONS.ADD});
  };

  const editDbHost = (item) => {
    const initialValues = {...item};
    store.setEditModalParams({open: true, action: ACTIONS.EDIT, initValues: initialValues});
  };

  const onDbHostChange = (value, keyStr) => {
    if (keyStr === SRC) {
      store.setSrcDbHostId(value);
    } else if (keyStr === DEST) {
      store.setDestDbHostId(value);
    }
  };

  const ondbCheck = (e, item) => {
    const checked = e.target.checked;
    if (checked) {
      const ids = store.checkedIdList.includes(item.id) ? store.checkedIdList : [...store.checkedIdList, item.id];
      store.setCheckedIdList(ids);
    } else {
      const ids = store.checkedIdList.filter((id) => id !== item.id);
      store.setCheckedIdList(ids);
    }
  };

  const delDbHost = (id) => {
    store.delDbHost(id);
    store.getDbHostList();
  };

  const delCheckedDbHost = () => {
    const checkedids = store.checkedIdList;
    store.delDbHosts(checkedids);
    store.getDbHostList();
  };

  const showDestDbCheck = async () => {
    if (store.srcDbHostId && store.destDbHostId) {
      const srcVrtsion = store.srcDbHost ? store.srcDbHost.dbVersion : '';
      const destVersion = store.destDbHost ? store.destDbHost.dbVersion : '';
      setCompareCheckLoading(true);
      await store.dbHost_version_compare({isUrlParam: true, sourceVersion: srcVrtsion, targetVersion: destVersion});
      setCompareCheckLoading(false);
      const {message: compareFlag} = store.versionCompareResult;
      if (compareFlag) {
        if (GT === compareFlag.toUpperCase()) {
          store.setDestDbCheckModalParams({open: true, srcHost: store.srcDbHost, destHost: store.destDbHost});
        } else {
          message.warning('源数据库版本应小于目标数据库版本！');
        }
      }
    } else {
      message.warning('源数据库或目标数据库不能为空！');
    }
  };

  return (
    <>
      <Card bordered={false}>
        <Space size={70} wrap={true}>
          <Space size={8}>
            <label>源数据库: </label>
            <Select
              allowClear
              showSearch
              style={{width: 250}}
              placeholder="Select a host"
              optionFilterProp="children"
              value={store.srcDbHostId}
              onChange={(val) => onDbHostChange(val, SRC)}
              filterOption={(input, option) => option.label.toLowerCase().includes(input.toLowerCase())}
              options={store.filted_srcDbHostList.map((item) => ({
                value: item.id,
                label: (
                  <Tooltip placement="topLeft" title={item.host}>
                    {item.host}
                  </Tooltip>
                ),
              }))}
            />
          </Space>

          <Space size={8}>
            <label>目标数据库: </label>
            <Select
              allowClear
              showSearch
              style={{width: 250}}
              placeholder="Select a host"
              optionFilterProp="children"
              value={store.destDbHostId}
              onChange={(val) => onDbHostChange(val, DEST)}
              filterOption={(input, option) => option.label.toLowerCase().includes(input.toLowerCase())}
              options={store.checked_destDbHostList.map((item) => ({
                value: item.id,
                label: (
                  <Tooltip placement="topLeft" title={item.host}>
                    {item.host}
                  </Tooltip>
                ),
              }))}
            />
          </Space>
          <Button type="primary" loading={compareCheckLoading} onClick={showDestDbCheck}>
            兼容性检查
          </Button>
        </Space>
      </Card>
      <div style={{margin: '20px 0'}}>
        <Space size={15}>
          <Button type="primary" onClick={addDbHost}>
            新增库
          </Button>

          {store.checkedIdList.length ? (
            <Popconfirm
              title={`确认删除已选的${store.checkedIdList.length}个数据库？`}
              icon={
                <QuestionCircleOutlined
                  style={{
                    color: 'red',
                  }}
                />
              }
              onConfirm={delCheckedDbHost}
            >
              <Button type="danger">删除库</Button>
            </Popconfirm>
          ) : (
            <Button type="danger" disabled>
              删除库
            </Button>
          )}
        </Space>
      </div>
      <Card style={{width: '100%'}} bordered={false}>
        <List
          grid={{
            gutter: 16,
            xs: 3,
            sm: 4,
            md: 6,
            lg: 8,
            xl: 10,
            xxl: 15,
          }}
          dataSource={store.dbHostList}
          renderItem={(item) => (
            <List.Item
            // onClick={() => onSelect(item)}
            // className={selectedMark == item.id ? 'db_item db_item_selected' : 'db_item'}
            >
              <div className="host_item">
                <Checkbox className="host_check" onChange={(e) => ondbCheck(e, item)} />
                <Card
                  size="small"
                  bordered={false}
                  cover={<img src={dbIcon} style={{width: '70%', margin: '0 auto'}} />}
                  actions={[
                    <a key="edit" onClick={() => editDbHost(item)}>
                      <EditOutlined />
                    </a>,
                    <Popconfirm
                      key="delete"
                      title={`确认删除 ${item.host}？`}
                      icon={
                        <QuestionCircleOutlined
                          style={{
                            color: 'red',
                          }}
                        />
                      }
                      onConfirm={() => delDbHost(item.id)}
                    >
                      <DeleteOutlined />
                    </Popconfirm>,
                  ]}
                >
                  <Card.Meta
                    title={
                      <Tooltip placement="topLeft" title={`${item.dbType}-${item.dbVersion}-${item.host}`}>
                        {item.host}
                      </Tooltip>
                    }
                  />
                </Card>
              </div>
            </List.Item>
          )}
        />
      </Card>
      <NewDbHost store={store} />
      <DestDBCheck store={store} />
    </>
  );
};
export default observer(HostShow);
