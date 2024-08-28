import {makeAutoObservable, runInAction} from 'mobx';
import {message} from 'antd';
import {DB_LIST_KEY, encode, storage} from '@utils/commonFun';
import {v4 as uuidv4} from 'uuid';
import AxiosRequest from '@utils/AxiosRequest';
import {
  DBHOST_CHECK_OR_ADD_URL,
  DBHOST_GET_DBS_URL,
  DBHOST_VERSION_COMPARE_URL,
  DBHOST_CHECK_ALL,
  DBHOST_CHECK_RESULT_EXPORT,
} from '@src/api';

class HomeStore {
  constructor() {
    makeAutoObservable(this);
  }

  loading = false;

  editModalParams = {open: false};

  destDbCheckModalParams = {open: false};

  resultDetailParams = {open: false};

  dbHostList = [];

  connFlag = false;

  dbHostExtraInfo = {};

  currDbList = [];

  versionCompareResult = {};

  get filted_srcDbHostList() {
    return this.dbHostList.filter((item) => this.destDbHostId !== item.id);
  }

  get checked_destDbHostList() {
    return this.dbHostList.filter((item) => this.srcDbHostId !== item.id);
  }

  checkedIdList = [];

  srcDbHostId = undefined;

  get srcDbHost() {
    return this.srcDbHostId ? this.dbHostList.find((item) => item.id === this.srcDbHostId) : undefined;
  }

  destDbHostId = undefined;

  get destDbHost() {
    return this.destDbHostId ? this.dbHostList.find((item) => item.id === this.destDbHostId) : undefined;
  }

  resultPageParam = {visible: false};

  checkFlag = false;

  checkResult = [];

  exportFlag = false;

  setLoading(loading) {
    this.loading = loading;
  }

  setEditModalParams(params) {
    this.editModalParams = params;
  }

  setDestDbCheckModalParams(params) {
    this.destDbCheckModalParams = params;
  }

  setResultDetailParams(params) {
    this.resultDetailParams = params;
  }

  setDbHostList(list) {
    this.dbHostList = list;
  }

  setConnFlag = (flag) => {
    this.connFlag = flag;
  };

  setDbHostExtraInfo = (data) => {
    this.dbHostExtraInfo = data;
  };

  setCurrDbList = (list) => {
    this.currDbList = list;
  };

  setVersionCompareResult(data) {
    this.versionCompareResult = data;
  }

  setCheckedIdList = (ids) => {
    this.checkedIdList = ids;
  };

  setSrcDbHostId = (data) => {
    this.srcDbHostId = data;
  };

  setDestDbHostId = (data) => {
    this.destDbHostId = data;
  };

  setResultPageParam = (data) => {
    this.resultPageParam = data;
  };

  setCheckFlag = (flag) => {
    this.checkFlag = flag;
  };

  setCheckResult = (data) => {
    this.checkResult = data;
  };

  setExportFlag = (flag) => {
    this.exportFlag = flag;
  };

  addDbHost = (values) => {
    const tmpList = [...this.dbHostList, {...values, id: uuidv4()}];
    return storage.setItem(DB_LIST_KEY, tmpList);
  };

  editDbHost = (values) => {
    const tmpList = [...this.dbHostList];
    const index = tmpList.findIndex((item) => item.id === values.id);
    tmpList[index] = {...values};
    return storage.setItem(DB_LIST_KEY, tmpList);
  };

  delDbHost = (id) => {
    if (id === this.srcDbHostId) {
      this.srcDbHostId = undefined;
    } else if (id === this.destDbHostId) {
      this.destDbHostId = undefined;
    }
    const tmpList = this.dbHostList.filter((item) => item.id !== id);
    return storage.setItem(DB_LIST_KEY, tmpList);
  };

  delDbHosts = (ids) => {
    if (ids.includes(this.srcDbHostId)) {
      this.srcDbHostId = undefined;
    } else if (ids.includes(this.destDbHostId)) {
      this.destDbHostId = undefined;
    }
    const tmpList = this.dbHostList.filter((item) => !ids.includes(item.id));
    return storage.setItem(DB_LIST_KEY, tmpList);
  };

  getDbHostList = () => {
    const dbHostList = storage.getItem(DB_LIST_KEY);
    const tmpList = Array.isArray(dbHostList) ? dbHostList : [];
    this.setDbHostList(tmpList);
  };

  exportCheckResult = (res) => {
    let data = res.data;
    if (!data) {
      return;
    }
    let fileName = 'result_data.docx';
    if (res.headers['content-disposition']) {
      fileName = JSON.parse(decodeURI(res.headers['content-disposition'].split('filename=')[1]));
    }
    let url = window.URL.createObjectURL(new Blob([data]));
    let a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.setAttribute('download', fileName);
    document.body.appendChild(a);
    a.click(); //执行下载
    window.URL.revokeObjectURL(a.href);
    document.body.removeChild(a);
  };

  dbHost_conn_check = async (params) => {
    try {
      this.setConnFlag(false);
      const res = await AxiosRequest.post(DBHOST_CHECK_OR_ADD_URL, params);
      runInAction(() => {
        if (res.code === 200) {
          this.setConnFlag(true);
        }
      });
    } catch (error) {}
  };

  dbHost_add = async (params) => {
    try {
      this.setConnFlag(false);
      const res = await AxiosRequest.post(DBHOST_CHECK_OR_ADD_URL, params);
      runInAction(() => {
        const data = res.data || {};
        if (res.code === 200) {
          this.setConnFlag(true);
        }
        this.setDbHostExtraInfo({dbType: data.dbType, dbVersion: data.dbVersion});
      });
    } catch (error) {
      this.setDbHostExtraInfo({});
    }
  };

  dbHost_get_dbs = async (params) => {
    try {
      const res = await AxiosRequest.post(DBHOST_GET_DBS_URL, params);
      runInAction(() => {
        const dbList = res.data && Array.isArray(res.data.dbItems) ? res.data.dbItems : [];
        this.setCurrDbList(dbList);
        if (res.code !== 200) {
          message.error('获取数据库列表失败！');
        }
      });
    } catch (error) {
      this.setCurrDbList([]);
    }
  };

  dbHost_version_compare = async (params) => {
    try {
      const res = await AxiosRequest.post(DBHOST_VERSION_COMPARE_URL, params);
      runInAction(() => {
        if (res.code === 200) {
          this.setVersionCompareResult(res.data || {});
        } else {
          this.setVersionCompareResult({});
          message.error('检查失败！');
        }
      });
    } catch (error) {
      this.setVersionCompareResult({});
    }
  };

  dbHost_check_all = async (params) => {
    this.setCheckFlag(false);
    this.setCheckResult([]);
    try {
      const res = await AxiosRequest.post(DBHOST_CHECK_ALL, params);
      runInAction(() => {
        const data = Array.isArray(res.data) ? res.data : [];
        this.setCheckResult(data);
        if (res.code === 200) {
          this.setCheckFlag(true);
        } else {
          message.error('检查失败！请点击返回按钮重试！');
        }
      });
    } catch (error) {
      this.setCheckResult([]);
    }
  };

  export_check_result = async (params) => {
    this.setExportFlag(false);
    try {
      const res = await AxiosRequest.post(DBHOST_CHECK_RESULT_EXPORT, params, {responseType: 'blob'});
      runInAction(() => {
        if (res.status === 200) {
          this.exportCheckResult(res);
          this.setExportFlag(true);
        } else {
          message.error('导出失败！');
        }
      });
    } catch (error) {
      this.setExportFlag(false);
    }
  };
}

export default HomeStore;
