import jsuri from 'jsuri';

let API_SERVER = location.origin;
API_SERVER = 'http://127.0.0.1:8080';
let origin = new jsuri(API_SERVER),
  originPath = origin.path();

function getUri(api) {
  let uri = origin.clone();
  uri.setPath(`${originPath}${api}`);
  return uri.toString();
}

export const DBHOST_CHECK_OR_ADD_URL = getUri('/dbcheck/cv');
export const DBHOST_GET_DBS_URL = getUri('/dbcheck/getdbs');
export const DBHOST_VERSION_COMPARE_URL = getUri('/dbcheck/compare');
export const DBHOST_CHECK_ALL = getUri('/dbcheck/checkall');
export const DBHOST_CHECK_RESULT_EXPORT = getUri('/dbcheck/export');
