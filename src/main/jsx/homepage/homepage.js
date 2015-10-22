import ReactDOM from 'react-dom';
import React from 'react';
import qwest from 'qwest';
import {stringify} from 'qs';
require('./homepage.css');
var Build = ({build}) => {
  return <paper-card  class="fancy">
  <div className="card-content">
  <div className="title"> title </div>
  <div className="medium">Title</div>
  <div className="small">subtitle</div>
  </div>

  <div className="card-actions">
  <paper-button>Some action</paper-button>
  </div>
  </paper-card>;
}
var HomePage = ({builds}) => {
  return <div className="layout horizontal wrap">
  {builds.map(build => <Build key={build.number} build={build}/>)}
  </div>;
};
window.addEventListener('WebComponentsReady', ()=>{
  const params = {tree:'*,userBuilds[*,commit[*]]'};
  const fetchUrl = `${rootURL}/${viewUrl}/api/json?${stringify(params)}`;
  qwest.get(fetchUrl,{},{responseType: 'json'}).then((res)=>{
    ReactDOM.render(<HomePage builds={res.userBuilds}/>, document.getElementById("homepage"));
  });
  // ReactDOM.render(<HomePage/>, document.getElementById("homepage"));
});
