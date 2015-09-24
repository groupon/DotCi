import React from 'react';
export default function(component,refreshFunction){
  return React.createClass({
    componentDidMount(){
      this.refreshTimer = setInterval(refreshFunction, 5000);
    },
    componentWillUnmount(){
      clearInterval(this.refreshTimer);
    },
    render(){
      return component;
    }
  });
}
