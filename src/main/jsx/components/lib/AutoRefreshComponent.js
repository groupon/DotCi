import React from 'react';
export const Job = component();
export const Build = component();
function component(){
  return React.createClass({
    componentDidMount(){
      this.refreshTimer = setInterval(this.props.refreshFunction, 5000);
    },
    componentWillUnmount(){
      clearInterval(this.refreshTimer);
    },
    render(){
      return this.props.component;
    }
  });
}
