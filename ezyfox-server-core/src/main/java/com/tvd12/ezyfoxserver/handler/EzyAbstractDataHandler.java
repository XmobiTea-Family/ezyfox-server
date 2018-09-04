package com.tvd12.ezyfoxserver.handler;

import static com.tvd12.ezyfoxserver.constant.EzyDisconnectReason.MAX_REQUEST_SIZE;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tvd12.ezyfox.constant.EzyConstant;
import com.tvd12.ezyfox.exception.EzyMaxRequestSizeException;
import com.tvd12.ezyfox.util.EzyDestroyable;
import com.tvd12.ezyfox.util.EzyExceptionHandler;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.EzyServer;
import com.tvd12.ezyfoxserver.command.EzySendResponse;
import com.tvd12.ezyfoxserver.constant.EzyIError;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.context.EzyServerContext;
import com.tvd12.ezyfoxserver.context.EzyZoneContext;
import com.tvd12.ezyfoxserver.delegate.EzySessionDelegate;
import com.tvd12.ezyfoxserver.entity.EzyAbstractSession;
import com.tvd12.ezyfoxserver.entity.EzySession;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.ezyfoxserver.response.EzyErrorParams;
import com.tvd12.ezyfoxserver.response.EzyErrorResponse;
import com.tvd12.ezyfoxserver.response.EzyResponse;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSetting;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSetting.EzyMaxRequestPerSecond;
import com.tvd12.ezyfoxserver.setting.EzySettings;
import com.tvd12.ezyfoxserver.socket.EzyChannel;
import com.tvd12.ezyfoxserver.statistics.EzyRequestFrame;
import com.tvd12.ezyfoxserver.statistics.EzyRequestFrameSecond;
import com.tvd12.ezyfoxserver.wrapper.EzyServerControllers;
import com.tvd12.ezyfoxserver.wrapper.EzySessionManager;
import com.tvd12.ezyfoxserver.wrapper.EzyZoneUserManager;

@SuppressWarnings("rawtypes")
public abstract class EzyAbstractDataHandler<S extends EzySession> 
        extends EzyLoggable
        implements EzySessionDelegate, EzyDestroyable {

    protected S session;
    protected EzyChannel channel;
    protected EzyUser user;
    protected EzyServer server;
    protected EzyServerContext context;
    protected EzyZoneContext zoneContext;
    protected EzyServerControllers controllers;
    protected EzyZoneUserManager userManager;
    protected EzySessionManager sessionManager;
    protected Lock lock = new ReentrantLock();
    
    protected EzySettings settings;
    protected Set<EzyConstant> unloggableCommands;
    protected EzySessionManagementSetting sessionManagementSetting;
    
    //===== for measure max request per second =====
    protected EzyRequestFrame requestFrameInSecond;
    protected EzyMaxRequestPerSecond maxRequestPerSecond;
    //=====  =====
    
    protected volatile boolean active = true;
    protected Map<Class<?>, EzyExceptionHandler> exceptionHandlers = newExceptionHandlers();
    
    public EzyAbstractDataHandler(EzyServerContext ctx, S session) {
        this.context = ctx;
        this.session = session;
        this.channel = session.getChannel();
        this.server = context.getServer();
        this.controllers = server.getControllers();
        this.sessionManager = server.getSessionManager();
        
        this.settings = server.getSettings();
        this.sessionManagementSetting = settings.getSessionManagement();
        this.unloggableCommands = settings.getLogger().getIgnoredCommands().getCommands();
        this.maxRequestPerSecond = sessionManagementSetting.getSessionMaxRequestPerSecond();
        this.requestFrameInSecond = new EzyRequestFrameSecond(maxRequestPerSecond.getValue());
        
        ((EzyAbstractSession)this.session).setDelegate(this);
    }
    
    protected EzyAppContext getAppContext(int appId) {
        return context.getAppContext(appId);
    }
    
    protected void setActive(boolean value) {
        this.active = value;
    }
    
    protected EzyZoneUserManager getUserManager(int zoneId) {
        EzyZoneContext zoneContext = context.getZoneContext(zoneId);
        return zoneContext.getZone().getUserManager();
    }
    
    protected void response(EzyResponse response) {
        if(context != null)
            newSendResponse().recipient(session).response(response).execute();
    }
    
    protected EzySendResponse newSendResponse() {
        return context.cmd(EzySendResponse.class);
    }
    
    protected void responseError(EzyIError error) {
        EzyErrorParams params = new EzyErrorParams();
        params.setError(error);
        response(new EzyErrorResponse(params));
    }
    
    @SuppressWarnings("unchecked")
    private Map<Class<?>, EzyExceptionHandler> newExceptionHandlers() {
        Map<Class<?>, EzyExceptionHandler> handlers = new ConcurrentHashMap<>();
        handlers.put(EzyMaxRequestSizeException.class, (thread, throwable) -> {
            if(sessionManager != null) 
                sessionManager.removeSession(session, MAX_REQUEST_SIZE);
        });
        addExceptionHandlers(handlers);
        return handlers;
    }
    
    protected void addExceptionHandlers(Map<Class<?>, EzyExceptionHandler> handlers) {
    }
    
    @Override
    public void destroy() {
        if(session != null)
            session.destroy();
        this.session = null;
        this.channel = null;
        this.server = null;
        this.user = null;
        this.context = null;
        this.zoneContext = null;
        this.controllers = null;
        this.userManager = null;
        this.sessionManager = null;
        this.lock = null;
        this.settings = null;
        this.unloggableCommands = null;
        this.sessionManagementSetting = null;
        this.requestFrameInSecond = null;
        this.maxRequestPerSecond = null;
        this.active = false;
        if(exceptionHandlers != null)
            this.exceptionHandlers.clear();
        this.exceptionHandlers = null;
    }
    
}
