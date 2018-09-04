package com.tvd12.ezyfoxserver.command.impl;

import com.tvd12.ezyfox.util.EzyExceptionHandlers;
import com.tvd12.ezyfox.util.EzyExceptionHandlersFetcher;
import com.tvd12.ezyfoxserver.EzyServer;
import com.tvd12.ezyfoxserver.command.EzyHandleException;

public class EzyServerHandleExceptionImpl 
        extends EzyAbstractCommand 
        implements EzyHandleException {

    private final EzyExceptionHandlersFetcher fetcher; 
    
    public EzyServerHandleExceptionImpl(EzyServer server) {
        this.fetcher = (EzyExceptionHandlersFetcher) server;
    }
    
    @Override
    public void handle(Thread thread, Throwable throwable) {
        EzyExceptionHandlers handlers = fetcher.getExceptionHandlers();
        try {
            handlers.handleException(thread, throwable);
        }
        catch(Exception e) {
            getLogger().warn("handle exception error", e);
        }
        finally {
            getLogger().debug("handle exception", throwable);
        }
    }
    
}
