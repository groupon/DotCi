import {job} from './../api/Api.jsx'; 
export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
  }
  addChangeListener(changeListener){
    this.changeListener = changeListener;
  }
  historyChanged(change){
    Object.assign(this,change);
    this.changeListener(this);
  }
  queryChange(query){
    job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", query.filter,query.limit).then(data => {
      this.historyChanged({... data, filters: data.buildHistoryTabs});
    });
  }
}
