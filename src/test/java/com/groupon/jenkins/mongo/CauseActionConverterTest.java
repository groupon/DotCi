package com.groupon.jenkins.mongo;

import com.groupon.jenkins.dynamic.build.cause.NullBuildCause;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class CauseActionConverterTest {

    //decode
    @Test
    public void should_return_null_if_dbObject_is_null() throws Exception {
        Assert.assertNull(new CauseActionConverter().decode(null, null, null));
    }

    @Test
    public void should_get_cause_from_dbObject() throws Exception {
        BasicDBObject cause1DbObject = new BasicDBObject("cause1", "cause1");
        DBObject causes = new BasicDBObjectBuilder().add("causes", Arrays.asList(cause1DbObject)).get();

        Mapper mapper = Mockito.mock(Mapper.class);
        Cause cause1 = new NullBuildCause();
        when(mapper.fromDBObject(null, cause1DbObject, null)).thenReturn(cause1);

        CauseActionConverter converter = new CauseActionConverter();
        converter.setMapper(mapper);
        CauseAction action = converter.decode(CauseAction.class, causes, Mockito.mock(MappedField.class));

        Assert.assertEquals(1, action.getCauses().size());
        Assert.assertEquals(cause1, action.getCauses().get(0));

    }
    //end decode

    //encode
    @Test
    public void should_return_null_if_object_is_null() throws Exception {
        Assert.assertNull(new CauseActionConverter().encode(null, null));
    }

    @Test
    public void should_convert_cause_action_to_old_format() throws Exception {
        Cause cause1 = new NullBuildCause();
        Mapper mapper = Mockito.mock(Mapper.class);
        when(mapper.toDBObject(cause1)).thenReturn(new BasicDBObject("cause1", "cause1"));

        CauseAction causeAction = new CauseAction(cause1);
        CauseActionConverter converter = new CauseActionConverter();
        converter.setMapper(mapper);

        DBObject dbObject = (DBObject) converter.encode(causeAction, null);

        Assert.assertEquals(dbObject.get("className"), CauseAction.class.getName());
        Assert.assertNotNull(dbObject.get("causes"));
        List dbCauses = ((List) dbObject.get("causes"));
        Assert.assertEquals(1, dbCauses.size());
        Assert.assertEquals("cause1", ((BasicDBObject) dbCauses.get(0)).get("cause1"));
    }
    //end encode
}
