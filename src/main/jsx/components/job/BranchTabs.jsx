import React from 'react';
import LocationHashHelper from './../mixins/LocationHashHelper.jsx'
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import Dialog from './../lib/Dialog.jsx';
import ActionButton from './../lib/ActionButton.jsx';
import Router from 'react-router';
import LoadingHelper from './../mixins/LoadingHelper.jsx';
require('./branch_tabs.css')
export default React.createClass({
  mixins: [LocationHashHelper,LoadingHelper], 
  getInitialState(){
    return {currentSelection: this.selectedHash()?this.selectedHash(): this.props.defaultTab};
  },
  currentTab(){
    const selectedTab = Router.HashLocation.getCurrentPath();
    return selectedTab|| this.props.defaultTab;
  },
  _render()  {
    return (<div className="ui text menu">
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
