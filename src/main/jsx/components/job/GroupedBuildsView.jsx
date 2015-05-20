import React from 'react';
import LinearBuildsView from './LinearBuildsView.jsx';
import {OrderedMap,List} from 'immutable';
import BuildStep from './BuildStep.jsx';
import Avatar from '../lib/Avatar.jsx';
require('./grouped_builds_view.css')
const PipeLineBuild =  React.createClass({
  render(){
    const build =this.props.builds.get(0);
    let {commit} = build.toObject();
    let {message,commitUrl,shortSha,committerName,branch, avatarUrl} = commit.toObject();
    return <span className="pipeline-steps"> 
      <div> {message} </div>
      <div className="build-row--committer">
        <Avatar avatarUrl={avatarUrl} />
        <span>{committerName}</span>
      </div>
      <div className="ui steps fluid ">
        {this.props.builds.map(build =><BuildStep key={build.get('number')} build={build}/> )}
      </div>
    </span>
  },
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
    return(<span className="pipeline-build-view">{buildGroups.toArray()}</span>);
  },
  _isTriggeredBuild(build) {
    return build.get('cause').get('name') === 'UPSTREAM';
  },
  _sourceBuildNumber(build){
    return build.get('parameters').filter(param => param.get('name')==='SOURCE_BUILD').get(0).get('value');
  }
});
