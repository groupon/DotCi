import React from "react";
import './app-mobile.css'
import FluxComponent from 'flummox/component';
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import {RouteHandler} from 'react-router';
export default  React.createClass({
  getInitialState(){
    return {active: "job"}
  },
  render(){
    return  <div>
      <div className="content">
        <div className="card">
          {this._content()}
        </div>
        <nav className="bar bar-tab">
          <a data-tab="job" 
            className= {"tab-item "+ (this.state.active === 'job'? 'active': '')} 
            onClick={this._tabClick} href="#">
            <span className="icon icon-list"></span>
            <span className="tab-label">Job</span>
          </a>
          <a data-tab="recent-projects" 
            className= {"tab-item "+ (this.state.active === 'recent-projects'? 'active': '')} 
            onClick={this._tabClick} href="#">
            <span className="icon icon-person"></span>
            <span className="tab-label">Your History</span>
          </a>
        </nav>
      </div> 
    </div>
  },
  _tabClick(e){
    this.replaceState( {active: e.currentTarget.getAttribute('data-tab')});
  },
  _content(){
    return this.state.active === 'job'?
    (window.emptyProject? <EmptyProject/>:<RouteHandler   {...this.props}/>):
      <RecentProjects flux={this.props.flux} />;
  }
})
