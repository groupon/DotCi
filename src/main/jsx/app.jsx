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
require('./polyfills.js');
//wiring
const begin = function (){

  const buildHistory = new Job();
  bindBuildHistoryActions(buildHistory);

  const buildMetrics = new Job();
  bindBuildMetricsActions(buildMetrics);

  const build = new Build();
  bindBuildActions(build);
  let rootPath = getRootPath()
  if(rootPath.endsWith('/')){
    rootPath = rootPath.substring(0,rootPath.length-1)
  }
  page.base(rootPath);
  page('/','/dotCIbuildHistory');
  page('/dotCIbuildHistory', function () {
    buildHistory.actions.QueryChange(buildHistory.query);
  });
  page('/dotCIbuildMetrics', function () {
    buildMetrics.actions.QueryChange(buildMetrics.query);
  });
  page(/^\/(\d+)\/?(dotCI([^\/]+)\/?)?$/, buildPage);
  page();
  function buildPage(ctx){
    const buildNumber = ctx.params[0];
    const subBuild= ctx.params[2] || "main";
    if(ctx.hash){
      build.selectedLine = ctx.hash.replace("#",'');
    }else{
      build.selectedLine = "0";
    }
    build.actions.BuildChange({buildNumber,subBuild});
  }
}
function getRootPath(){
  const jobPath = jobUrl.replace(rootURL,'');
  return window.location.pathname.split(jobPath)[0] +jobPath;
}
window.addEventListener('WebComponentsReady', begin);
