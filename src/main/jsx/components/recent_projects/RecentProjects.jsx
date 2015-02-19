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
import BuildIcon from "../BuildIcon"
import FluxComponent from 'flummox/component';

class RecentProjectsHeader extends React.Component{
    render(){
        return (
            <div className="panel-heading">
                <h3 className="panel-title">Recent Projects</h3>
            </div>
        );
    }
};


class RecentProject extends React.Component{
    render(){
        return (
            <li key={this.props.url} className="list-group-item">
                <a href={this.props.url}> {this.props.name}  </a>
                <BuildIcon state="ABORTED"/>
            </li>
        );
    }
};

class RecentProjectsWidget extends React.Component{
    constructor(props) {
        super(props);
    }
    render(){
        var recentProjects = this.props.recentProjects.map(function (project) {
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
};
export default class RecentProjectsView extends React.Component{
    render(){
        return (
            <FluxComponent connectToStores={['recentProjects']} flux={this.props.flux}>
                <RecentProjectsWidget/>
            </FluxComponent>
        )
    }
}
