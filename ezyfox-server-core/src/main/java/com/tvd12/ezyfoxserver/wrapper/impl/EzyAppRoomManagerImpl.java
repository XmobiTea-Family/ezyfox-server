package com.tvd12.ezyfoxserver.wrapper.impl;

import com.tvd12.ezyfoxserver.wrapper.*;

public class EzyAppRoomManagerImpl extends EzyAbstractRoomManager implements EzyAppRoomManager {

    protected EzyAppRoomManagerImpl(EzyAppRoomManagerImpl.Builder builder) {
        super(builder);
    }

    public static EzyAppRoomManagerImpl.Builder builder() {
        return new EzyAppRoomManagerImpl.Builder();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public static class Builder
            extends EzyAbstractRoomManager.Builder<EzyAppRoomManagerImpl.Builder> {

        @Override
        public EzyAppRoomManager build() {
            return new EzyAppRoomManagerImpl(this);
        }
    }
}
