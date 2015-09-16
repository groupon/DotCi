/*
 * Model has 
 * 1.data 
 * 2. query that corrsponds to that data
 */
class Action{
  constructor(onCallBack){
    this.onCallBack = onCallBack;
  }
  onAction(onAction){
    this.onAction = onAction;
  }
  send(data){
    this.onCallBack(data,this.onAction);
  }
}
export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
    this.query={}
    const self = this;
    const QueryChange = new Action((newQuery,callBack)=>{
      Object.assign(self.query,newQuery);
      callBack(self);
    });
    const DataChange = new Action((data,callBack)=>{
      Object.assign(self,data);
      callBack(self);
    });
    const RemoveFilter = new Action((removedFilter,callBack)=>{
      //-------- Optimistic Update
      const idx = self.filters.indexOf(removedFilter);
      self.filters.splice(idx, 1);
      self.actions.DataChange.send(self);
      //---------------
      callBack(self);
    });
    this.actions = {DataChange,QueryChange,RemoveFilter }
  }
}
