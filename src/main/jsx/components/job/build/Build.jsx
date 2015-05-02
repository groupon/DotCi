import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
import ActionButton from './../../lib/ActionButton.jsx';
import FaviconHelper from './../../mixins/FaviconHelper.jsx';
import LoadingHelper from './../../mixins/LoadingHelper.jsx';
import simpleStorage from './../../../vendor/simpleStorage.js';
require('./build.less');
export default  React.createClass({
  mixins: [FaviconHelper,LoadingHelper],
  componentDidMount(){
    this._fetchBuild()
  },
  componentWillUnmount() {
    clearInterval(this.refreshTimer);
  },
  componentDidUpdate(){
    if(this._getBuildResult() == 'IN_PROGRESS' ){
      this._setRefreshTimer();
    }else{
      this._webNotifyCompletion();
    }
    this.setFavicon(this._getBuildResult());
  },
  _getBuildResult(){
    return this.props.build && this.props.build.get('result')
  },
  _setRefreshTimer(){
    if(!this.refreshTimer)
      this.refreshTimer = setInterval(this._refreshCurrentBuild, 5000);
  },
  _fetchBuild(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.props.url);
  },
  _refreshCurrentBuild(){
    const actions = this.props.flux.getActions('app');
    actions.refreshBuild(this.props.url);
  },
  _render(){
    return(<div id="build">
      {this._buildActions()}
      <BuildRow  build={this.props.build}/>
      <Console log={this.props.build.get('log')}/>
    </div>);
  },
  _buildActions(){
    return  this.props.build.get('cancelable')? this._inProgressActions():[this._restartButton()];
  },
  _inProgressActions(){
    return [this._cancelButton(), this._watchButton()];
  },
  _watchButton(){
    return this._supportsNotifications()?<ActionButton ref="watchButton" onClick={this._watchBuild} tooltip="Notify when done( web notification)" icon="fa fa-eye"/>: <span/>;
  },
  _supportsNotifications(){
    return simpleStorage.canUse() && ("Notification" in window);
  },
  _watchBuild(e){
    Notification.requestPermission((result)=>{
      if(result == 'granted'){
        this.refs.watchButton.disable();
        const url = this.props.build.get('cancelUrl')
        simpleStorage.set(url, true, {TTL: 7200000}) // 2 hrs
      }
    });
  },
  _webNotifyCompletion(){
    const url = this.props.build && this.props.build.get('cancelUrl')
    if(simpleStorage.get(url)){
      new Notification(`Build #${this._get('number')} finished with ${this._get('result')}`, {body:`${this._get('commit').get('message')}`, icon: "<i class='fa fa-bed'></i>"});
      simpleStorage.deleteKey(url);
    }
  },
  _restartButton(){
    return <ActionButton onClick={this._restartBuild} tooltip="Restart Build" icon="fa fa-refresh" />;
  },
  _cancelButton(){
    return <ActionButton onClick={this._cancelBuild} tooltip="Cancel Build" icon="fa fa-times-circle-o"/>;
  },
  _cancelBuild(e){
    e.preventDefault();
    const appActions = this.props.flux.getActions('app');
    appActions.cancelBuild(this.props.build.get('cancelUrl'));
  },
  _restartBuild(e){
    e.preventDefault();
    window.location = window.location + '/rebuild/parameterized';
  },
  _get(key){
    return this.props.build.get(key)
  }
});
