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
import Avatar from '../lib/Avatar.jsx';
import AutoRefreshHelper from './../mixins/AutoRefreshHelper.jsx'
import Loading from './../mixins/Loading.jsx';
import {recentProjects} from './../../api/Api.jsx';
require("./recent-projects.css");

var RecentProject = React.createClass({
  render(){
    return (
      <paper-item className={"recent-project " + this.props.lastBuildResult}> 
        <paper-item-body three-line>
          <a href={this.props.url} className="project-name">
            <span className="project-title">{this._projectName()}</span>-{this.props.number}
          </a>
          <div secondary>
            <iron-icon icon="github:commit"/> {this.props.commit.message}
          </div>
          <div secondary className="finished">
            <iron-icon icon="alarm"/>
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

export default React.createClass({
  mixins: [AutoRefreshHelper],
  getInitialState(){
    return {};
  },
  componentWillMount(){
    this._loadRecentProjects()
    this.setRefreshTimer(this._loadRecentProjects);
  },
  _loadRecentProjects(){
    var self =this;
    recentProjects().then(data =>{ 
      self.setState({recentProjects: data.recentProjects});
    });
  },
  renderSmall(){
    return this._render(true);
  },
  render(){
    return this._render(false);
  },
  _render(small){
    if(!this.state.recentProjects) return <Loading/>;
    var recentProjects = this.state.recentProjects.map(project => <RecentProject key={project.url} small={true} {...project}/>);
    return (
      <div id="recent-projects">
        <paper-menu id="project-list" className="list">
          {recentProjects}
        </paper-menu>
      </div>
    );
  }
});
