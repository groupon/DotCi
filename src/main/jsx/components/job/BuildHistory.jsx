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
require('./build_history.less');


var BuildHistoryTable = React.createClass({
  getInitialState: function() {
      return {filter: ''};
    },
  render(){
    let builds = this.props.builds.filter(this._applyFilter).map((build) => <BuildRow key={build.number} {...build}/>);
    return(
      <div className="builds">
         <FilterBar onChange={this._onFilterChange}/> 
        {builds}
      </div>
    );
  },
  _applyFilter(build){
    const filter = this.state.filter.trim();
    const filterRegex = new RegExp(filter, 'gi');
    return !filter || build.commit.message.match(filterRegex) || build.commit.branch.match(filterRegex)|| build.commit.committerName.match(filterRegex);
  },
  _onFilterChange(filter){
    this.replaceState({filter:filter });
  }
});
var BuildHistoryTabs = React.createClass({
  getInitialState(){
    return {currentSelection: 0};
  },
  render()  {
    return (<div className="ui  buttons">
      {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i,this._isTabRemovable(tab)))}
      <a className="ui icon button" href="#" onClick={this._addTab} > <i className="icon fa fa-plus-circle"></i></a>
      <Dialog ref="addDialog" title="Add new brach tab" onSave={this._onTabSave} >
        <div className="ui label"> Branch Regex </div>
        <input type="text" ref="newBranchTab" defaultValue=""/>
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
  _notifyTabSelection: function (tabIndex) {
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(this.props.tabs[tabIndex]);
  },
  _onActiveTabChange(event){
    var tab =parseInt(event.currentTarget.getAttribute('data-tab'));
    this.setState({currentSelection: tab});
    this._notifyTabSelection(tab);
  },
  _getHistoryTab(tab,i,closable) {
    var cx = React.addons.classSet;
    var classes = cx({
      'ui':true,
      'labeled':true ,
      'icon':true ,
      'button':true,
      'branch-tab': true,
      'tab-active': this.state.currentSelection==i
    });
    return (<div className={classes} key={i} data-tab={i}  onClick={this._onActiveTabChange} href="#" >
            <i className="icon octicon octicon-git-branch "></i>
      {tab}
      {closable?<a className="tab-close fa fa-times-circle-o"></a>: ''}
    </div>);
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
        <h4 className="ui horizontal header divider">
          <i className="fa fa-server"></i>
          Builds
        </h4>
        <BuildHistoryTable builds ={this.props.builds}/>
      </div>
    );
  }
});
