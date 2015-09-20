__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from "react-dom";
import page from 'page';
import  Job from './models/Job.js';
import Build from './models/Build.js';
import bindBuildHistoryActions from './client/BuildHistoryPageActions.js';
import bindBuildMetricsActions from './client/BuildMetricsPageActions.js';
import bindBuildActions from './client/BuildPageActions.js';
require('./app.css');
import Drawer from './Drawer.jsx';
//wiring
window.onload = function (){

  const buildHistory = new Job();
  bindBuildHistoryActions(buildHistory);

  const buildMetrics = new Job();
  bindBuildMetricsActions(buildMetrics);

  const build = new Build();
  bindBuildActions(build);

  page.base(getRootPath());
  page('/','dotCIbuildHistory');
  page('dotCIbuildHistory', function () {
    buildHistory.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page('dotCIbuildMetrics', function () {
    buildMetrics.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page(':build',(ctx)=>{
    build.number = ctx.path;
    build.actions.BuildChange(ctx.path);
  });
  page();
}
function getRootPath(){
  const jobPath = jobUrl.replace(rootURL,'');
  return window.location.pathname.split(jobPath)[0] +jobPath;
}
