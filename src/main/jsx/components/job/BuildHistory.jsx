/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import React from 'react';
import ActionButton from './../lib/ActionButton.jsx';
import BuildRow from './BuildRow.jsx';
import Dialog from './../lib/Dialog.jsx';
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import LocationHashHelper from './../mixins/LocationHashHelper.jsx'
import Router from 'react-router';
import FilterBar from './../FilterBar.jsx';
import LoadingHelper from './../mixins/LoadingHelper.jsx';
import AutoRefreshHelper from './../mixins/AutoRefreshHelper.jsx';
import GroupedBuildsView from './GroupedBuildsView.jsx';
import LinearBuildsView from './LinearBuildsView.jsx';
import ToggleButton from './../lib/ToggleButton.jsx';
import RangeSlider from './../lib/RangeSlider.jsx';
require('./build_history.less');
var BuildHistoryTable = React.createClass({
  getInitialState: function() {
    return {filter: '',grouped: false};
  },
  _filteredBuilds(){
    return this.props.builds.filter(this._applyFilter);
  },
  render(){
    return(
      <div>
        <ToggleButton onClick={this._groupBuilds} tooltip="Group by commit"><i className="fa fa-bars"></i></ToggleButton>
        <FilterBar id="filter-bar" onChange={this._onFilterChange}/> 
        {this.props.countSlider}
        {this.state.grouped? <GroupedBuildsView builds={this._filteredBuilds()} /> : <LinearBuildsView builds={this._filteredBuilds()} />}
      </div>
    );
  },
  _groupBuilds(grouped){
    this.setState({grouped})
  },
  _applyFilter(build){
    const filter = this.state.filter.trim();
    const filterRegex = new RegExp(filter, 'gi');
    let {message,branch,committerName} = build.get('commit').toObject();
    return !filter || message.match(filterRegex) || branch.match(filterRegex)|| committerName.match(filterRegex);
  },
  _onFilterChange(filter){
    this.setState({filter});
  }
});
var BuildHistoryTabs = React.createClass({
  mixins: [LocationHashHelper], 
  getInitialState(){
    return {currentSelection: this.selectedHash()?this.selectedHash(): this.props.defaultTab};
  },
  render()  {
    return (<div className="ui text menu">
      <div className="ui item"><ActionButton tooltip="Build Now" href="build?delay=0sec" icon="fa fa-rocket" primary/></div>
      {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i,this._isTabRemovable(tab))).toArray()}
      <ActionButton className="ui item" tooltip="Add new tab" onClick={this._addTab} icon="fa fa-plus-circle"/>
      <Dialog ref="addDialog" title="Add new brach tab" onSave={this._onTabSave} >
        <div className="ui labeled input">
          <div className="ui label">
            Branch Regex
          </div>
          <input type="text" ref="newBranchTab" placeholder=""/>
        </div>
      </Dialog>
    </div>);
  },
  _isTabRemovable(tab){
    return !contains(tab)(['master','All','Mine']);
  },
  _addTab(){
    const addDialog = this.refs.addDialog;
    addDialog.open();
  },
  _onTabSave(){
    const tabExpr = this.refs.newBranchTab.getDOMNode().value
    this.props.flux.addBranchTab(tabExpr);
  },
  _notifyTabSelection: function (tab) {
    this.replaceState({currentSelection: tab});
    this.props.onTabChange(tab);
  },
  _onLocationHashChange(event){
    const selectedTab = this.selectedHash();
    this._notifyTabSelection(selectedTab?selectedTab:this.defaultTab);
  },
  _onTabRemove(event){
    event.stopPropagation();
    this.replaceState(this.getInitialState());
    this._notifyTabSelection(this.state.currentSelection);
    var tab = event.currentTarget.getAttribute('data-tab');
    this.props.flux.removeBranchTab(tab);
  },
  _getHistoryTab(tab,i,closable) {
    var classes = classNames({
      'ui':true,
      'item':true ,
      'green':true ,
      'active': this.state.currentSelection== tab
    });
    return (<a className={classes} key={i} href={'#'+tab}>
      <i className="icon octicon octicon-git-branch "></i>
      {tab}
      {closable?<div data-tab={tab} className="tab-close fa fa-times-circle-o" onClick={this._onTabRemove}></div>: ''}
    </a>);
  }
});

export default React.createClass({
  mixins:[LoadingHelper,AutoRefreshHelper],
  defaultTab: 'All',
  componentDidMount(){
    this._loadBuildHistory();
    this.setRefreshTimer(this._loadBuildHistory);
  },
  _loadBuildHistory(){
    const actions =this.props.flux.getActions('app');
    actions.getJobInfoFromServer("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",this._currentTab(),this._buildCount());
  },
  _currentTab(){
    const selectedTab = Router.HashLocation.getCurrentPath();
    return selectedTab|| this.defaultTab;
  },
  _render(){
    const countSlider = <RangeSlider ref="buildCount" tooltip="Build count" queryParam="count" onChange={this._onCountChange} min={20}  max={100} step={5}  />
    return(<div id="build-history">
      <BuildHistoryTabs  onTabChange={this._onTabChange} flux={this.props.flux} tabs={this.props.tabs} defaultTab={this.defaultTab}/>
      <BuildHistoryTable countSlider={countSlider} builds ={this.props.builds}/>
    </div>);
  },
  _onTabChange(tab){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(tab, this._buildCount());
  },
  _onCountChange(count){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(this._currentTab(),count);
  },
  _buildCount(){
    return this.refs.buildCount?this.refs.buildCount.value():20;
  }
});
