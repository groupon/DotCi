import React from "react";
import find from 'ramda/src/find'
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
import FaviconHelper from './../../mixins/FaviconHelper.jsx';
import SubBuildsMenu from './SubBuildsMenu.jsx';
require('./build.less');
export default  React.createClass({
  mixins: [FaviconHelper],
  componentDidUpdate(){
    this.setFavicon(this._getBuildResult());
  },
  _getBuildResult(){
    return this.props.build && this.props.build.result;
  },
  _subBuild(){
    return this.props.subBuild;
  },
  _buildResult(){
    return this._selectedAxis().result;
  },
  _selectedBuildUrl(){
    return this._selectedAxis().url;
  },
  _selectedAxis(){
    const selectedSubBuild = this._subBuild();
    return find(axis => axis.script === selectedSubBuild)(this._get('axisList'))
  },
  render(){
    return(<div id="build">
      <BuildRow  build={this.props.build}/>
      <SubBuildsMenu buildNumber={this._get('number')} axisList={this._get('axisList')} selectedBuild={this._subBuild()}/>
      <Console log={this.props.build.log} url={this._selectedBuildUrl()} inProgress={this._get('inProgress')} buildResult={this._buildResult()}/>
    </div>);
  },
  _get(key){
    return this.props.build[key];
  }
});
