import React from 'react';
import BuildIcon from './BuildIcon.jsx';
import BuildRow from './BuildRow.jsx';
export default React.createClass({
  render(){
    return <div className="step pipeline-step ">
      <i className="icon"></i>
      <span className="content"> <BuildRow build={this.props.build} compact/></span>
    </div>
  }
});
