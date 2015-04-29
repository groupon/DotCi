import React from "react";
export default  React.createClass({
  render(){
    return (<a className="circular ui icon button hint--top"  data-hint={this.props.tootip}  href="#" onClick={this.props.onClick}>
      <i className={this.props.icon}></i>
    </a>);
  }
});
