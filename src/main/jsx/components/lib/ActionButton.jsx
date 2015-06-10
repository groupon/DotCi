import React from "react";
export default  React.createClass({
  render(){
    const styles =  "circular ui icon button hint--top" + (this.props.primary? " green ":"")+ (this.props.className || "");
    return (<a ref="button" className={styles} data-hint={this.props.tooltip}  href={this.props.href|| '#'} onClick={this._onClick}>
      <i className={this.props.icon}></i>
    </a>);
  },
  _onClick(e){
    this.disable();
    this.props.onClick(e);
  },
  disable() {
    this.refs.button.getDOMNode().classList.add('disabled');
  }
});
