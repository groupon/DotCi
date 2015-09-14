
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
import BuildIcon from './BuildIcon.jsx';
import BuildCauseIcon from './BuildCauseIcon.jsx';
import BuildProgressBar from './BuildProgressBar.jsx';
import Responsive from './../mixins/Responsive.jsx';
require('./build_row.css');
require('./build_row_small.css');
export default React.createClass({
  mixins: [Responsive(
    {
      "only screen and (max-width: 680px)": "renderSmall",
      "all and (min-width: 680px)": "renderDefault",
    }
  )],
  renderSmall(){
    let {result,number,displayTime,commit} = this.props.build.toObject();
    let {message,committerName,branch, avatarUrl} = commit.toObject();
    return (   <paper-material >
      <Router.Link   to={'job-widgets'} params={{widget: number}}>
        <paper-item className={"build-row-small "+result}> 
          <BuildProgressBar build={this.props.build}/>
          <paper-item-body three-line>
            <div>{number}-{message}</div>
            <div secondary>
              <Avatar avatarUrl={avatarUrl} />
              <span>{committerName}({branch})</span>
            </div>
            <div  secondary>
              {displayTime} 
            </div>
          </paper-item-body>
        </paper-item>
      </Router.Link>
    </paper-material>);
  },
  renderDefault(){
    let {result,number, cancelUrl,commit,durationString,displayTime,cause, estimatedDuration,duration} = this.props.build.toObject();
    let {message,commitUrl,shortSha,committerName,branch, avatarUrl} = commit.toObject();
    return (
      <div className="build-row">
        <BuildProgressBar build={this.props.build}/>
        <div className ={"build-info build-info-"+result}>
          {this._statusRow(result,cause)}
          <span>
            <div className="build-row--title"><small>{branch}</small> 
              <Router.Link  to={'job-widgets'} params={{widget: number}}>{message}</Router.Link>
            </div> 
            <div className="build-row--committer">
              <Avatar avatarUrl={avatarUrl} />
              <span>{committerName}</span>
            </div>
            <div className="build-row--cause">{cause.get('shortDescription')}</div>
          </span>
          <span>  
            <div>#<Router.Link  className="build-row--number" to={'job-widgets'} params={{widget: number}}>{number}{result.toLowerCase()}</Router.Link></div>
            <div><iron-icon icon="github:octoface"/><a className="github-link link-no-decoration" href={commitUrl}> {shortSha}</a></div>
          </span>
          <span>
            <div>
              <iron-icon icon="alarm" />
              <span className="detail">{durationString}</span>
            </div>
            <div>
              <iron-icon icon="dotci:calendar" />
              <span className="detail">{displayTime}</span>
            </div>
          </span>
        </div>
      </div>
    );
  },
  _statusRow(result, cause){
    return this.props.compact? <span/>:<span>
      <BuildIcon result={result} />
      <BuildCauseIcon cause={cause.get('name')} />
    </span>
  },
});
