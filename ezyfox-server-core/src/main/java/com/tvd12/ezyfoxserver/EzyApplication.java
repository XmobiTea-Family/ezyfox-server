package com.tvd12.ezyfoxserver;

import com.tvd12.ezyfoxserver.app.EzyAppRequestController;
import com.tvd12.ezyfoxserver.setting.EzyAppSetting;
import com.tvd12.ezyfoxserver.wrapper.EzyAppUserManager;
import com.tvd12.ezyfoxserver.wrapper.EzyAppRoomManager;

public interface EzyApplication extends EzyServerChild {

    EzyAppSetting getSetting();

    EzyAppUserManager getUserManager();

    EzyAppRoomManager getRoomManager();

    EzyAppRequestController getRequestController();
}
