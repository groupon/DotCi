import React from "react";
import Router from 'react-router';
export default React.createClass({
  mixins: [Router.State],
  render(){
    return <b> {this.getParams().buildNumber} </b>;
  }
});
