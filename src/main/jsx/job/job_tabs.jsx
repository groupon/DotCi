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
    'reactjs'
], function(React){
    var Tab = React.createClass({
        render: function(){
            return (
            //<li role="presentation"  class="{tab.active?'active':''}">
            //    <a href="buildHistory/{this.props.url}" class="build-history-tab-button " data-target="#${tab.url}" data-toggle="tab" >
            //        <span class="${tab.fontIcon} ${tab.state}" ></span>
            //        ${tab.name}
            //        <j:if test="${tab.removable}">
            //            <button class="close closeTab removeTab" type="button"  data-name='${tab.name}' >x</button>
            //        </j:if>
            //    </a>
            //</li>
                <h1>asdfas</h1>
            );
        }
    });

    return JobTabs = React.createClass({
        getInitialState: function() {
            return {tabs: {}};
        },
        componentDidMount: function() {
            $.getJSON(  this.props.url, function( data ) {
                this.setState({tabs: data});
            }.bind(this));
        },
        render: function(){
            var tabs = _.map(this.state.tabs, function(tab,url){
                return ( <Tab url={url} name ={tab}/> );
            });
            return (
                <div role="tabpanel">
                    <ul class="nav nav-tabs">
                    {tabs}
                    </ul>
                </div>
            );

        }
    });
});