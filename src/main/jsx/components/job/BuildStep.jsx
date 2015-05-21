import React from 'react';
import BuildRow from './BuildRow.jsx';
export default React.createClass({
  render(){
    return <div className="pipeline-step ">
      {this._isDownstreamBuild()? this._step() : <span/>}
      <span className="content"> <BuildRow build={this.props.build} compact/></span>
    </div>
  },
  _step(){
    return <span><i className="right-arrow fa fa-arrow-right"></i> <div>{this._dotCiStep()}</div></span>
  },
  _dotCiStep(){
    return  this._isDownstreamBuild() ? this.props.build.get('parameters').filter(param => param.get('name')==='DOTCI_STEP').get(0).get('value'): '';
  },
  _isDownstreamBuild(){
    return this.props.build.get('cause').get('name') === 'UPSTREAM';
  }
});
