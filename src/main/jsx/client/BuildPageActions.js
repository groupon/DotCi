import ReactDOM from 'react-dom';
import React from 'react';
import {build as fetchBuild, buildLog as fetchBuildLog} from './../api/Api.jsx';
import Build from './../components/job/build/Build.jsx';

function dataChange(build){
  ReactDOM.render(<Build build={build} subBuild="main"/>, document.getElementById('content'));
}
function buildChange(build){
  const actions = build.actions;
  let query = build.query;
  fetchBuild(build.number).then(data => {
    actions.BuildInfoChange(data);
    fetchBuildLog(build.number,"main").then(text => actions.LogChange(text));
  });

}

export default function(build){
  const actions = build.actions;
  actions.BuildInfoChange.onAction = dataChange;
  actions.BuildChange.onAction = buildChange;
  actions.LogChange.onAction = dataChange;
}
