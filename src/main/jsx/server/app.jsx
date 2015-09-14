import React from "react";
import BuildHistory from './../components/job/BuildHistory.jsx';
import Flux from './../Flux.jsx';
export default function renderServer(builds){
  var window = {};
  return React.renderToString(<BuildHistory   tabs={[]} builds={builds} flux={new Flux()}/>);
}
