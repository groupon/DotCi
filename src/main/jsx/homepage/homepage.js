import ReactDOM from 'react-dom';
import React from 'react';
import qwest from 'qwest';
import BuildRow from './../components/job/BuildRow.jsx'
import {stringify} from 'qs';
var HomePage = ({builds}) => {
  return <div className="layout vertical wrap">
  {builds.map((build,idx) =>  <BuildRow  fullUrl key={idx} build={build}/>)}
  </div>;
};
window.addEventListener('WebComponentsReady', ()=>{
  const params = {tree:'*,userBuilds[*,commit[*]]'};
  const fetchUrl = `${rootURL}/${viewUrl}/api/json?${stringify(params)}`;
  qwest.get(fetchUrl,{},{responseType: 'json'}).then((res)=>{
    ReactDOM.render(<HomePage builds={res.userBuilds}/>, document.getElementById("homepage"));
  });
});
