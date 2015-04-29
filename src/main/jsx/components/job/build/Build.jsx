import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
import ActionButton from './../../lib/ActionButton.jsx';
import FaviconHelper from './../../mixins/FaviconHelper.jsx';
require('./build.less');
export default  React.createClass({
  mixins: [FaviconHelper],
  componentDidMount(){
    this._fetchBuild()
  },
  componentDidUpdate(){
    if(this._getBuildResult() == 'IN_PROGRESS' ){
      this._setRefreshTimer();
      this.setFavicon(this._getBuildResult());
    }else{
      this._clearRefreshTimer();
    }
  },
  componentWillUnmount: function() {
    this._clearRefreshTimer(); 
  },
  _getBuildResult(){
    return this.props.build && this.props.build.get('result')
  },
  _setRefreshTimer(){
    if(!this.refreshTimer){
      this.refreshTimer = setInterval(this._fetchBuild, 5000);
    }
  },
  _clearRefreshTimer(){
    if(this.refreshTimer) clearInterval(this.refreshTimer);
  },
  _fetchBuild(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.props.url);
  },
  render(){
    return this.props.build? (<div id="build">
      {this._buildActions()}
      <BuildRow  build={this.props.build}/>
      <Console log={this.props.build.get('log')}/>
    </div>):<div/>;
  },
  _buildActions(){
    return this.props.build.get('cancelable')? this._cancelButton():this._restartButton();
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
  }
});
