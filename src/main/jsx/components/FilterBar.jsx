import React from 'react';
export default  React.createClass({
  render(){
    return <paper-input label="Filter... " onInput={this._onChange}></paper-input>
  },
  _onChange(event){
    this.props.onChange(event.target.value);
  }
});
