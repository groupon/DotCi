import createAction from './createAction.js';
import Page from './Page.js';
export default class extends Page{
  constructor(){
    super();
    this.query={
      filter: 'All',
      limit: 50
    }
    const self = this;
    this.actions =  {
      QueryChange : createAction((data,onAction) =>{
        const oldQuery = Object.assign({},...self.query);
        Object.assign(self.query,data);
        onAction(self,oldQuery);
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
