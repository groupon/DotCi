import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
import ActionButton from './../../lib/ActionButton.jsx';
import FaviconHelper from './../../mixins/FaviconHelper.jsx';
import LoadingHelper from './../../mixins/LoadingHelper.jsx';
import AutoRefreshHelper from './../../mixins/AutoRefreshHelper.jsx';
import SubBuildsMenu from './SubBuildsMenu.jsx';
import simpleStorage from './../../../vendor/simpleStorage.js';
require('./build.less');
export default  React.createClass({
  mixins: [FaviconHelper,LoadingHelper,AutoRefreshHelper],
  componentDidMount(){
    this._fetchBuild()
  },
  componentDidUpdate(){
    if(this._getBuildResult() == 'IN_PROGRESS' ){
      this.setRefreshTimer(this._refreshCurrentBuild);
    }else{
      this.clearAutoRefresh();
      this._webNotifyCompletion();
    }
    this.setFavicon(this._getBuildResult());
  },
  componentWillReceiveProps(nextProps){
    if(nextProps.subBuild !== this.props.subBuild){
      const actions = this.props.flux.getActions('app');
      actions.loadBuildLog(this.props.url,nextProps.subBuild.replace('dotCI',''));
    }
  },
  _getBuildResult(){
    return this.props.build && this.props.build.get('result')
  },
  _fetchBuild(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.props.url,this._subBuild());
  },
  _subBuild(){
    return this.props.subBuild.replace('dotCI','');
  },
  _buildResult(){
    return this._selectedAxis().get('result');
  },
  _selectedBuildUrl(){
    return this._selectedAxis().get('url');
  },
  _selectedAxis(){
    const selectedSubBuild = this._subBuild();
    return this._get('axisList').toArray().find(axis => axis.get('script') === selectedSubBuild)
  },
  _refreshCurrentBuild(){
    const actions = this.props.flux.getActions('app');
    actions.refreshBuild(this.props.url,this._subBuild());
  },
  _render(){
    return(<div id="build">
      {this._buildActions()}
      <a className="ui circular icon button hint--top" data-hint="More Details" href={this._get('number')+"/detail"}><i className="fa fa-cogs"></i></a>
      <BuildRow  build={this.props.build}/>
      <SubBuildsMenu buildNumber={this._get('number')} axisList={this._get('axisList')} selectedBuild={this._subBuild()}/>
      <Console log={this.props.build.get('log')} url={this._selectedBuildUrl()} buildResult={this._buildResult()}/>
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
    return <ActionButton key="restart-build" href={`${window.jobUrl}/${this.props.build.get('number')}/rebuild`} tooltip="Restart Build" icon="fa fa-refresh" />;
  },
  _cancelButton(){
    return <ActionButton key="cancel-build" onClick={this._cancelBuild} tooltip="Cancel Build" icon="fa fa-times-circle-o"/>;
  },
  _cancelBuild(e){
    e.preventDefault();
    const appActions = this.props.flux.getActions('app');
    appActions.cancelBuild(this.props.build.get('cancelUrl'));
  },
  _restartBuild(e){
    e.preventDefault();
    window.location = window.jobUrl +'rebuild';
  },
  _get(key){
    return this.props.build.get(key)
  }
});
