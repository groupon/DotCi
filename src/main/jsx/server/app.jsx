import React from "react";
import ReactDOMServer from "react-dom/server";
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import BuildHistory from './../models/BuildHistory.js'
export default function renderServer(builds){
  const buildHistory = new BuildHistory();
  const buildsA = [];
  builds.forEach((b,i)=> buildsA.push(b));
  buildHistory.builds = buildsA;
  return ReactDOMServer.renderToString(<BuildHistoryPage buildHistory={buildHistory}/>) ;
}
