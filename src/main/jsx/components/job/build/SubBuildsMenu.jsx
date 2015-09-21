import React from "react";
import BuildProgressBar from './../BuildProgressBar.jsx';
require('./sub-builds-menu.css');
export default  React.createClass({
  render(){
    if(this._isMultiConfig()){
      const selected = this.props.axisList.findIndex(subBuild => this.props.selectedBuild === subBuild.script)
      return <paper-tabs selected={selected}>
        {this._subBuilds()}
      </paper-tabs> 
    }

    return <span/>;
  },
  _isMultiConfig(){
    return this.props.axisList.length > 1;
  },
  _subBuilds(){
    return this.props.axisList.map(subBuild =>{
      const build = subBuild.script;
      return <paper-tab key={build}>
        <BuildProgressBar build={subBuild} />
        <a   href={'dotCI'+build}>
          <span className= {"subbuild-"+ subBuild.result}>{build}</span>
          <span className="subbuild-duration">{subBuild.duration?Math.floor(subBuild.duration/ 60000) +' mins': '-'}</span> 
        </a>
      </paper-tab>
    }); 
  }
});
