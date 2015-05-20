import React from 'react';
import BuildRow from './BuildRow.jsx';
export default React.createClass({
  render(){
    return <div className="step pipeline-step ">
      <i className="icon">{this._dotCiStep()}</i>
      <span className="content"> <BuildRow build={this.props.build} compact/></span>
    </div>
  },
  _dotCiStep(){
    return  this.props.build.get('cause').get('name') === 'UPSTREAM'? this.props.build.get('parameters').filter(param => param.get('name')==='DOTCI_STEP').get(0).get('value'): '';
  }
});
