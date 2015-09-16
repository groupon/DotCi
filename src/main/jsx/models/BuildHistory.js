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
    return this.onCallBack(data,this.onAction);
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
      return callBack(self);
    });
    const DataChange = new Action((data,callBack)=>{
      Object.assign(self,data);
      return callBack(self);
    });
    const RemoveFilter = new Action((removedFilter,callBack)=>{
      // Object.assign(self,data);
      return callBack(self);
    });
    this.Actions = {DataChange,QueryChange,RemoveFilter }
  }
}
