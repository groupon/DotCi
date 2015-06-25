import React from 'react';
import LocationHashHelper from './../mixins/LocationHashHelper.jsx'
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import ActionButton from './../lib/ActionButton.jsx';
import Router from 'react-router';
import CustomAttributes from './../mixins/CustomAttributes.jsx';
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
        <paper-tab>
          <ActionButton tooltip="Add new tab" onClick={this._addTab} icon="fa fa-plus-circle" dontDisable/>
        </paper-tab>
      </paper-tabs>
      <paper-dialog  ref="ca-addDialog"  attrs={{heading:"Add new brach tab"}} onClick={this._onTabSave} >
        <paper-input ref="ca-branchInput" attrs={{label:"Branch Expression"}}></paper-input>
        <div className="buttons">
          <paper-button ref="ca-1" attrs={{"dialog-dismiss": true}}>Cancel</paper-button>
          <paper-button id="addTabButton" ref="ca-2" attrs={{ "dialog-confirm": true}}>Accept</paper-button>
        </div>
      </paper-dialog>
    </div>
           );
  },
  _isTabRemovable(tab){
    return !contains(tab)(['master','All','Mine']);
  },
  _addTab(){
    const addDialog = this.refs['ca-addDialog'];
    addDialog.getDOMNode().toggle();
  },
  _onTabSave(e){
    if(e.target.parentElement && e.target.parentElement.id === "addTabButton"){
      const tabExpr = this.refs['ca-branchInput'].getDOMNode().value
      if(tabExpr){
        this.props.flux.addBranchTab(tabExpr);
      }
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
