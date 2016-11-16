package com.groupon.jenkins;

import com.mongodb.ServerAddress;
import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;

public class SetupConfigTest {
    @Test
    public void should_get_single_mongo_serveraddress_from_host_port() throws UnknownHostException {
        final SetupConfig setupConfig = new SetupConfig(null);
        setupConfig.setDbHost("localhost");
        setupConfig.setDbPort(27013);
        final List<ServerAddress> mongoServerAddresses = setupConfig.getMongoServerAddresses();
        Assert.assertEquals(1, mongoServerAddresses.size());
        Assert.assertEquals("localhost", mongoServerAddresses.get(0).getHost());
    }

    @Test
    public void should_get_multiple_mongo_serveraddress_from_multiple_host_port() throws UnknownHostException {
        final SetupConfig setupConfig = new SetupConfig(null);
        setupConfig.setDbHost("localhost,localhost");
        setupConfig.setDbPort(27013);
        final List<ServerAddress> mongoServerAddresses = setupConfig.getMongoServerAddresses();
        Assert.assertEquals(2, mongoServerAddresses.size());
        Assert.assertEquals("localhost", mongoServerAddresses.get(0).getHost());
        Assert.assertEquals("localhost", mongoServerAddresses.get(1).getHost());
    }

    @Test
    public void should_use_port_if_specified_use_defualt_otherwise() throws UnknownHostException {
        final SetupConfig setupConfig = new SetupConfig(null);
        setupConfig.setDbHost("localhost,localhost:1682");
        setupConfig.setDbPort(27013);
        final List<ServerAddress> mongoServerAddresses = setupConfig.getMongoServerAddresses();
        Assert.assertEquals(2, mongoServerAddresses.size());
        Assert.assertEquals(27013, mongoServerAddresses.get(0).getPort());
        Assert.assertEquals(1682, mongoServerAddresses.get(1).getPort());
    }
}
