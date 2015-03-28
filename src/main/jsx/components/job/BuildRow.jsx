
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
import Avatar from '../lib/Avatar.jsx';
import Router from 'react-router';
require('./build_row.less');
var BuildRow = React.createClass({
    render(){
        return (
          <Router.Link className ={"build-row-"+this.props.result}  to={'build'} params={{buildNumber: this.props.number}}>
               <Avatar emailDigest={this.props.commit.emailDigest} />
               {this._commitInfo()}
                {this._buildDuration()}
            </Router.Link>
        );
    },
    _commitInfo(){
      return(<span className="commit-info">
        <span className="commit-info-column">
        <h5>{this.props.commit.message}</h5>
        <object>
        <a className="commit-link"  href={this.props.commit.commitUrl} >
          <span className="icon octicon octicon-git-compare"></span> {this.props.commit.shortSha} <i className="fa fa-external-link-square"></i>
        </a>
      </object>
      </span>
      <span className="commit-info-column">
        {this.props.commit.committerName}
        <span>{this.props.commit.branch}</span>
      </span>
      </span>);
    },
    _buildDuration(){
      return( <span className="build-duration">
        <div className="ui label">
          <i className="icon fa fa-clock-o"></i> Duration:
          <span className="detail">{this.props.duration}</span>
        </div>
        <div className="ui label">
          <i className="icon fa fa-clock-o"></i> Started
          <span className="detail">{this.props.displayTime}</span>
        </div>
      </span>
            );
    }
});
export default BuildRow;
