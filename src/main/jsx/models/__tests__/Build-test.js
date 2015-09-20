import Build from './../Build.js'
import  {assert} from 'chai';
describe('Build', ()=> {
  it('should  group log lines',()=>{
    const build =new Build();
    build.log = ["group0", "group0-line1", "$group1", "group1-line2" , "$group2", "group2-line2" , "group2-line3"];
    assert.equal(3,build.log.length);
    assert.equal(2,build.log[0].length);
    assert.equal(2,build.log[1].length);
    assert.equal(3,build.log[2].length);
    assert.equal("group2-line3",build.log[2][2]);
  })
});
