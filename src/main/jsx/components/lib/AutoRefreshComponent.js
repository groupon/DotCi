import React from 'react';
export const Job = component();
export const Build = component();
function component(){
  return React.createClass({
    componentDidMount(){
      this.refreshTimer = setInterval(this.props.refreshFunction, this.props.refreshInterval);
    },
    componentWillUnmount(){
      clearInterval(this.refreshTimer);
    },
    render(){
      return this.props.component;
    }
  });
}
