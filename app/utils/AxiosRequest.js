import Axios from 'axios';
import {message} from 'antd';
Axios.defaults = Object.assign(Axios.defaults, {
  // `transformResponse` 在传递给 then/catch 前，允许修改响应数据
  transformResponse: [
    function (data) {
      // 对 data 进行任意转换处理
      return data;
    },
  ],
  // `timeout` 指定请求超时的毫秒数(0 表示无超时时间)
  timeout: 60000,
  withCredentials: true,
});
Axios.defaults.crossDomain = true;
// 添加响应拦截器
Axios.interceptors.response.use(
  function (res) {
    let result = {};
    if (res.headers && res.headers['content-type'].indexOf('application/json') != -1) {
      try {
        result = JSON.parse(res.data);
      } catch (error) {
        result = {};
      }
      if (result.code == 200) {
        //兼容性能平台接口
        console.info('logging: the request success');
      }
      return result;
    } else {
      result = res.data;
      if (res.status == 200) {
        return res;
      } else {
        console.info('logging: the request error');
        message.error(res.statusText);
        return Promise.reject(res.statusText);
      }
    }
  },
  function (error) {
    console.info('Error', error);
    // 对响应错误做点什么
    if (error.response) {
      // 请求已发出，但服务器响应的状态码不在 2xx 范围内
      console.log(error.response.data);
      console.log(error.response.code);
      console.log(error.response.headers);
    } else {
      console.info('Error', error.message);
    }
    message.error('Network error, please check and try again！');
    return Promise.reject(error.message);
  },
);

export default {
  get: (url = '', params = {}, config = {}) => {
    return new Promise((resolve, reject) => {
      Axios({
        method: 'get',
        url: url,
        params: params,
        headers: {},
        ...config,
      })
        .then((res) => {
          resolve(res);
        })
        .catch((err) => {
          console.info(`Server error: ${err}`);
          reject(err);
        });
    });
  },
  post: (url = '', params = {}, config = {}) => {
    const isUrlParam = params && params.isUrlParam;
    Reflect.deleteProperty(params, 'isUrlParam');
    const reqParams = params && isUrlParam ? { params: params } : { data: params };
    return new Promise((resolve, reject) => {
      Axios({
        method: 'post',
        url: url,
        ...reqParams,
        headers: {
          'Content-Type': 'application/json',
        },
        ...config,
      })
        .then((res) => {
          resolve(res);
        })
        .catch((err) => {
          console.info(`Server error: ${err}`);
          reject(err);
        });
    });
  },
  put: (url = '', params = {}, config = {}) => {
    return new Promise((resolve, reject) => {
      Axios({
        method: 'put',
        url: url,
        data: params,
        headers: {},
        ...config,
      })
        .then((res) => {
          resolve(res);
        })
        .catch((err) => {
          console.info(`Server error: ${err}`);
          reject(err);
        });
    });
  },
  delete: (url = '', params = {}, config = {}) => {
    return new Promise((resolve, reject) => {
      Axios({
        method: 'delete',
        url: url,
        data: params,
        ...config,
      })
        .then((res) => {
          resolve(res);
        })
        .catch((err) => {
          console.info(`Server error: ${err}`);
          reject(err);
        });
    });
  },
};
