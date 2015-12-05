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
import BuildIcon  from './../job/BuildIcon.jsx';
import BuildRow  from './../job/BuildRow.jsx';
var RecentProject = (props) => {
  const builds = props.builds.map(build => {
    return  <BuildRow tiny={props.tiny} fullUrl key={build.number} build={build}/>
  });

  return <div >
    <b>{props.project}</b>
    <div>
      {builds}
    </div>
  </div>
};

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
  render(){
    if(!this.state.recentProjects) return <Loading/>;
    const recentProjects = this.state.recentProjects.map(project => {
      return <RecentProject tiny={this.props.tiny} key={project.name} project={project.name} builds={project.builds}/>
    });
    return (
      <div className="layout vertical wrap">
        {recentProjects}
      </div>
    );
  }
});
