import ReactDOM from 'react-dom';
import React from 'react';
import {build as fetchBuild, buildLog as fetchBuildLog,cancelBuild as cancelBuildApi} from './../api/Api.jsx';
import Build from './../components/job/build/Build.jsx';
import Drawer from './../Drawer.jsx';

function dataChange(build){
  ReactDOM.render(<Build build={build} subBuild={build.subBuild}/>, document.getElementById('content'));
  ReactDOM.render(<Drawer menu="build" build={build}/>, document.getElementById('nav'));
}
async function buildChange(build){
  const actions = build.actions;
  let query = build.query;
  const data =await fetchBuild(build.number);
  actions.BuildInfoChange(data);
  const logText = await fetchBuildLog(build.number,build.subBuild)
  actions.LogChange(logText);
}
async function cancelBuild(build){
  await  cancelBuildApi(build.cancelUrl);
}

export default function(build){
  const actions = build.actions;
  actions.BuildInfoChange.onAction = dataChange;
  actions.LogChange.onAction = dataChange;
  actions.CancelBuild.onAction = cancelBuild;
  actions.BuildChange.onAction = buildChange;
}
