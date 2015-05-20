import React from 'react';
import LinearBuildsView from './LinearBuildsView.jsx';
import BuildRow from './BuildRow.jsx';
import {OrderedMap,List} from 'immutable';
require('./grouped_builds_view.css')
const PipeLineBuild =  React.createClass({
  render(){
    return <span className="grouped-builds">
      <div>
        <BuildRow build={this.props.builds.get(0)} />
        <div className="pipeline">
          {this._getSteps()} 
        </div>
      </div>
    </span>
  },
  _getSteps(){
    return this.props.builds.map(build => <BuildRow key={build.get('number')} build={build}/> )
  }
});


export default React.createClass({
  render(){
    const sourceBuilds = this.props.builds.filter(build => !this._isTriggeredBuild(build))
    .reduce((map,build) => map.set(build.get('number')+'',List.of(build)), OrderedMap());
    const groupedBuilds = this.props.builds.filter(build => this._isTriggeredBuild(build)).reduce((map,build) => {
      const sourceBuildNumber = this._sourceBuildNumber(build);
      const builds =map.get(sourceBuildNumber);
      return map.set(sourceBuildNumber, builds.push(build));
    },sourceBuilds)

    const buildGroups = groupedBuilds.map((builds,buildNumber) => <PipeLineBuild key={buildNumber} builds={builds}/>);
    return(<span>{buildGroups.toArray()}</span>);
  },
  _isTriggeredBuild(build) {
    return build.get('cause').get('name') === 'UPSTREAM';
  },
  _sourceBuildNumber(build){
    return build.get('parameters').filter(param => param.get('name')==='SOURCE_BUILD').get(0).get('value');
  }
});
