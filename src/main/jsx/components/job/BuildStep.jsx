import React from 'react';
import BuildRow from './BuildRow.jsx';
require('./build_step.css');
export default React.createClass({
  render(){
    return <div className="pipeline-step">
      {this._isDownstreamBuild()? this._arrow() : <span/>}
      <div className="content">
        {this.props.detail?  <BuildRow key="build-row" build={this.props.build} compact/> : this._compact()} 
      </div>
    </div>
  },
  _compact(){
    return <div className={"summary ui button circular compact-"+this.props.build.result} key="summary"  
      data-number={this.props.build.number} onClick={this._onClick} >{this._dotCiStep()}</div>
  },
  _onClick(e){
    this.props.onClick(parseInt(e.target.getAttribute('data-number')));
  },
  _arrow(){
    return <iron-icon icon="arrow-forward"/>
  },
  _dotCiStep(){
    return  this._isDownstreamBuild() ? this.props.build.parameters
    .filter(param => param.name ==='DOTCI_STEP')[0].value: this.props.build.number;
  },
  _isDownstreamBuild(){
    return this.props.build.cause.name === 'UPSTREAM';
  }
});
