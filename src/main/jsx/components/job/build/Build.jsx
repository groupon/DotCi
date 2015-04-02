import React from "react";
import Console from './Console.jsx';
const BuildHeader = React.createClass({
  render(){
    return  <div/>;
  }
});

export default  React.createClass({
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
     actions.currentBuildChanged(this.props.url);
  },
  render(){
    return  (<div>
      <h1> {this.props.build.number}  </h1>
    <Console {...this.props} />
  </div>);
  }
});
