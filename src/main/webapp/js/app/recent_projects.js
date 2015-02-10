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
define([
    'jquery','reactjs'
], function($,React){
    var BuildIcon =React.createClass({displayName: "BuildIcon",
        render: function() {
            var buildIcon ;
            switch(this.props.state){
                case 'SUCCESS': buildIcon = "fa-check"; break;
                case 'FAILURE': buildIcon = "fa-times"; break;
                case 'BUILDING': buildIcon = "fa-circle-o-notch fa-spin"; break;
                case 'ABORTED': buildIcon = "fa-ban"; break;
            }
            return (
                React.createElement("span", {className: "fa fa-fw " + buildIcon})
            );
        }
    });

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
                    React.createElement("a", {href: this.props.url}, " ", this.props.name, "  "), 
                    React.createElement(BuildIcon, {state: "ABORTED"})
                )
            );
        }
    });

    var RecentProjectsWidget = React.createClass({displayName: "RecentProjectsWidget",
        getInitialState: function() {
            return {recentProjects: []};
        },
        componentDidMount: function() {
            $.getJSON(  this.props.url, function( data ) {
                this.setState({recentProjects: data});
            }.bind(this));
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
    });
    return React;
});
