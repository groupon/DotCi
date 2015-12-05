import React from 'react';
export default  React.createClass({
  render(){
    return <paper-input ref="input" value={this.props.value} label="Filter... " onInput={this._onChange}></paper-input>
  },
  _onChange(event){
    this.props.onChange(event.target.value);
  },
  focus(){
    this.refs.input.focus();
  }
});
