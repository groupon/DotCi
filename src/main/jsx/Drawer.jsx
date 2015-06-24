import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import  CustomAttributes from './components/mixins/CustomAttributes.jsx';

export default React.createClass( {
  mixins: [CustomAttributes],
  getInitialState(){
    return {selectedTab: "1"}
  },
  render(){
    return <div>
      <paper-tabs ref="ca-1" attrs={{selected: this.state.selectedTab}}>
        <paper-tab data-tabidx="0" onClick={this._onTabClick}><i className="icon-text fa fa-user"></i> Recent Builds</paper-tab>
        <paper-tab data-tabidx="1" onClick={this._onTabClick}> Current</paper-tab>
      </paper-tabs>
      <div>
        {this.state.selectedTab === "0"? <RecentProjects flux={this.props.flux}/> : this._currentMenu()}
      </div>

    </div>
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
