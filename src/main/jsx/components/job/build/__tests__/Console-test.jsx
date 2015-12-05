import Console from '../Console.jsx';
import React from 'react/addons';
import  {assert} from 'chai';
var TestUtils = React.addons.TestUtils;
var findById = function(component,id){
  const elements =TestUtils.findAllInRenderedTree(component,(inst) => inst.getDOMNode().id == id);
  return elements[0];
}

describe('Console', ()=> {
  it('should  fold if clicked on the arrow',()=>{
    // const console=TestUtils.renderIntoDocument(<Console log={Immutable.fromJS(["$1","1.1","1.2", "$2","2.1","2.2"])}/>);

    // const line4Node =findById(console,'L4');
    // const line4 =  line4Node.getDOMNode();
    // const parentDiv = line4.parentNode;
    // assert.isTrue(parentDiv.classList.contains('open'));

    // TestUtils.Simulate.click(line4Node);

    // assert.isTrue(parentDiv.classList.contains('closed'));
  })
});

