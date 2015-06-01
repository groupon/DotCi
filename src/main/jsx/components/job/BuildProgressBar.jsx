import React from 'react';
require('./build_progress_bar.css');
export default React.createClass({
  render(){
    const {result,duration,estimatedDuration} = this.props.build.toObject();
    return result === 'IN_PROGRESS'? <div className=" ui top attached progress">
      <div className="build-progress-bar bar" style={{width:((duration/estimatedDuration)*100)+"%"}}></div>
    </div>:<span/>
  }
});
