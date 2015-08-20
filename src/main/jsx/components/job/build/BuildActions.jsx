import React from "react";
import simpleStorage from './../../../vendor/simpleStorage.js';
export default React.createClass( {
  componentDidUpdate(){
    if(this.props.result !== 'IN_PROGRESS' ){
      this._webNotifyCompletion();
    }
  },
  render(){
    const buildActions = [];
    if(this.props.cancelable){
      this._add(buildActions,"cancel", <a onClick={this._cancelBuild} href="#">Cancel</a>);
      if(this._supportsNotifications()){
        this._add(buildActions,"visibility", <a onClick={this._watchBuild} href="#">Watch</a>);
      }
    }else{
      this._add(buildActions,"autorenew", <a href={this.props.number+"/rebuild"}>Restart</a>);
    }
    this._add(buildActions,"delete", <a href={this.props.number+"/confirmDelete"}>Delete</a>);
    this._add(buildActions,"label",
              <a href={this.props.number+"/detail"}>More...</a>);
              return <span> {buildActions} </span>
  },
  _supportsNotifications(){
    return simpleStorage.canUse() && ("Notification" in window);
  },
  _watchBuild(e){
    e.preventDefault();
    Notification.requestPermission((result)=>{
      if(result === 'granted'){
        const url = this.props.cancelUrl;
        simpleStorage.set(url, true, {TTL: 7200000}) // 2 hrs
      }
    });
  },
  _webNotifyCompletion(){
    const url = this.props.cancelUrl;
    if(simpleStorage.get(url)){
      new Notification(`Build #${this.props.number} finished with ${this.props.result}`, {body:`${this.props.commit.get('message')}`, icon: "<iron-icon icon='done'></iron-icon>"});
      simpleStorage.deleteKey(url);
    }
  },
  _cancelBuild(e){
    e.preventDefault();
    const appActions = this.props.flux.getActions('app');
    appActions.cancelBuild(this.props.cancelUrl);
  },
  _add(actions, icon,action){
    actions.push(
      <paper-icon-item key={icon}>
        <iron-icon icon={icon} />{action}
      </paper-icon-item>
    );
  }
})
