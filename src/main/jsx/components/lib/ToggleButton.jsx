import React from 'react';
export default React.createClass({
  render(){
    return <div data-hint={this.props.tooltip} className="hint--bottom ">
      <paper-toggle-button onClick={this._onChange}/>
      {this.props.children}
    </div>
  }
});
