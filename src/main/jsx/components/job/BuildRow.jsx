
import React from 'react';
import Avatar from '../lib/Avatar.jsx';
require('./build_row.less');
var BuildRow = React.createClass({
    render(){
        return (
          <a className ={"build-row-"+this.props.result}  href={this.props.number+'/console'} >
               <Avatar emailDigest={this.props.commit.emailDigest} />
               {this._commitInfo()}
                {this._buildDuration()}
            </a>
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
