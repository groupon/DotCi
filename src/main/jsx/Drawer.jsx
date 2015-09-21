import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import BuildActions  from './components/job/build/BuildActions.jsx'
import Dialog from './components/lib/Dialog.jsx'; 
import JobActions from './components/job/JobActions.jsx';
import PageLink from './components/lib/PageLink.jsx';
export default React.createClass( {
  getInitialState(){
    return {view: "current"}
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
        <PageLink   href= '/dotCIbuildHistory'> 
          <iron-icon  icon="list"/>Build History
        </PageLink>
      </paper-icon-item>
      <paper-icon-item>
        <PageLink   href='/dotCIbuildMetrics'> 
          <iron-icon  icon="trending-up"/> Build Metrics
        </PageLink>
      </paper-icon-item>
      {this._contextMenu()}
    </div> 
  },
  _contextMenu(){
    switch(this.props.menu){
      case 'job':
        return <JobActions/>;
      default: 
        return this._buildMenu();
    }

  },
  _buildMenu(){
    return <BuildActions  {...this.props.build}/>;
  }
});
