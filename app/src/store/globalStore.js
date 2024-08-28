import {runInAction, makeAutoObservable} from 'mobx'
import {message} from 'antd'

export default class GlobalStore {
  constructor() {
    makeAutoObservable(this)
  }

  MenuSelectKeys = ''
  GitUrl = ''
  breadcrumb = []

  declarationVisible = false;

  setSelectKeysValue = (data) => {
    this.MenuSelectKeys = data
  }

  setBreadcrumb(breadcrumb = []) {
    this.breadcrumb = breadcrumb
  }

  setDeclarationVisible(visible) {
    this.declarationVisible = visible
  }
}
