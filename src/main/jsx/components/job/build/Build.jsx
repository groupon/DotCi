import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
export default  React.createClass({
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
     actions.currentBuildChanged(this.props.url);
  },
  render(){
    return this.props.build? (<div>
      <BuildRow  {...this.props.build}/>
      <Console {...this.props} />
  </div>):<div/>;
  }
});
