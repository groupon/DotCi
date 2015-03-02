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
import BuildIcon from "../lib/BuildIcon.jsx";
import FluxComponent from 'flummox/component';
import Panel from "react-bootstrap/lib/Panel";
import ListGroup from "react-bootstrap/lib/ListGroup";
import ListGroupItem from "react-bootstrap/lib/ListGroupItem";

var RecentProject = React.createClass({
    render(){
        return (
            <ListGroupItem key={this.props.url}>
                <a href={this.props.url}> {this.props.name} </a>
                <BuildIcon state={this.props.lastBuildStatus}/>
            </ListGroupItem>
        );
    }
});

var RecentProjectsWidget =React.createClass({
    render(){
        var recentProjects = this.props.recentProjects.map(function (project) {
            return (
                <RecentProject key={project.url} url={project.url} name ={project.name} lastBuildStatus={project.lastBuildStatus}/>
            );
        });
        return (
        <Panel collapsable defaultExpanded header="Recent Projects">
            <ListGroup fill>
            {recentProjects}
            </ListGroup>
        </Panel>


        );
    }
});
export default React.createClass({
    render(){
        return (
            <FluxComponent connectToStores={['recentProjects']} flux={this.props.flux}>
                <RecentProjectsWidget/>
            </FluxComponent>
        );
    }
});
