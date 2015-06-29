import React from "react";
import Router from 'react-router';
import Immutable from 'immutable';
import CustomAttributes from './../../mixins/CustomAttributes.jsx';
import BuildProgressBar from './../BuildProgressBar.jsx';
require('./sub-builds-menu.css');
export default  React.createClass({
  mixins: [CustomAttributes],
  render(){
    if(this._isMultiConfig()){
      const selected = this.props.axisList.findIndex(subBuild => this.props.selectedBuild === subBuild.get('script'))
      return <paper-tabs ref="ca-subbuilds" attrs={{selected}}>
        {this._subBuilds()}
      </paper-tabs> 
    }

    return <span/>;
  },
  _isMultiConfig(){
    return this.props.axisList.count() > 1;
  },
  _subBuilds(){
    return this.props.axisList.map(subBuild =>{
      const build = subBuild.get('script');
      return <paper-tab key={build}>
        <BuildProgressBar build={subBuild} />
        <Router.Link   href='#' to="job-widgets-param" params={{widget:this.props.buildNumber , param: 'dotCI'+build}}>
          <span className= {"subbuild-"+ subBuild.get('result')}>{build}</span>
          <span className="subbuild-duration">{subBuild.get('duration')?Math.floor(subBuild.get('duration')/ 60000) +' mins': '-'}</span> 
        </Router.Link>
      </paper-tab>
    }); 
  }
});
