import React from "react";
import Router from 'react-router';
require('./build.less');
export default React.createClass({
  mixins: [Router.State],
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.getParams().buildNumber);
  },
  render(){
    if( this.props.build)
      return <span id="buildLog"><pre> {this._renderLog(this.props.build.log)}</pre></span>;
    return <span/>;
  },
  _renderLog(logLines){
    return logLines.map(log => <p><a/>{log}</p>); 
  }

});
