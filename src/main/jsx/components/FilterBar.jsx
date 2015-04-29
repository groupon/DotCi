
import React from 'react';
export default  React.createClass({
  render(){
    return (
      <div id={this.props.id} className="ui icon input">
        <input type="text" onChange={this._onChange} placeholder="Filter..."/>
        <i className="fa fa-filter icon"></i>
      </div>
    );
  },
  _onChange(event){
    this.props.onChange(event.target.value);
  }
});
