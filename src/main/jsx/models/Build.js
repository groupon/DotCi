import createAction from './createAction.js';
import last from 'ramda/src/last';
export default class {
  constructor(){
    this.query={}
    const self = this;
    this.actions =  {
      BuildChange : createAction((number,onAction) =>{
        this.number = number;
        onAction(self);
      }),
      LogChange : createAction((logText,onAction) =>{
        self.log = logText;
        onAction(self);
      }),
      BuildInfoChange : createAction((buildInfo,callBack)=>{
        Object.assign(self,buildInfo);
        self.inProgress = self.result === 'IN_PROGRESS';
        callBack(self);
      }),
      CancelBuild : createAction((url,callBack)=>{
        self.result = 'ABORTED';
        self.actions.BuildInfoChange(self);
        callBack(self);
      }),

    }
  }
  set log(logText){
    this._log=logText.split("\n"); 
  }
  get log(){
    if(!this._log) return [];
    return  this._log.reduce((grouped,line) => {
      if( line.startsWith('$')){
        grouped.push([line]);
      }else{
        (last()(grouped)).push(line);
      }
      return grouped;
    },[[]])
  }
};
