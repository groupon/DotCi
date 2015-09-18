import React from "react";
import ReactDOMServer from "react-dom/server";
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import BuildHistory from './../models/Model.js'
export default function renderServer(builds,filters){
  const buildHistory = new BuildHistory();

  const filtersA = [];
  filters.forEach(b=> filtersA.push(b));

  buildHistory.filters = filtersA;
  const buildsA = [];
  builds.forEach((b,i)=> buildsA.push(b));
  buildHistory.builds = buildsA;
  return ReactDOMServer.renderToString(<BuildHistoryPage buildHistory={buildHistory}/>) ;
}
