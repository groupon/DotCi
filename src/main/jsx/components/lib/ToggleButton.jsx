import React from 'react';
export default React.createClass({
  render(){
    return <div data-hint={this.props.tooltip} className="hint--bottom ">
      <paper-toggle-button onClick={this._onClick}/>
    </div>
  },
  _onClick(e){
    this.props.onClick(e.currentTarget.checked);
  }
});
