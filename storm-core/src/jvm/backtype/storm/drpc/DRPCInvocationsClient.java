package backtype.storm.drpc;

import backtype.storm.generated.DRPCRequest;
import backtype.storm.generated.DistributedRPCInvocations;
import org.apache.thrift7.TException;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;
import org.apache.thrift7.transport.TTransport;

public class DRPCInvocationsClient implements DRPCInvocations {
    protected TTransport conn;
    protected DistributedRPCInvocations.Client client;
    protected String host;
    protected int port;

    public DRPCInvocationsClient(String host, int port) {
        this(host, port, true);
    }

    public DRPCInvocationsClient(String host, int port, boolean connectImmediately) {
        try {
            this.host = host;
            this.port = port;
            if (connectImmediately)
                connect();
        } catch(TException e) {
            throw new RuntimeException(e);
        }
    }

    protected void ensureConnected() throws TException {
        if (!isConnected())
            connect();
    }

    protected boolean isConnected() {
        return conn != null && conn.isOpen();
    }

    protected void connect() throws TException {
        conn = new TFramedTransport(new TSocket(host, port));
        client = new DistributedRPCInvocations.Client(new TBinaryProtocol(conn));
        conn.open();
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }       

    public void result(String id, String result) throws TException {
        try {
            doResult(id, result);
        } catch(TException e) {
            close();
            throw e;
        }
    }

    protected void doResult(String id, String result) throws TException {
        ensureConnected();
        client.result(id, result);
    }

    public DRPCRequest fetchRequest(String func) throws TException {
        try {
            return doFetchRequest(func);
        } catch(TException e) {
            close();
            throw e;
        }
    }

    protected DRPCRequest doFetchRequest(String func) throws TException {
        ensureConnected();
        return client.fetchRequest(func);
    }

    public void failRequest(String id) throws TException {
        try {
            doFailRequest(id);
        } catch(TException e) {
            close();
            throw e;
        }
    }

    protected void doFailRequest(String id) throws TException {
        ensureConnected();
        client.failRequest(id);
    }

    public void close() {
        if (conn != null) {
            conn.close();
            conn = null;
        }
        client = null;
    }
}
