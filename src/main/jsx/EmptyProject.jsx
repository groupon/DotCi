import React from "react";
export default React.createClass( {
  render(){
    return(<div>
      <h4> You haven't stated any builds recently.</h4>
      <a href={window.rootURL+'/mygithubprojects'}> Setup a new DotCi Job </a>
    </div>);
  }
})
