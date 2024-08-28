import * as React from 'react';
import {Route, Switch, Redirect} from 'react-router-dom';
import {inject, observer} from 'mobx-react';

const RouteGuard = (props) => {
  const {routerConfig, location} = props;
  const {pathname} = location;

  const currentRoute = routerConfig.find((item) => item.path === pathname);
  if (currentRoute) {
    const {path, exact, strict, component, render} = currentRoute;
    return <Route exact={exact} strict={strict} path={path} component={component} render={render} />;
  } else {
    return <Redirect to="/home" />;
  }
};
export default observer(RouteGuard);
