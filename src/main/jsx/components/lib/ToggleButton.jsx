import React from 'react';
export default React.createClass({
  getInitialState(){
    return {active: false}
  },
  render(){
    return <div data-hint={this.props.tooltip} className={"ui toggle button hint--top " +( this.state.active? "active":"") } onClick={this._onClick}>
      {this.props.children}
    </div>
  },
  _onClick(e){
    e.preventDefault();
    this.props.onClick(!this.state.active);
    this.setState({active: !this.state.active});
  }
});
