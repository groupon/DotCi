import React from "react";
import Router from 'react-router';
import Immutable from 'immutable';
import BuildProgressBar from './../BuildProgressBar.jsx';
require('./sub-builds-menu.css');
export default  React.createClass({
  render(){
    return this._isMultiConfig()? 
      <div className="ui secondary pointing menu">
        {this._subBuilds()}
      </div> : <span/>;
  },
  _isMultiConfig(){
    return this.props.axisList.count() > 1;
  },
  _subBuilds(){
    return this.props.axisList.map(subBuild =>{
      const build = subBuild.get('script');
      const classes = " item " + (this.props.selectedBuild === build? "active" : '');
      return <span key={build} className="item">
        <BuildProgressBar build={subBuild} />
        <Router.Link className={classes}   href='#' to="job-widgets-param" params={{widget:this.props.buildNumber , param: 'dotCI'+build}}>
          <span className= {"subbuild-"+ subBuild.get('result')}>{build}</span>
          <span className="subbuild-duration">{subBuild.get('duration')?Math.floor(subBuild.get('duration')/ 60000) +' mins': '-'}</span> 
        </Router.Link>
      </span>
    }); 
  }
});
