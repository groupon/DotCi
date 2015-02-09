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
$(function() {

    var RecentProjectsHeader = React.createClass({
        render: function() {
            return (
                <div className="panel-heading">
                    <h3 className="panel-title">Recent Projects</h3>
                </div>
            );
        }
    });


    var RecentProject = React.createClass({
        render: function(){
            return (
                <li className="list-group-item">
                    <a href={this.props.url}> {this.props.name}  </a>
                </li>
            );
        }
    });

    var RecentProjectsWidget = React.createClass({

        getInitialState: function() {
            return {recentProjects: []};
        },
        componentDidMount: function() {
          //make ajax call here
        },
        render: function(){
            var recentProjects = this.state.recentProjects.map(function (project) {
                return (
                    <RecentProject url={project.url} name ={project.name}/>
                );
            });
            return (
                <div className="panel panel-info">
                    <RecentProjectsHeader/>
                    <ul className="list-group">
                    {recentProjects}
                    </ul>
                </div>
            );
        }
    })


    React.render(
        <RecentProjectsWidget url="/recentProjects"/>,
        document.getElementById('recent-projects')
    );




});
