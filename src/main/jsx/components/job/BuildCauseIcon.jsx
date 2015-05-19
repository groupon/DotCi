import React from 'react';
export default React.createClass({
  render(){
    return <i className={"fa  fa-lg "+ this._resultIcon()}></i>;
  },
  _resultIcon(){
    switch(this.props.cause){
      case "MANUAL": return "fa-user";
      case "GITHUB_PUSH": return "mega-octicon octicon-git-commit";
      case "GITHUB_PULL_REQUEST": return "mega-octicon octicon-git-pull-request";
      case "UPSTREAM": return "fa-arrow-circle-o-up";
      default: return "fa-question-circle";
    }
  }
})
