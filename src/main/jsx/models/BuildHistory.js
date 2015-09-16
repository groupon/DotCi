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
    this.Actions = {
      QueryChange(newQuery){
        return {name:'QUERY_CHANGE', data: newQuery}
      },
      DataChange(newData){
        return {name:'DATA_CHANGE', data: newData}
      },
      RemoveFilter(filter){
        return {name:'REMOVE_FILTER', data: filter}
      }
    }
  }
  setActionChangeListener(actionChangeListener){
    this.actionChangeListener = actionChangeListener;
  }
  sendAction(action){
    switch(action.name) {
      case 'QUERY_CHANGE':
        Object.assign(this.query,action.data);
      break;
      case 'DATA_CHANGE':
        Object.assign(this,action.data);
      break;
      case 'REMOVE_FILTER':
        //optimistic update here
        break;
    }
    this.actionChangeListener(action,this);
  }
}
