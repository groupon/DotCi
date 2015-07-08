package com.groupon.jenkins.dynamic.build;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("build_log")
public class BuildLog {
    @Id
    private ObjectId id = new ObjectId();
    public BuildLog(ObjectId buildId) {
        this.buildId = buildId;
    }

    public ObjectId getBuildId() {
        return buildId;
    }

    public byte[] getLog() {
        return log;
    }

    private ObjectId buildId;
    private  byte[] log;

    public void append(byte[] b, int off, int len) {
      this.log = concatenateByteArrays(this.log, b,off,len);
    }
    byte[] concatenateByteArrays(byte[] a, byte[] b, int off, int len) {
        if(a == null){
            byte[] bresult = new byte[len];
            System.arraycopy(b, 0, bresult, off, len);
            return bresult;
        }
        byte[] result = new byte[a.length + len];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length+off, len);
        return result;
    }
}
