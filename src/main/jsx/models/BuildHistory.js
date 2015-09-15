export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
    this.query={}
  }
  addChangeListener(changeListener){
    this.changeListener = changeListener;
  }
  addQueryChangeListener(queryChangeListener){
    this.queryChangeListener = queryChangeListener;
  }
  historyChanged(change){
    Object.assign(this,change);
    this.changeListener(this);
  }
  queryChanged(query){
    Object.assign(this.query,query);
    this.queryChangeListener(this);
  }
}
