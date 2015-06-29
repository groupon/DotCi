import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
import FaviconHelper from './../../mixins/FaviconHelper.jsx';
import LoadingHelper from './../../mixins/LoadingHelper.jsx';
import AutoRefreshHelper from './../../mixins/AutoRefreshHelper.jsx';
import SubBuildsMenu from './SubBuildsMenu.jsx';
require('./build.less');
export default  React.createClass({
  mixins: [FaviconHelper,LoadingHelper,AutoRefreshHelper],
  componentDidMount(){
    this._fetchBuild()
  },
  componentDidUpdate(){
    if(this._getBuildResult() === 'IN_PROGRESS' ){
      this.setRefreshTimer(this._refreshCurrentBuild);
    }else{
      this.clearAutoRefresh();
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
      <BuildRow  build={this.props.build}/>
      <SubBuildsMenu buildNumber={this._get('number')} axisList={this._get('axisList')} selectedBuild={this._subBuild()}/>
      <Console log={this.props.build.get('log')} url={this._selectedBuildUrl()} buildResult={this._buildResult()}/>
    </div>);
  },
  _get(key){
    return this.props.build.get(key)
  }
});
