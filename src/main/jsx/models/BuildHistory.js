export default class {
  constructor(){
    this.filters= [];
    this.builds= [];
    this.buildCount =50;
    this.buildFilter ='All';
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
