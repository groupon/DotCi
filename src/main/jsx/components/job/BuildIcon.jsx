import React from 'react';
export default React.createClass({
  render(){
    return <iron-icon icon={this._resultIcon()}></iron-icon>;
  },
  _resultIcon(){
    switch(this.props.result){
      case "FAILURE": return "close";
      case "SUCCESS": return "check";
      case "IN_PROGRESS": return "autorenew";
      case "ABORTED": return "ban";
    }
  }
})
