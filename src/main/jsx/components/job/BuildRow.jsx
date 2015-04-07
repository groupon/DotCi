
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
class CommitInfo {
  render(){
    let {message,commitUrl,shortSha,committerName,branch} = this.props.commit.toObject();
    return(<span className="commit-info">
      <span className="commit-info-column">
        <h5>{message}</h5>
        <object>
          <a className="commit-link"  href={commitUrl} >
            <span className="icon octicon octicon-git-compare"></span> {shortSha} <i className="fa fa-external-link-square"></i>
          </a>
        </object>
      </span>
      <span className="commit-info-column">
        {committerName}
        <span>{branch}</span>
      </span>
    </span>);
  }
}
var BuildRow = React.createClass({
  render(){
    let {result,number, cancelUrl,commit} = this.props.build.toObject();
    return (
      <Router.Link className ={"build-row-"+result}  to={'job-widgets'} params={{widget: number}}>
        <Avatar emailDigest={commit.get('emailDigest')} />
        <CommitInfo commit={commit} />
        {this._buildDuration()}
      </Router.Link>
    );
  },
  _buildDuration(){
    let {duration,displayTime} = this.props.build.toObject();
    return( <span className="build-duration">
      <div className="ui label">
        <i className="icon fa fa-clock-o"></i> Duration:
        <span className="detail">{duration}</span>
      </div>
      <div className="ui label">
        <i className="icon fa fa-clock-o"></i> Started
        <span className="detail">{displayTime}</span>
      </div>
    </span>
          );
  }
});
export default BuildRow;
