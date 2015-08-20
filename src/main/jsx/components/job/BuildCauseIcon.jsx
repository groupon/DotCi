import React from 'react';
export default React.createClass({
  render(){
    return <iron-icon icon={this._resultIcon()}/>;
  },
  _resultIcon(){
    switch(this.props.cause){
      case "MANUAL": return "account-circle";
      case "GITHUB_PUSH": return "octicon octicon-git-commit";
      case "GITHUB_PULL_REQUEST": return "octicon octicon-git-pull-request";
      case "UPSTREAM": return "eject";
      default: return "warning";
    }
  }
})
