import React from "react";
require('./progress_bar.css');
export default React.createClass({
  render(){
    const progressBar = this.props.visible?  <paper-progress  indeterminate ></paper-progress> : <span className="progressPlaceholder"/>;
    return <div className="progressBar">{progressBar}</div>;
  }
});
