/* eslint-disable no-mixed-spaces-and-tabs */
import React from 'react'
import {Menu} from 'antd'
import {HomeOutlined, TableOutlined, FolderOpenOutlined, FileMarkdownOutlined, SettingOutlined} from '@ant-design/icons'
import PropTypes from 'prop-types'
import {inject, observer} from 'mobx-react'
import {Link} from 'react-router-dom'
import {getPathKey} from '@utils/commonFun'

const MyMenu = ({globalStore}) => {
  const rootMenuKeys = ['home']
  const onClickMenu = async (e) => {
    await globalStore.setSelectKeysValue(e.key)
  }

  const selectedMenuKey = globalStore.MenuSelectKeys
  const selectRootMenu = selectedMenuKey

  const items = [
    // { label: <span className='menutext'>首页</span>, key: 'home', icon: <HomeOutlined />},
    {label: <Link to="/home">首页</Link>, key: 'home'},
  ]

  return (
    <Menu
      items={items}
      theme="light"
      mode="horizontal"
      onClick={onClickMenu}
      selectedKeys={[selectRootMenu]}
      style={{width: 'calc(100% - 10px)'}}
    />
  )
}

export default inject('globalStore')(observer(MyMenu))
