function createAction(onCallBack){
  const action = function(data){
    onCallBack(data,action.onAction);
  }
  return action;
}
export default class {
  constructor(){
    // this.builds= [];
    // this.filters= [];
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
      }),
      RemoveFilter : createAction((removedFilter,callBack)=>{
        //-------- Optimistic Update
        const idx = self.filters.indexOf(removedFilter);
        self.filters.splice(idx, 1);
        self.actions.DataChange(self);
        //---------------
        callBack(removedFilter);
      }),
      AddFilter: createAction((newFilter,callBack)=>{
        //-------- Optimistic Update
        self.filters.push(newFilter);
        self.actions.DataChange(self);
        //---------------
        callBack(newFilter);
      })
    }
  }
}
