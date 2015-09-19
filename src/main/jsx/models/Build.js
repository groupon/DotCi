import createAction from './createAction.js';
export default class {
  constructor(){
    this.query={}
    const self = this;
    this.actions =  {
      QueryChange : createAction((data,onAction) =>{
        Object.assign(self.query,data);
        onAction(self);
      }),
      DataChange : createAction((data,callBack)=>{
        Object.assign(self,data);
        callBack(self);
      })
    }
  }
};
