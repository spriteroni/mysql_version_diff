import React, {useEffect, useState} from 'react';
import { observer} from 'mobx-react';
import {Modal, Form, Input, Button, message} from 'antd';
import './home.less';
import {ACTIONS, getHostFormData} from '@utils/commonFun';

const formObj = {
  host: undefined,
  port: undefined,
  username: undefined,
  password: undefined,
}

const NewDatabase = ({store}) => {
  const [form] = Form.useForm();
  const [testBtnLoading, setTestBtnLoading] = useState(false);
  const [btnLoading, setBtnLoading] = useState(false);
  const {editModalParams} = store;
  const {open, action, initValues = {}} = editModalParams;
  useEffect(() => {
    if (open) {
      if (action === ACTIONS.ADD) {
        form.setFieldsValue(formObj);
      } else {
        const tmpValues = {
          ...initValues,
        };
        Reflect.deleteProperty(tmpValues, 'id');
        Reflect.deleteProperty(tmpValues, 'checked');
        form.setFieldsValue({
          ...tmpValues,
        });
      }
    }
  }, [open]);

  const handleOk = () => {
    form
      .validateFields()
      .then(async (values) => {
        if (action === ACTIONS.ADD) {
          const formData = getHostFormData(values);
          setBtnLoading(true);
          await store.dbHost_add(formData);
          setBtnLoading(false);
          if (store.connFlag) {
            const flag = store.addDbHost({...values, ...store.dbHostExtraInfo});
            if (flag) {
              store.getDbHostList();
              store.setEditModalParams({
                open: false,
              });
            } else {
              message.error('添加失败, 请重试！');
            }
          } else {
            message.error('连接失败');
          }
        } else {
          const formData = getHostFormData(values);
          setBtnLoading(true);
          await store.dbHost_add(formData);
          setBtnLoading(false);
          if (store.connFlag) {
            const flag = store.editDbHost({...initValues, ...values, ...store.dbHostExtraInfo});
            if (flag) {
              store.getDbHostList();
              store.setEditModalParams({
                open: false,
              });
            } else {
              message.error('编辑失败, 请重试！');
            }
          } else {
            message.error('连接失败');
          }
        }
      })
      .catch((info) => {
        console.log('Validate Failed:', info);
      });
  };

  const handleCancel = () => {
    store.setEditModalParams({
      open: false,
    });
  };

  const handleTestLink = async () => {
    form
      .validateFields()
      .then(async (values) => {
        const formData = getHostFormData(values);
        setTestBtnLoading(true);
        await store.dbHost_conn_check(formData);
        setTestBtnLoading(false);
        if (store.connFlag) {
          message.success('连接成功');
        } else {
          message.error('连接失败');
        }
      })
      .catch((info) => {
        console.log('Validate Failed:', info);
      });
  };
  return (
    <Modal
      title={action === ACTIONS.ADD ? '新增' : '编辑'}
      width={700}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      footer={[
        <Button key="back" loading={testBtnLoading} onClick={handleTestLink}>
          测试连接
        </Button>,
        <Button key="submit" type="primary" loading={btnLoading} onClick={handleOk}>
          {action === ACTIONS.ADD ? '添加' : '确认'}
        </Button>,
      ]}
      destroyOnClose
    >
      <Form
        form={form}
        name="dbInfo"
        labelCol={{
          span: 5,
        }}
        wrapperCol={{
          span: 18,
        }}
        initialValues={initValues}
      >
        <Form.Item
          label="数据库连接地址"
          name="host"
          rules={[
            {
              required: true,
              message: 'Please input',
            },
          ]}
        >
          <Input placeholder="eg: mysql.polardb.rds.aliyuncs.com" />
        </Form.Item>
        <Form.Item
          label="端口号"
          name="port"
          rules={[
            {
              required: true,
              message: 'Please input',
            },
          ]}
        >
          <Input placeholder="eg: 3306" />
        </Form.Item>
        <Form.Item
          label="用户名"
          name="username"
          rules={[
            {
              required: true,
              message: 'Please input',
            },
          ]}
        >
          <Input placeholder="Please input" />
        </Form.Item>

        <Form.Item
          label="密码"
          name="password"
          rules={[
            {
              required: true,
              message: 'Please input',
            },
          ]}
        >
          <Input.Password placeholder="Please input" />
        </Form.Item>
      </Form>
    </Modal>
  );
};
export default observer(NewDatabase);
