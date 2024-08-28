import React from 'react'
import {Route, Switch, Redirect} from 'react-router-dom'
import {inject, observer} from 'mobx-react'
import RouteGuard from './routeGuard'
import Home from '@src/pages/home/home'

const router = [
  {
    path: '/',
    exact: true,
    render: () => <Redirect to="/home" />,
  },
  {
    path: '/home',
    exact: true,
    component: Home,
  },
]

const AppRoute = ({globalStore}) => {
  return (
    <Switch>
      <RouteGuard routerConfig={router} globalStore={globalStore} />
    </Switch>
  )
}
export default inject('globalStore')(observer(AppRoute))
