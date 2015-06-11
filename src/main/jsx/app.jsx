/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

__webpack_public_path__= window.resURL+'js/';
import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import Job from "./components/job/Job.jsx";
import Flux from "./Flux.jsx";
import Router from 'react-router';
require('./app.css');
var RouteHandler = Router.RouteHandler;
var Route = Router.Route;
const App = React.createClass( {
  statics:{
    routerWillRun({flux}){
      const actions =flux.getActions('app');
      return actions.getJobInfoFromServer("fullName,githubUrl,permissions");
    }
  },
  render(){
    const flux = this.props.flux;  
    return    <div className="app" >   
      <RecentProjects flux={flux} />   
      {window.emptyProject? <div/>:<RouteHandler   {...this.props}/>}
    </div>;
  }
});

async function performRouteHandlerStaticMethod(routes, methodName, ...args) {
  return Promise.all(routes
                     .map(route => route.handler[methodName])
                     .filter(method => typeof method === 'function')
                     .map(method => method(...args))
                    );
}

window.onload = function(){
  let flux = new Flux();
  const jobPath = jobUrl.replace(rootURL,'');
  const rootPath = window.location.pathname.split(jobPath)[0] +jobPath;
  var routes = (
    <Route  path={rootPath} handler={App} >
      <Route  handler={Job}>
        {Job.Routes }
      </Route>
    </Route>
  );
  const router = Router.create({
    routes: routes,
    location: Router.HistoryLocation
  })
  if(!window.emptyProject){
    router.run(async  (Handler, state) => {
      const routeHandlerInfo = { state, flux };
      await performRouteHandlerStaticMethod(state.routes, 'routerWillRun', routeHandlerInfo);
      React.render(<Handler flux ={flux}/>, document.getElementById('app'));
    });
  }else{
    React.render(<App flux ={flux}/>, document.getElementById('app'));
  }
};
