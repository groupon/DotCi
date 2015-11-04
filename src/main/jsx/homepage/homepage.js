import ReactDOM from 'react-dom';
import React from 'react';
import RecentProjects from './../components/recent_projects/RecentProjects.jsx'
import {stringify} from 'qs';
window.addEventListener('WebComponentsReady', ()=>{
  ReactDOM.render(<RecentProjects />, document.getElementById("homepage"));
});
