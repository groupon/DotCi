import Router from 'react-router';
export default {
  addHashListener(hashListener){
    window.addEventListener("hashchange", hashListener, false);
  },
  removeHashListener(hashListener){
    window.removeEventListener("hashchange", hashListener, false);
  },
  getHashIds(event){
    const oldId = event.oldURL.split('#')[1];
    const newId = event.newURL.split('#')[1];
    return [oldId,newId]
  },
  selectedHash(){
    return Router.HashLocation.getCurrentPath();
  },
  componentDidMount(){
    this.addHashListener(this._onLocationHashChange);
  },
  componentWillUnmount(){
    this.removeHashListener(this._onLocationHashChange);
  }
}
