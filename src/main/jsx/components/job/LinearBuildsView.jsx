import React from 'react';
import BuildRow from './BuildRow.jsx';
var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
require('./linear_builds_view.css')
export default React.createClass({
  render(){
    const builds = this.props.builds.map((build) => <BuildRow key={build.get('number')} build={build}/>);
    return(<span className="builds">
      {builds.toArray()}
    </span>);
  }
});

