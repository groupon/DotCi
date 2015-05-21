import React from 'react';
export default React.createClass({
  render(){
    return <i className={"fa  full-width "+ this._resultIcon()}></i>;
  },
  _resultIcon(){
    switch(this.props.result){
      case "FAILURE": return "fa-times";
      case "SUCCESS": return "fa-check-circle";
      case "IN_PROGRESS": return "fa-spinner";
      case "ABORTED": return "fa-ban";
    }
  }
})
