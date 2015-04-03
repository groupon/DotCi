import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
require('./build.less');
export default  React.createClass({
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.props.url);
  },
  render(){
    return this.props.build? (<div id="build">
      {this._buildActions()}
      <BuildRow  {...this.props.build}/>
      <Console {...this.props} />
    </div>):<div/>;
  },
  _buildActions(){
    return this.props.build.cancelable? this._cancelButton():this._restartButton()
  },
  _restartButton(){
    return <a className="circular ui icon button" href="#" onClick={this._restartBuild}>
      <i className="fa fa-refresh"></i>
    </a>
  },
  _cancelButton(){
    return <div className="circular ui icon button" href="#" onClick={this._cancelBuild}>
      <i className="fa fa-times-circle-o"></i>
    </div>
  },
  _cancelBuild(e){
    e.preventDefault();
    const appActions = this.props.flux.getActions('app');
    appActions.cancelBuild(this.props.build.cancelUrl);
  },
  _restartBuild(e){
    e.preventDefault();
    window.location = window.location + '/rebuild/parameterized';
  }
});
