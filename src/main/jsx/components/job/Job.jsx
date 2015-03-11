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
import BuildMetrics from './BuildMetrics.jsx';
import Widgets from '../lib/Widgets.jsx';
import Header from './Header.jsx';
import Router from 'react-router';
var RouteHandler = Router.RouteHandler;
var DefaultRoute = Router.DefaultRoute;
var Redirect = Router.Redirect;
var Route = Router.Route;
var JobWidgets = React.createClass({
    mixins: [Router.State],
  render(){
    return  <Widgets activeWidget={this.getParams().widget}>
      <BuildHistory icon="fa fa-history" url="buildHistory" name="Build History" tabs={this.props.buildHistoryTabs} builds={this.props.builds} flux={this.props.flux}/>
      <BuildMetrics icon="fa fa-bar-chart" url="buildMetrics" name="Build Metrics"/>
      </Widgets>;
  }
});

export default React.createClass({
  statics:{
    Routes: (<Route>
      <DefaultRoute handler={JobWidgets} />
      <Route name="job-widgets" path=":widget" handler={JobWidgets}/>
    </Route>)
  },
  render(){
    return (
      <FluxComponent connectToStores={['job']} flux={this.props.flux}>
        <Header/>
        <RouteHandler {...this.props}/>
      </FluxComponent>
    );
  }
});

