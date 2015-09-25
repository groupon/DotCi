import React from 'react';
export default React.createClass({
  render(){
    const toggleButton = this.props.checked ? <paper-toggle-button checked  onClick={this._onClick}/> 
      :  <paper-toggle-button onClick={this._onClick}/>;
      return <div data-hint={this.props.tooltip} className="hint--bottom ">
        {toggleButton}
      </div>
  },
  _onClick(e){
    this.props.onClick(e.currentTarget.checked);
  }
});
