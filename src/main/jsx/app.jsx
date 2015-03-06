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

import React from "react";
import RecentProjects from "./components/recent_projects/RecentProjects.jsx";
import Job from "./components/job/Job.jsx";
import Flux from "./Flux.jsx";
import Router from 'react-router';
require('./app.less');
var RouteHandler = Router.RouteHandler;
var Route = Router.Route;
const App=  React.createClass({
  render(){
    const flux = this.props.flux;
    return    <div className="app" >
      <div className="recent-projects">
        <RecentProjects flux={flux} />
      </div>
      <div className="job">
        <RouteHandler {...this.props}/>
      </div>
    </div>;
  }
});
window.onload = function(){
  let flux = new Flux();
  var routes = (
    <Route  path="/"  handler={App} >
      <Route name="job" path="/" handler={Job}>
        {Job.Routes}
      </Route>
    </Route>
  );
  let actions = flux.getActions('app');
  actions.getRecentProjectsFromServer();
  actions.getJobInfoFromServer();
  Router.run(routes, function (Handler, state) {
    var params = "/"+state.params;
    React.render(<Handler params={params} flux={flux}/>, document.getElementById('app'));
  });
};
