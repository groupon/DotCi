import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import  CustomAttributes from './components/mixins/CustomAttributes.jsx';
import Dialog from './components/lib/Dialog.jsx';
import { TransitionHook } from 'react-router';

export default React.createClass( {
  mixins: [CustomAttributes, TransitionHook],
  getInitialState(){
    return {selectedTab: "1"}
  },
  routerWillLeave (nextState, router) {
    debugger
  },
  render(){
    return <div>
      <paper-toolbar id="drawerToolbar">
        <div className="  title">Current</div>
        <paper-icon-button onClick={this._onJobChange} icon="hardware:keyboard-arrow-down"></paper-icon-button>
      </paper-toolbar>
      {this._currentMenu()}
      <Dialog ref="recentProjectsDialog" heading="Recent Builds" noButtons lazy>
        <RecentProjects flux = {this.props.flux} />
      </Dialog>
    </div>
  },
  _onJobChange(e){
    this.refs.recentProjectsDialog.show();
  },
  _currentMenu(){
    return <div className="list short">
      <paper-icon-item>
        <div className="fa fa-wrench" ></div> <a href="dotCIbuildHistory">Build History</a>
      </paper-icon-item>
      <paper-icon-item>
        <div className="fa fa-trash-o" ></div> <a href="dotCIbuildMetrics">Build Metrics</a>
      </paper-icon-item>
      {this._contextMenu()}
    </div> 
  },
  _contextMenu(){
    var route = this.props.routerState.params['widget'] || 'dotCIbuildHistory';
    switch(route){
      case 'dotCIbuildHistory':
        return(<paper-icon-item>
          <div className="fa fa-rocket" ></div> <a href="build?delay=0sec">Build Now</a>
        </paper-icon-item>);
        case 'dotCIbuildMetrics':
          return '';
        default: 
          return '';
    }

  },
  _onTabClick(e){
    this.setState({selectedTab: e.currentTarget.getAttribute('data-tabidx')});
  }
});
