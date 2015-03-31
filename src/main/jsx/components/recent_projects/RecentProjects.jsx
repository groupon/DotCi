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
require("./recent_projects.less");

var RecentProject = React.createClass({
  render(){
    return (
      <a className={"recent-project-"+this.props.lastBuildResult} href={this.props.url}> 
        <div className={"project-name-"+this.props.lastBuildResult}>{this.props.projectName}</div>
        <div className="commit-message"><span className="icon octicon octicon-git-commit"/>{this.props.commit.message}</div>
        <div className="finished">
          <i className="icon fa fa-calendar"></i> 
          <span className="detail">Finished: {this.props.startTime}</span>
        </div>
      </a>
    );
  }
});

var RecentProjectsWidget =React.createClass({
  componentWillMount(){
    this.props.flux.getRecentProjectsFromServer();
  },
  render(){
    var recentProjects = this.props.recentProjects.map(function (project) {
      return (
        <RecentProject key={project.url} {...project}/>
      );
    });
    return (
      <div>
        <h4 className="ui horizontal header divider">
          <i className="fa fa-user"></i>
          Recent Projects
        </h4>
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
