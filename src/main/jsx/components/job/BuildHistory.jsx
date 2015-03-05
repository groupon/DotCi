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
import BuildRow from './BuildRow.jsx';

require('./build_history.less');


var BuildHistoryTable = React.createClass({
    render(){
        let builds = this.props.builds.map((build) => <BuildRow key={build.number} {...build}/>);
        return(
          <div className="flex-column">
                 {builds}
               </div>
        );
    }
});
var BuildHistoryTabs = React.createClass({
    getInitialState(){
        return {currentSelection: 0};
    },
    render()  {
      return (<div className="branch-tabs">
        {this.props.tabs.map((tab,i)=>this._getHistoryTab(tab,i))}
        <a className="branch-tab" href="#" onClick={this._addTab}> <i className="fa fa-plus-circle"></i></a>
              </div>
        );
    },
    _addTab(){
    },
    _notifyTabSelection: function (tabIndex) {
        let actions = this.props.flux.getActions('app');
        actions.buildHistorySelected(this.props.tabs[tabIndex]);
    },
    _onActiveTabChange(event){
        var tab =parseInt(event.currentTarget.getAttribute('data-tab'));
        this.setState({currentSelection: tab});
        this._notifyTabSelection(tab);
    },
    _getHistoryTab(tab,i) {
      var cx = React.addons.classSet;
      var classes = cx({
        'branch-tab': true,
        'branch-tab-active': this.state.currentSelection==i
      });
      return   <a className={classes} key={i} data-tab={i}  onClick={this._onActiveTabChange} href="#" >{tab}</a>;
    }
});
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
