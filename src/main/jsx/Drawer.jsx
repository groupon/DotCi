import React from "react";
import {Link} from 'react-router';
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import  CustomAttributes from './components/mixins/CustomAttributes.jsx';
import BuildActions  from './components/job/build/BuildActions.jsx'
import Dialog from './components/lib/Dialog.jsx'; 
import { TransitionHook } from 'react-router';

export default React.createClass( {
  mixins: [CustomAttributes, TransitionHook],
  childContextTypes: {
    router: React.PropTypes.func
  },
  getChildContext () {
    return {
      router: this.props.router
    }
  },
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
        <Link   to="job-widgets" params={{widget: 'dotCIbuildHistory'}}> 
          <iron-icon  icon="list"/>Build History
        </Link>
      </paper-icon-item>
      <paper-icon-item>
        <Link   to="job-widgets" params={{widget: 'dotCIbuildMetrics'}}> 
          <iron-icon  icon="trending-up"/> Build Metrics
        </Link>
      </paper-icon-item>
      {this._contextMenu()}
    </div> 
  },
  _contextMenu(){
    var route = this.props.routerState.params['widget'] || 'dotCIbuildHistory';
    switch(route){
      case 'dotCIbuildHistory':
        return(<paper-icon-item>
          <iron-icon icon="send" /><a href="build?delay=0sec">Build Now</a>
        </paper-icon-item>);
        case 'dotCIbuildMetrics':
          return '';
        default: 
          return this._buildMenu();
    }

  },
  _onTabClick(e){
    this.setState({selectedTab: e.currentTarget.getAttribute('data-tabidx')});
  },
  _buildMenu(){
    const currentBuild = this.props.job.get('build');
    return currentBuild? <BuildActions flux={this.props.flux} {...currentBuild.toObject()} /> : '';
  }
});
