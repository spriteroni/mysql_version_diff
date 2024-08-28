import {message} from 'antd';
const storageObj = window.localStorage;
export const DB_LIST_KEY = 'db_host_list';
export const MIGRATION_TOOL_DECLARATION_FLAG = 'migration_tool_declaration_flag';
// Tool
export const ACTIONS = {
  ADD: 'add',
  EDIT: 'edit'
}

export const getHostFormData = (values) => {
  const formData = new FormData();
  formData.append('host', values.host);
  formData.append('port', values.port);
  formData.append('userName', values.username);
  formData.append('pwd', values.password);
  return formData;
};

export const storage = {
  setItem(key, value) {
    let tmpVal;
    let flag = true;
    try {
      tmpVal = JSON.stringify(value);
    } catch (e) {
      flag = false;
    }
    storageObj.setItem(key, encode.encode(tmpVal));
    return flag;
  },
  getItem(key) {
    const value = storageObj.getItem(key);
    const tmpVal = encode.decode(value);
    try {
      return JSON.parse(tmpVal);
    } catch (e) {
      return undefined;
    }
  },
  removeItem(key) {
    storageObj.removeItem(key);
  },
  clear() {
    storageObj.clear();
  },
};

export const encode = {
  encode(str) {
    return window.btoa(encodeURIComponent(str));
  },
  decode(str) {
    return decodeURIComponent(window.atob(str));
  },
};

export const getDeclarationFlag = () => {
  const declarationFlag = storage.getItem(MIGRATION_TOOL_DECLARATION_FLAG);
  if(declarationFlag){
    return declarationFlag
  }
  return undefined;
};

export const setDeclarationFlag = () =>{
  storage.setItem(MIGRATION_TOOL_DECLARATION_FLAG, new Date().getTime());
}
