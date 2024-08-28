import React, {useMemo} from 'react';
import { observer} from 'mobx-react';
import {Modal, Table} from 'antd';
import './home.less';

const ResultDetail = ({store}) => {
  const {resultDetailParams} = store;
  const {open, info = {}} = resultDetailParams;

  const handleCancel = () => {
    store.setResultDetailParams({
      open: false,
    });
  };

  const columns = [
    {
      title: '源数据库',
      dataIndex: 'sourceDBMsg',
      key: 'sourceDBMsg',
      width: '50%',
      onCell: () => {
        return {
          style: {
            maxWidth: '50%',
            wordWrap: 'break-word',
            wordBreak:'break-all',
            overflow: 'hidden',
            whiteSpace:'break-spaces'
            }
          };
      },
    },
    {
      title: '目标数据库',
      dataIndex: 'targetDBMsg',
      key: 'targetDBMsg',
      onCell: () => {
        return {
          style: {
            wordWrap: 'break-word',
            wordBreak:'break-all',
            overflow: 'hidden',
            whiteSpace:'break-spaces'
            }
          };
      },
    },
  ];

  const tableData = useMemo(() => {
    return info.msgList ? info.msgList.map((item, index) => ({id: `${index}`, ...item})) : [];
  }, [info]);

  return (
    <Modal title="请查看" width={'70%'} open={open} onCancel={handleCancel} footer={null} destroyOnClose>
      <Table
        bordered
        title={() => `结论：${info.message}`}
        rowKey={(record)=> `${record.sourceDBMsg}_${record.targetDBMsg}`}
        style={{width: '100%', overflowY: 'auto'}}
        columns={columns}
        dataSource={tableData}
        pagination={false}
      />
    </Modal>
  );
};
export default observer(ResultDetail);
