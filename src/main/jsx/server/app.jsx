import React from "react";
import BuildHistory from './../components/job/BuildHistory.jsx';
import Flux from './../Flux.jsx';
export default function renderServer(builds){
  const buildHistory = {
    filters: [],
    builds: builds
  }
  return React.renderToString(<BuildHistory buildHistory={buildHistory}/>) ;
}
