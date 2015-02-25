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
import FluxComponent from 'flummox/component';



var BuildRow = React.createClass({
    render(){
        return (
            <a href={this.props.number} className="list-group-item">
                <h1> {this.props.number} </h1>
            </a>
        );
    }
});

var BuildHistoryTable = React.createClass({
    render(){
        let builds = this.props.builds.map((build) => <BuildRow key={build.number} {...build}/>);
        return   <div className="list-group">
                 {builds}
        </div>;
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