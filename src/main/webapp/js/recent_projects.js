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

    var RecentProjectsHeader = React.createClass({displayName: "RecentProjectsHeader",
        render: function() {
            return (
                React.createElement("div", {className: "panel-heading"}, 
                    React.createElement("h3", {className: "panel-title"}, "Recent Projects")
                )
            );
        }
    });


    var RecentProject = React.createClass({displayName: "RecentProject",
        render: function(){
            return (
                React.createElement("li", {className: "list-group-item"}, 
                    React.createElement("a", {href: this.props.url}, " ", this.props.name, "  ")
                )
            );
        }
    });

    var RecentProjectsWidget = React.createClass({displayName: "RecentProjectsWidget",

        getInitialState: function() {
            return {recentProjects: []};
        },
        componentDidMount: function() {
          //make ajax call here
        },
        render: function(){
            var recentProjects = this.state.recentProjects.map(function (project) {
                return (
                    React.createElement(RecentProject, {url: project.url, name: project.name})
                );
            });
            return (
                React.createElement("div", {className: "panel panel-info"}, 
                    React.createElement(RecentProjectsHeader, null), 
                    React.createElement("ul", {className: "list-group"}, 
                    recentProjects
                    )
                )
            );
        }
    })


    React.render(
        React.createElement(RecentProjectsWidget, {url: "/recentProjects"}),
        document.getElementById('recent-projects')
    );




});
