import React from 'react';
import LinearBuildsView from './LinearBuildsView.jsx';
import BuildStep from './BuildStep.jsx';
import Avatar from '../lib/Avatar.jsx';
import sortBy from 'ramda/src/sortBy'
require('./grouped_builds_view.css')
const PipeLineBuild =  React.createClass({
  getInitialState(){
    return {detail: 0}
  },
  render(){
    const build =this.props.builds[0];
    let {commit} = build;
    let {message,commitUrl,shortSha,committerName,branch, avatarUrl} = commit;
    const builds = sortBy(b=> b.number)(this.props.builds);
    return <span className="pipeline-build"> 
      <span className="commit">
        <div> {message} </div>
        <span>
          <Avatar avatarUrl={avatarUrl} />
          <span>{committerName}</span>
        </span>
        <div> {branch} </div>
        <div><iron-icon icon="github:octoface"/><a className="github-link link-no-decoration" href={commitUrl}>{shortSha}</a></div>
      </span>
      <div className="pipeline-steps">
        {builds.map(build =><BuildStep ref={build.number} onClick={this._onBuildStepClick} detail={this._isDetail(build)} 
          key={build.number} build={build}/> )}
      </div>
    </span>
  },
  _isDetail(build){
    return this.state.detail === build.number;
  },
  _onBuildStepClick(number){
    this.setState({detail: number})
  }
});


export default React.createClass({
  render(){
    const sourceBuilds = this.props.builds
    .filter(build => !this._isTriggeredBuild(build))
    .reduce((map,build) => {
      map[build.number+'']=[build]
      return map;
    }, {});
    const groupedBuilds = this.props.builds.filter(build => this._isTriggeredBuild(build)).reduce((map,build) => {
      const sourceBuildNumber = this._sourceBuildNumber(build);
      const builds =map[sourceBuildNumber];
      if(!builds){ //Source Build might not be fetched && show only complete pipelines
        return map;
      }
      builds.push(build);
      return map;
    },sourceBuilds)

    const buildGroups = []
    for (var buildNumber in groupedBuilds){
      buildGroups.push(<PipeLineBuild key={buildNumber} builds={groupedBuilds[buildNumber]}/>);
    } 
    return(<span className="pipeline-build-view">{buildGroups}</span>);
  },
  _isTriggeredBuild(build) {
    return build.cause.name === 'UPSTREAM';
  },
  _sourceBuildNumber(build){
    return build.parameters.filter(param => param.name==='SOURCE_BUILD')[0].value;
  }
});
