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
import CustomAttributes from './../mixins/CustomAttributes.jsx'
require("./recent-projects.css");

var RecentProject = React.createClass({
  mixins: [CustomAttributes],
  render(){
    return (
      <paper-item className={"recent-project " + this.props.lastBuildResult}> 
        <paper-item-body ref="ca-1" attrs={{"three-line": ""}}>
          <a href={this.props.url} className="project-name">
            {this._projectName()}- #{this.props.number}
          </a>
          <div ref="ca-2" attrs={{secondary: ""}}>
            <span className="icon-text octicon octicon-git-commit"/>{this.props.commit.message}
          </div>
          <div className="finished">
            <i className="icon-text fa fa-calendar"></i> 
            <span className="detail">{this.props.startTime}</span>
          </div>
        </paper-item-body>
      </paper-item>
    );
  },
  _projectName(){
    return this.props.small? this.props.projectName.split('/')[1]: this.props.projectName;
  }
});

var RecentProjectsWidget =React.createClass({
  mixins: [CustomAttributes, AutoRefreshHelper,Responsive(
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
        <paper-toolbar ref="ca-1" id="drawerToolbar" attrs={{ role:"toolbar"}} className="x-scope">
          <div id="topBar" className="center horizontal layout toolbar-tools style-scope paper-toolbar">
            <span className="paper-font-title">
              <i className="icon-text fa fa-user"></i> Recent Builds
            </span>
          </div>
        </paper-toolbar>
        <paper-menu id="project-list" className="list">
          {recentProjects}
        </paper-menu>
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
