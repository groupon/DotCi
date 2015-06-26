import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import  CustomAttributes from './components/mixins/CustomAttributes.jsx';
import Dialog from './components/lib/Dialog.jsx';

export default React.createClass( {
  mixins: [CustomAttributes],
  getInitialState(){
    return {selectedTab: "1"}
  },
  render(){
    return <div>
      <paper-toolbar id="drawerToolbar">
        <div className="  title">Current</div>
        <paper-icon-button onClick={this._onJobChange} icon="hardware:keyboard-arrow-down"></paper-icon-button>
      </paper-toolbar>
      {this._currentMenu()}
      <Dialog ref="recentProjectsDialog" heading="Recent Builds" noButtons>
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
    </div> 
  },
  _onTabClick(e){
    this.setState({selectedTab: e.currentTarget.getAttribute('data-tabidx')});
  }
});
