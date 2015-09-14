export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
  }
  addChangeListener(changeListener){
    this.changeListener = changeListener;
  }
  filterChange(){
  }
  buildCountChange(){
  }
  historyChanged(change){
    Object.assign(this,change);
    this.changeListener(this);
  }
}
