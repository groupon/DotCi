import React from 'react';
import LocationHashHelper from './../mixins/LocationHashHelper.jsx'
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import ActionButton from './../lib/ActionButton.jsx';
import Router from 'react-router';
import CustomAttributes from './../mixins/CustomAttributes.jsx';
import Dialog from './../lib/Dialog.jsx';
require('./branch_tabs.css')
export default React.createClass({
  mixins: [LocationHashHelper,CustomAttributes], 
  statics: {
    currentTab(){
      const selectedTab = Router.HashLocation.getCurrentPath();
      return selectedTab|| 'All';
    }
  },
  getInitialState(){
    const selectedTab = Router.HashLocation.getCurrentPath();
    return {currentSelection: selectedTab|| 'All'};
  },

  render()  {
    const selected = this.props.tabs.findIndex(tab => tab == this.state.currentSelection); 
    return (<div>
      <paper-tabs ref="ca-branchTabs" attrs={{selected}}>
        {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i,this._isTabRemovable(tab))).toArray()}
        <span className="hint--left" data-hint="Add new Tab" ><paper-icon-button onClick={this._addTab} ref="ca-addTab" attrs={{icon:"add-circle"}}></paper-icon-button> </span>
      </paper-tabs>
      {this._addTabDialog()}
    </div>
           );
  },
  _addTabDialog(){
    return <Dialog  ref="addTabDialog" heading="Add new brach tab" onSave={this._onTabSave} >
      <paper-input ref="ca-branchInput" attrs={{label:"Branch Expression"}}></paper-input>
    </Dialog>;
  },
  _isTabRemovable(tab){
    return !contains(tab)(['master','All','Mine']);
  },
  _addTab(){
    this.refs.addTabDialog.show();
  },
  _onTabSave(e){
    const tabExpr = this.refs['ca-branchInput'].getDOMNode().value
    if(tabExpr){
      this.props.flux.addBranchTab(tabExpr);
    }
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
  _onTabSelect(e){
    this.setHash(e.currentTarget.getAttribute('data-tab'));
  },
  _getHistoryTab(tab,i,closable) {
    return <paper-tab data-tab={tab}  key={i} onClick={this._onTabSelect}>
      {tab}
      {closable?<div data-tab={tab} className="tab-close fa fa-times-circle-o" onClick={this._onTabRemove}></div>: ''}
    </paper-tab>;
  }
});
