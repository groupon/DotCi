import React from 'react';
import BuildRow from './BuildRow.jsx';
var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
require('./linear_builds_view.css')
export default React.createClass({
  render(){
    return(<span className="builds">{this._builds()}</span>);
  },
  _builds(){
    const builds = this.props.builds.map((build) => <BuildRow small={this.props.small} key={build.get('number')} build={build}/>);
    if(this.props.small){
      return <ReactCSSTransitionGroup component="ul" className="table-view" transitionName="build-transition">
        {builds.toArray()}
      </ReactCSSTransitionGroup>
    }
    return <ReactCSSTransitionGroup transitionName="build-transition">
      {builds.toArray()}
    </ReactCSSTransitionGroup>
  }
});

