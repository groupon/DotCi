import React from 'react';
export default React.createClass({
  render(){
      return <img  className="ui avatar image" alt={this.props.emailDigest}  src={"https://secure.gravatar.com/avatar/"+this.props.emailDigest+".png"}/>;
  }
});
