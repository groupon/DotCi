import ReactDOM from 'react-dom';
import React from 'react';
import {build as fetchBuild} from './../api/Api.jsx';
import Build from './../components/job/build/Build.jsx';

function dataChange(build){
  ReactDOM.render(<Build build={build} subBuild="main"/>, document.getElementById('content'));
}
function queryChange(build){
  const actions = build.actions;
  let query = build.query;
  fetchBuild(build.number).then(data => {
    actions.DataChange(data);
  });
}

export default function(build){
  const actions = build.actions;
  actions.DataChange.onAction = dataChange;
  actions.QueryChange.onAction = queryChange;
}
