import React, {useState, useEffect} from 'react';
import { observer} from 'mobx-react';
import {Modal, Checkbox, Divider, Spin, message} from 'antd';
import {getHostFormData} from '@utils/commonFun';
import './home.less';

const DestDBCheck = ({store}) => {
  const [loading, setLoading] = useState(false);
  const {destDbCheckModalParams} = store;
  const {open, srcHost} = destDbCheckModalParams;

  const [checkedList, setCheckedList] = useState([]);
  const [indeterminate, setIndeterminate] = useState(false);
  const [checkAll, setCheckAll] = useState(false);

  useEffect(() => {
    if (open) {
      initPageData();
    }
  }, [open]);

  const initPageData = async () => {
    setLoading(true);
    await getCurrDbList();
    setLoading(false);
  };

  const getCurrDbList = async () => {
    const formData = getHostFormData(srcHost);
    await store.dbHost_get_dbs(formData);
  };

  const onChange = (list) => {
    setCheckedList(list);
    setIndeterminate(!!list.length && list.length < store.currDbList.length);
    setCheckAll(list.length === store.currDbList.length);
  };
  const onCheckAllChange = (e) => {
    setCheckedList(e.target.checked ? store.currDbList : []);
    setIndeterminate(false);
    setCheckAll(e.target.checked);
  };

  const handleOk = async () => {
    if(!checkedList || checkedList.length === 0){
      message.warning('请选择要检测的库！');
      return;
    }
    store.setResultPageParam({visible: true, checkedList});
    store.setDestDbCheckModalParams({
      open: false,
    });
  };

  const handleCancel = () => {
    store.setDestDbCheckModalParams({
      open: false,
    });
  };

  return (
    <Modal
      title="检测目标选择"
      width={700}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      destroyOnClose
    >
      <Spin spinning={loading}>
        <Checkbox indeterminate={indeterminate} onChange={onCheckAllChange} checked={checkAll}>
          Check all
        </Checkbox>
        <Divider />
        <Checkbox.Group options={store.currDbList} value={checkedList} onChange={onChange} />
      </Spin>
    </Modal>
  );
};
export default observer(DestDBCheck);
