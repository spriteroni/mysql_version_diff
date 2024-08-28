import React from 'react'
import {Layout} from 'antd'
import {inject, observer} from 'mobx-react'
import MyHeader from './Header'

import AppRoute from '@src/route/index'

const {Content} = Layout

const Frame = ({globalStore}) => {
  return (
    <Layout className="root_layout">
      <MyHeader globalStore={globalStore} />
      <Layout>
        <Content id="main_content" className='main_content'>
          <div className="content">
            <AppRoute />
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}

export default inject('globalStore')(observer(Frame))
