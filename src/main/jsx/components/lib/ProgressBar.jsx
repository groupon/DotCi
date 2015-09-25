import React from "react";
require('./progress_bar.css');
export default React.createClass({
  render(){
    const progressBar = this.props.visible?  <paper-progress  indeterminate ></paper-progress> : <paper-progress value="0" />;
    return <div className="progressBar">{progressBar}</div>;
  }
});
