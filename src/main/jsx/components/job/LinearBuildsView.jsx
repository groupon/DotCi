import React from 'react';
import BuildRow from './BuildRow.jsx';
export default React.createClass({
  render(){
    const builds = this.props.builds.map((build) => <BuildRow key={build.get('number')} build={build}/>);
    return(<span className="builds">{builds.toArray()}</span>);
  }
});

