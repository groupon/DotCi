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
import FluxComponent from 'flummox/component';
import BuildHistory from './BuildHistory.jsx';
//Lazy load BuildMetrics; not used frequently
import BuildMetrics from 'react-proxy!./BuildMetrics.jsx';
import Widgets from '../lib/Widgets.jsx';
import Header from './Header.jsx';
import Router from 'react-router';
import Build from './build/Build.jsx';
var RouteHandler = Router.RouteHandler;
var DefaultRoute = Router.DefaultRoute;
var Route = Router.Route;
var JobWidgets = React.createClass({
  contextTypes: {
    router: React.PropTypes.func
  },
  render(){
    let widgetParam = this.context.router.getCurrentParams().widget;
    let activeWidget = widgetParam? widgetParam:"buildHistory";
    return  <Widgets activeWidget={activeWidget}>
      <BuildHistory icon="fa fa-history" url="buildHistory" name="Build History" tabs={this._get('buildHistoryTabs')} builds={this._get('builds')} flux={this.props.flux}/>
      <BuildMetrics icon="fa fa-bar-chart" url="buildMetrics" name="Build Metrics" buildTimes={this._get('buildTimes')} flux={this.props.flux} />
      <Build icon="fa fa-file" url={this._isNumeric(activeWidget)? activeWidget: ''} name={"Build - " + widgetParam} build={this._get('build')} flux={this.props.flux} tabVisibleWhenActive />
    </Widgets>;
  },
  _isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  },
  _get(key){
    return this.props.job.get(key);
  }
});

export default React.createClass({
  statics:{
    Routes:[ <DefaultRoute key="defaultRoute" handler= {JobWidgets} />,
      <Route name="job-widgets" key="job-widgets" path=":widget" handler={JobWidgets}/>,
      <Router.Redirect key="trailingRedirect" from=":widget/" to="job-widgets" />
    ] ,
    routerWillRun: async function({flux}){
      const actions =flux.getActions('app');
      const selectedTab = Router.HashLocation.getCurrentPath();
      return await actions.getJobInfoFromServer("buildHistoryTabs,fullName,githubUrl,permissions,builds[*,commit[*]]",selectedTab? selectedTab:'master');
    }
  },
  render(){
    return (
      <div className={this.props.className}>
        <FluxComponent connectToStores={['job']} flux={this.props.flux}>
          <Header />
          <RouteHandler/>
        </FluxComponent>
      </div>
    );
  }
});

