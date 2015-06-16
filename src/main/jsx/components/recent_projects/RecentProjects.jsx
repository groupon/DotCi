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
import Avatar from '../lib/Avatar.jsx';
import AutoRefreshHelper from './../mixins/AutoRefreshHelper.jsx'
import Responsive from './../mixins/Responsive.jsx';
require("./recent-projects.css");

var RecentProject = React.createClass({
  render(){
    return (
      <div className={"recent-project " + this.props.lastBuildResult}> 
        <div className="project-name">
          {this._projectName()}
          <a href={this.props.url}>#{this.props.number}</a>
        </div>
        <div><span className="icon-text octicon octicon-git-commit"/>{this.props.commit.message}</div>
        <div className="finished">
          <i className="icon-text fa fa-calendar"></i> 
          <span className="detail">{this.props.startTime}</span>
        </div>
      </div>
    );
  },
  _projectName(){
    return this.props.small? this.props.projectName.split('/')[1]: this.props.projectName;
  }
});

var RecentProjectsWidget =React.createClass({
  mixins: [AutoRefreshHelper,Responsive(
    {
      "only screen and (max-width: 1450px)": "renderSmall",
      "all and (min-width: 1450px)": "renderDefault",
    }
  )],
  componentWillMount(){
    this._loadRecentProjects()
    this.setRefreshTimer(this._loadRecentProjects);
  },
  _loadRecentProjects(){
    this.props.flux.getRecentProjectsFromServer();
  },
  renderSmall(){
    return this._render(true);
  },
  renderDefault(){
    return this._render(false);
  },
  _render(small){
    var recentProjects = this.props.recentProjects.map(function (project) {
      return (
        <RecentProject small={small} key={project.url} {...project}/>
      );
    });
    return (
      <div id="recent-projects">
        <div className="header align-center">
          <i className="icon-text fa fa-user"></i> Recent Builds
        </div>
        <div id='project-list' >
          {recentProjects}
        </div>
      </div>
    );
  }
});
export default React.createClass({
  render(){
    return (
      <div className={this.props.className}>
        <FluxComponent connectToStores={['recentProjects']} flux={this.props.flux}>
          <RecentProjectsWidget/>
        </FluxComponent>
      </div>
    );
  }
});
