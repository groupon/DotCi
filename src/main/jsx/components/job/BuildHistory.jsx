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

import React from 'react';
import Nav from 'react-bootstrap/lib/Nav';
import NavItem from 'react-bootstrap/lib/NavItem';
import ListGroup from 'react-bootstrap/lib/ListGroup.js';
import ListGroupItem from 'react-bootstrap/lib/ListGroupItem.js';
import BuildIcon from '../lib/BuildIcon.jsx';
require('./build_history.less');



var BuildRow = React.createClass({
    render(){
        return (
            <ListGroupItem className ={"build-row-"+this.props.result} header={this._header()} href={this.props.number+''} >
                <img  alt={this.props.commit.emailDigest} src={"https://secure.gravatar.com/avatar/"+this.props.commit.emailDigest+".png?r=PG&s=46"}/>
                <span >{this.props.commit.committerName}</span> 
                <button type="button" className="btn btn-link with-space" >
                    <span className="octicon octicon-git-compare"/> {this.props.commit.shortSha} <i className="fa fa-external-link-square"></i>
                </button>
                <span className="pull-right">
                    <span ><small><i className="fa fa-clock-o"></i> Duration:</small> {this.props.duration} </span>
                    <br/>
                    <span ><small><i className="fa fa-clock-o"></i> Started:</small> {this.props.displayTime} </span>
                </span>
            </ListGroupItem>
        );
    },
    _header(){
        return (
            <span>
                <BuildIcon state={this.props.result}/>
                <span className="octicon octicon-git-branch with-space"></span><small>{this.props.commit.branch}</small>: {this.props.commit.message}
            </span>
        );
    }
});

var BuildHistoryTable = React.createClass({
    render(){
        let builds = this.props.builds.map((build) => <BuildRow key={build.number} {...build}/>);
        return      <ListGroup>
                 {builds}
        </ListGroup>;
    }
});
var BuildHistoryTabs = React.createClass({
    getInitialState(){
        return {currentSelection: 0}
    },
    render()  {
        return (
            <Nav bsStyle="pills" activeKey={this.state.currentSelection} onSelect={this._onActiveTabChange}>
               {this.props.tabs.map((tab,i) => this._getHistoryTab(tab,i))}
            </Nav>
        )
    },
    _notifyTabSelection: function (tabIndex) {
        let actions = this.props.flux.getActions('app');
        actions.buildHistorySelected(this.props.tabs[tabIndex]);
    },
    _onActiveTabChange(tab){
        this.setState({currentSelection: tab});
        this._notifyTabSelection(tab);
    },
    _getHistoryTab(tab,i) {
        return <NavItem key={i} eventKey={i} > {tab}</NavItem>;
    }
})
export default React.createClass({
    render(){
        return(
            <div>
                <BuildHistoryTabs flux={this.props.flux} tabs={this.props.tabs}/>
                <BuildHistoryTable builds ={this.props.builds}/>
            </div>
        );
    }
});