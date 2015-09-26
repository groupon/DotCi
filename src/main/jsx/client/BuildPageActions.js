import ReactDOM from 'react-dom';
import React from 'react';
import {build as fetchBuild, buildLog as fetchBuildLog,cancelBuild as cancelBuildApi} from './../api/Api.jsx';
import BuildPage from './../pages/BuildPage.jsx';
import Drawer from './../Drawer.jsx';
import page from 'page';
import {Build as AutoRefreshComponent} from './../components/lib/AutoRefreshComponent.js';

function dataChange(build){
  const buildPage =<BuildPage build={build} subBuild={build.subBuild}/>
  if(build.inProgress){
    const refreshFunction = ()=>{
      build.actions.BuildReload({buildNumber:build.number,subBuild:build.subBuild});
    };
    ReactDOM.render(<AutoRefreshComponent component={buildPage} refreshFunction={refreshFunction}/>, document.getElementById('content'));
  }else{
    ReactDOM.render(buildPage, document.getElementById('content'));
  }
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
function lineSelect(build){
  window.history.pushState(null, null, '#'+build.selectedLine);
}

export default function(build){
  const actions = build.actions;
  actions.BuildInfoChange.onAction = dataChange;
  actions.LogChange.onAction = dataChange;
  actions.CancelBuild.onAction = cancelBuild;
  actions.BuildChange.onAction = buildChange;
  actions.BuildReload.onAction = buildChange;
  actions.LineSelect.onAction = lineSelect;
}
