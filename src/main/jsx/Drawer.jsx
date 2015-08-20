import React from "react";
import {Link} from 'react-router';
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import  CustomAttributes from './components/mixins/CustomAttributes.jsx';
import BuildActions  from './components/job/build/BuildActions.jsx'
import Dialog from './components/lib/Dialog.jsx'; 
import { TransitionHook } from 'react-router';
import JobActions from './components/job/JobActions.jsx';

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
    return {view: "current"}
  },
  routerWillLeave (nextState, router) {
    debugger
  },
  render(){
    return <div>
      <paper-toolbar id="drawerToolbar">
        <paper-icon-button  src={`${resURL}/logo.png`} disabled />
        <div className="title">{this._isCurrent()?"Current": "Recent"}</div>
        <paper-icon-button onClick={this._onJobChange} icon={this._isCurrent()?"expand-more": "expand-less"}></paper-icon-button>
      </paper-toolbar>
      {this._isCurrent()? this._currentMenu(): <RecentProjects flux ={this.props.flux}/>}
    </div>
  },
  _onJobChange(e){
    if(this._isCurrent()){
      this.replaceState({view: 'recentProjects'})
    }else{
      this.replaceState({view: 'current'})
    }
  },
  _isCurrent(){
    return this.state.view === 'current';
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
        return <JobActions/>;
      case 'dotCIbuildMetrics':
        return '';
      default: 
        return this._buildMenu();
    }

  },
  _buildMenu(){
    const currentBuild = this.props.job.get('build');
    return currentBuild? <BuildActions flux={this.props.flux} {...currentBuild.toObject()} /> : '';
  }
});
