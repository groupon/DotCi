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
    return <span>
      <paper-button id="currentBranchButton" onClick={this._onBranchChange} >
        {this.state.currentSelection}<iron-icon  icon="hardware:keyboard-arrow-down"></iron-icon>
      </paper-button>
      <Dialog ref="branchListDialog" noButtons >
        <div>
          {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i,this._isTabRemovable(tab))).toArray()}
          <paper-item>
            <paper-button onClick={this._addTab} ref="ca-addTab" >
              New <iron-icon  icon="add-circle"></iron-icon>
            </paper-button> 
          </paper-item>
        </div>
      </Dialog>
      {this._addTabDialog()}
    </span>;

  },
  _onBranchChange(e){
    if(e.currentTarget && e.currentTarget.id==="currentBranchButton"){
      this.refs.branchListDialog.show();
    }
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
    this.refs.branchListDialog.hide();
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
    this.refs.branchListDialog.hide();
    this.setHash(e.currentTarget.getAttribute('data-tab'));
  },
  _getHistoryTab(tab,i,closable) {
    return <paper-item   key={i} >
      <paper-button data-tab={tab} onClick={this._onTabSelect}>
        {tab}
      </paper-button>
      {closable?<div data-tab={tab} className="tab-close fa fa-times-circle-o" onClick={this._onTabRemove}></div>: ''}
    </paper-item>;
  }
});
