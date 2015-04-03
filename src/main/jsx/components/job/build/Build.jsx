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
      <BuildRow  {...this.props.build}/>
      {this._buildActions()}
      <Console {...this.props} />
    </div>):<div/>;
  },
  _buildActions(){
    return <div>
      {this.props.cancelable? this._cancelButton():''}
      <a className="circular ui icon button" href="#" onClick={this._restartBuild}>
        <i className="fa fa-refresh"></i>
      </a>
    </div>
  },
  _cancelButton(){
    return <div className="circular ui icon button">
      <i className="fa fa-times-circle-o"></i>
    </div>
  },
  _restartBuild(e){
    e.preventDefault();
    window.location = window.location + '/rebuild/parameterized';
  }
});
