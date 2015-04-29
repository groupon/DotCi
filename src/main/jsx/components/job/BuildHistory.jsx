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
import Dialog from './../lib/Dialog.jsx';
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import LocationHashHelper from './../mixins/LocationHashHelper.jsx'
import Router from 'react-router';
require('./build_history.less');


var BuildHistoryTable = React.createClass({
  getInitialState: function() {
    return {filter: ''};
  },
  render(){
    let builds = this.props.builds.filter(this._applyFilter).map((build) => <BuildRow key={build.get('number')} build={build}/>);
    return(
      <div className="builds">
        <FilterBar onChange={this._onFilterChange}/> 
        {builds.toArray()}
      </div>
    );
  },
  _applyFilter(build){
    const filter = this.state.filter.trim();
    const filterRegex = new RegExp(filter, 'gi');
    let {message,branch,committerName} = build.get('commit').toObject();
    return !filter || message.match(filterRegex) || branch.match(filterRegex)|| committerName.match(filterRegex);
  },
  _onFilterChange(filter){
    this.replaceState({filter:filter });
  }
});
var BuildHistoryTabs = React.createClass({
  mixins: [LocationHashHelper], 
  componentDidMount(){
    this.addHashListener(this._onActiveTabChange);
  },
  componentWillUnmount(){
    this.removeHashListener(this._onActiveTabChange);
  },
  getInitialState(){
    return {currentSelection: this.selectedHash()?this.selectedHash(): 'master'};
  },
  render()  {
    return (<div className="ui  buttons">
      {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i,this._isTabRemovable(tab))).toArray()}
      <a className="ui icon button" href="#" onClick={this._addTab} > <i className="icon fa fa-plus-circle"></i></a>
      <Dialog ref="addDialog" title="Add new brach tab" onSave={this._onTabSave} >
        <div className="ui labeled input">
          <div className="ui label">
            Branch Regex
          </div>
          <input type="text" ref="newBranchTab" placeholder=""/>
        </div>
      </Dialog>
    </div>
           );
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
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(tab);
  },
  _onActiveTabChange(event){
    const selectedTab = Router.HashLocation.getCurrentPath();
    this._notifyTabSelection(selectedTab?selectedTab:'master');
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
      'labeled':true ,
      'icon':true ,
      'button':true,
      'branch-tab': true,
      'tab-active': this.state.currentSelection== tab
    });
    return (<a className={classes} key={i} href={'#'+tab}>
      <i className="icon octicon octicon-git-branch "></i>
      {tab}
      {closable?<div data-tab={tab} className="tab-close fa fa-times-circle-o" onClick={this._onTabRemove}></div>: ''}
    </a>);
  }
});

const FilterBar = React.createClass({
  render(){
    return (<div id="filter-bar" className=" ui icon input">
      <input type="text" onChange={this._onChange} placeholder="Filter..."/>
      <i className="fa fa-filter icon"></i>
    </div>);
  },
  _onChange(event){
    this.props.onChange(event.target.value);
  }
});
export default React.createClass({
  render(){
    return(
      <div id="build-history">
        <BuildHistoryTabs flux={this.props.flux} tabs={this.props.tabs}/>
        <BuildHistoryTable builds ={this.props.builds}/>
      </div>
    );
  }
});
