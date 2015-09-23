__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from "react-dom";
import page from 'page';
import  Job from './models/Job.js';
import Build from './models/Build.js';
import bindBuildHistoryActions from './client/BuildHistoryPageActions.js';
import bindBuildMetricsActions from './client/BuildMetricsPageActions.js';
import bindBuildActions from './client/BuildPageActions.js';
import {isNumeric} from './util/Number.js';
require('./app.css');
require('./polyfills.js');
//wiring
window.onload = function (){

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
    buildHistory.actions.QueryChange({filter: 'All', limit: 50});
  });
  page('/dotCIbuildMetrics', function () {
    buildMetrics.actions.QueryChange({filter: 'All', limit: 50});
  });
  page('/:buildNumber',(ctx)=>{
    const {buildNumber} = ctx.params;
    const subBuild = 'main';
    buildPage(buildNumber,subBuild,ctx.hash);
  });
  page('/:buildNumber/:subBuild',(ctx)=>{
    const {buildNumber,subBuild} = ctx.params;
    buildPage(buildNumber,subBuild,ctx.hash);
  });
  page();
  function buildPage(buildNumber,subBuild,hash){
    build.number = buildNumber;
    if(hash){
      build.selectedLine = hash.replace("#",'');
    }else{
      build.selectedLine = "0";
    }
    if(isNumeric(buildNumber)){
      build.actions.BuildChange({buildNumber,subBuild});
    }else{
      window.location = ctx.canonicalPath;
    }
  }
}
function getRootPath(){
  const jobPath = jobUrl.replace(rootURL,'');
  return window.location.pathname.split(jobPath)[0] +jobPath;
}
