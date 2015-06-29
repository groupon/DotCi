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
import BuildRow from './BuildRow.jsx';
import Router from 'react-router';
import FilterBar from './../FilterBar.jsx';
import LoadingHelper from './../mixins/LoadingHelper.jsx';
import AutoRefreshHelper from './../mixins/AutoRefreshHelper.jsx';
import GroupedBuildsView from './GroupedBuildsView.jsx';
import LinearBuildsView from './LinearBuildsView.jsx';
import ToggleButton from './../lib/ToggleButton.jsx';
import RangeSlider from './../lib/RangeSlider.jsx';
import BranchTabs from './BranchTabs.jsx';
import ActionButton from './../lib/ActionButton.jsx';
require('./build_history.css');
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
        <span className="buildHistory-bar" >
          <FilterBar id="filter-bar" onChange={this._onFilterChange}/> 
          {this.props.branchSelector}
          {this.props.countSlider}
          <ToggleButton onClick={this._groupBuilds} tooltip="Pipeline View"></ToggleButton>
        </span>
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

export default React.createClass({
  mixins:[AutoRefreshHelper,LoadingHelper],
  componentDidMount(){
    this._loadBuildHistory();
    this.setRefreshTimer(this._loadBuildHistory);
  },
  _loadBuildHistory(){
    const actions =this.props.flux.getActions('app');
    actions.getJobInfoFromServer("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", this._currentTab(),this._buildCount());
  },
  _currentTab(){
    return BranchTabs.currentTab();
  },
  _buildCount(){
    return RangeSlider.currentValue();
  },
  _render(){
    const countSlider = <RangeSlider ref="buildCount" tooltip="Build count"  onChange={this._onCountChange} min={20}  max={100} step={5}  />;
    const branchSelector = <BranchTabs  ref="branchTabs" onTabChange={this._onTabChange} flux={this.props.flux} tabs={this.props.tabs} defaultTab={this.defaultTab}/>;
    return(<div className="align-center" >
      <BuildHistoryTable branchSelector={branchSelector} countSlider={countSlider} builds ={this.props.builds}/>
    </div>);
  },
  _onTabChange(tab){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(tab, this._buildCount());
  },
  _onCountChange(count){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(this._currentTab(),count);
  }
});
