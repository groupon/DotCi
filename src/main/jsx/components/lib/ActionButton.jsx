import React from "react";
export default  React.createClass({
  render(){
    const styles =  "circular ui icon button hint--top" + (this.props.primary? " green":"");
    return (<a ref="button" className={styles} data-hint={this.props.tooltip}  href={this.props.href|| '#'} onClick={this.props.onClick}>
      <i className={this.props.icon}></i>
    </a>);
  },
  disable() {
    this.refs.button.getDOMNode().classList.add('disabled');
  }
});
