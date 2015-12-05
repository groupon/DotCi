export default class {
  exit(){
    this.setActionState(false);
  }
  enter(){
    this.setActionState(true);
  }
  setActionState(active){
    const actions = this.actions;
    for (let actionName in actions) {
      if( actions.hasOwnProperty(actionName) ) {
        const action = actions[actionName];
        action.active = active;
      } 
    }
  }
}
