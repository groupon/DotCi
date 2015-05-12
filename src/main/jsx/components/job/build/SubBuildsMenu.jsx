import React from "react";
import Router from 'react-router';
import Immutable from 'immutable';
require('./sub_builds_menu.less');
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
      const classes = "ui item " + (this.props.selectedBuild === build? "active" : '');
      return <Router.Link className={classes}  key={build} href='#' to="job-widgets-param" params={{widget:this.props.buildNumber , param: 'dotCI'+build}}>
        <span className= {"subbuild-"+ subBuild.get('result')}>{build}</span>
      </Router.Link>;
    }); 
  }
});
