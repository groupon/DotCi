import React from 'react';
export default  React.createClass({
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
