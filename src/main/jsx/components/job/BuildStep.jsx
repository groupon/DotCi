import React from 'react';
import BuildRow from './BuildRow.jsx';
require('./build_step.css');
export default React.createClass({
  render(){
    return <div className="pipeline-step">
      {this._isDownstreamBuild()? this._step() : <span/>}
      {this.props.detail? <span className="content"> <BuildRow build={this.props.build} compact/> </span>: this._compact()}
    </div>
  },
  _compact(){
    return <div className={"ui button content compact-"+this.props.build.get('result')}  data-number={this.props.build.get('number')} onClick={this._onClick} >{this._dotCiStep()} </div>
  },
  _onClick(e){
    this.props.onClick(parseInt(e.target.getAttribute('data-number')));
  },
  _step(){
    return <i className="right-arrow fa fa-arrow-right"/>;
  },
  _dotCiStep(){
    return  this._isDownstreamBuild() ? this.props.build.get('parameters').filter(param => param.get('name')==='DOTCI_STEP').get(0).get('value'): this.props.build.get('number');
  },
  _isDownstreamBuild(){
    return this.props.build.get('cause').get('name') === 'UPSTREAM';
  }
});
