import React from 'react';
import LinearBuildsView from './LinearBuildsView.jsx';
import {OrderedMap,List} from 'immutable';
import BuildStep from './BuildStep.jsx';
import Avatar from '../lib/Avatar.jsx';
require('./grouped_builds_view.css')
const PipeLineBuild =  React.createClass({
  getInitialState(){
    return {detail: 0}
  },
  render(){
    const build =this.props.builds.get(0);
    let {commit} = build.toObject();
    let {message,commitUrl,shortSha,committerName,branch, avatarUrl} = commit.toObject();
    return <span className="pipeline-build"> 
      <span className="commit">
        <div> {message} </div>
        <span>
          <Avatar avatarUrl={avatarUrl} />
          <span>{committerName}</span>
        </span>
        <div> {branch} </div>
        <div><i className="octicon octicon-mark-github"></i><a className="github-link link-no-decoration" href={commitUrl}>{shortSha}</a></div>
      </span>
      <div className="pipeline-steps">
        {this.props.builds.sortBy(b => b.get('number')).map(build =><BuildStep ref={build.get('number')} onClick={this._onBuildStepClick} detail={this._isDetail(build)} key={build.get('number')} build={build}/> )}
      </div>
    </span>
  },
  _isDetail(build){
    return this.state.detail === build.get('number');
  },
  _onBuildStepClick(number){
    this.setState({detail: number})
  }
});


export default React.createClass({
  render(){
    const sourceBuilds = this.props.builds.filter(build => !this._isTriggeredBuild(build))
    .reduce((map,build) => map.set(build.get('number')+'',List.of(build)), OrderedMap());
    const groupedBuilds = this.props.builds.filter(build => this._isTriggeredBuild(build)).reduce((map,build) => {
      const sourceBuildNumber = this._sourceBuildNumber(build);
      const builds =map.get(sourceBuildNumber);
      if(!builds){ //Source Build might not be fetched && show only complete pipelines
        return map;
      }
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
