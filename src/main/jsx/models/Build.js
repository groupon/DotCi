import createAction from './createAction.js';
import last from 'ramda/src/last';
if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position) {
    position = position || 0;
    return this.indexOf(searchString, position) === position;
  };
}
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
        callBack(self);
      })
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
  get log12h(){
    const groupedLines = logLines.reduce((list,line)=>{
      if( line.startsWith('$')){
        return  list.push(List.of(line))
      }
      const newLast = last()(list).push(line)
      return list.set(-1,newLast);
    } , [[]]);
    //Add start indexes Turn [[..],[...],..] => [[[...],1] [[....],4], ..]
    const groupedLinesWithIdx =  groupedLines.reduce((list,group) => {
      if(last()(list)){
        return list.push(List.of(group,list.last().get(0).size+ list.last().get(1)))
      }
      return list.push([group,1]);
    }, []);
  }
};
