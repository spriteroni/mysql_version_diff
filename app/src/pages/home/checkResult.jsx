import React, {useEffect, useState} from 'react';
import { observer} from 'mobx-react';
import { Space, Button, Tooltip, message, Table} from 'antd';
import {HomeOutlined, CheckCircleTwoTone, CloseCircleTwoTone} from '@ant-design/icons';
import './home.less';
import ResultDetail from './resultDetail';

const CheckResult = ({store}) => {
  const [tableLoading, setTableLoading] = useState(false);
  const [exportLoading, setExportLoading] = useState(false);
  const [tableData, setTableData] = useState([]);
  useEffect(() => {
    const initPage = async() => {
      const {resultPageParam, srcDbHost: srcHost, destDbHost: destHost} = store;
      const checkedList = resultPageParam.checkedList || [];
      const params = {
        sourceDB: {
          host: srcHost.host,
          port: srcHost.port,
          userName: srcHost.username,
          pwd: srcHost.password,
          dbVersion: srcHost.dbVersion,
          dbNames: checkedList,
        },
        targetDB: {
          host: destHost.host,
          port: destHost.port,
          userName: destHost.username,
          pwd: destHost.password,
          dbVersion: destHost.dbVersion,
          dbNames: checkedList,
        },
      };
      setTableLoading(true);
      await store.dbHost_check_all(params);
      setTableLoading(false);
      let resultData = [...store.checkResult];
      setTableData(resultData);
    };
    initPage();
  }, []);

  const columns = [
    {
      title: '检查项',
      dataIndex: 'checkCode',
      key: 'checkCode',
    },
    {
      title: '检查内容',
      dataIndex: 'checkContent',
      key: 'checkContent',
    },
    {
      title: '结果',
      dataIndex: 'sqlState',
      key: 'sqlState',
      render: (_, item) => {
        return (
          <Space key="info">
            <div>
              {item.sqlState === '000000' ? (
                <CheckCircleTwoTone style={{fontSize: '18px'}} twoToneColor="#87d068" />
              ) : (
                <CloseCircleTwoTone style={{fontSize: '18px'}} twoToneColor="#eb2f96" />
              )}
            </div>

            {item.sqlState === '000000' ? (
              <span className="result_detail">&nbsp;</span>
            ) : (
              <a className="result_detail" onClick={() => showResultDetail(item)}>
                详情
              </a>
            )}
          </Space>
        );
      },
    },
  ];

  const exportCheckReport = async() => {
    const param = [...tableData]
    setExportLoading(true);
    await store.export_check_result(param);
    setExportLoading(false);
    if(store.exportFlag){
      message.success('导出成功');
    }
    
  };

  const backHome = () => {
    store.setResultPageParam({visible: false});
  };

  const showResultDetail = (item) => {
    store.setResultDetailParams({open: true, info: item});
  };

  return (
    <>
      <div className="page_header">
        <div className="host_name_wrapper">
          <label className="host_name_label">源数据库: </label>
          {store.srcDbHost ? (
            <Tooltip placement="topLeft" title={store.srcDbHost.host}>
              <span className="host_name">{store.srcDbHost.host}</span>
            </Tooltip>
          ) : (
            <span>未选择</span>
          )}
        </div>

        <div className="host_name_wrapper">
          <label className="host_name_label">目标数据库: </label>
          {store.destDbHost ? (
            <Tooltip placement="topLeft" title={store.destDbHost.host}>
              <span className="host_name">{store.destDbHost.host}</span>
            </Tooltip>
          ) : (
            <span>未选择</span>
          )}
        </div>

        <Button type="primary" loading={exportLoading} disabled={!store.checkFlag} onClick={exportCheckReport}>
          导出检查报告
        </Button>
        <Button icon={<HomeOutlined />} onClick={backHome}>
          返回
        </Button>
      </div>
      <Table
        rowKey={'keyStr'}
        loading={tableLoading}
        style={{width: '100%', overflowY: 'auto'}}
        columns={columns}
        dataSource={tableData}
        pagination={false}
      />
      <ResultDetail store={store} />
    </>
  );
};
export default observer(CheckResult);
