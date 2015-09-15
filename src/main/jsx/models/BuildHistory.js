/*
 * Model has 
 * 1.data 
 * 2. query that corrsponds to that data
 */
export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
    this.query={}
  }
  addDataChangeListener(changeListener){
    this.dataChangeListener = changeListener;
  }
  addQueryChangeListener(queryChangeListener){
    this.queryChangeListener = queryChangeListener;
  }
  dataChanged(change){
    Object.assign(this,change);
    this.dataChangeListener(this);
  }
  queryChanged(query){
    Object.assign(this.query,query);
    this.queryChangeListener(this);
  }
}
