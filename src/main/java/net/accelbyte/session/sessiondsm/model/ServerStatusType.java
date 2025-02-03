/*
 * Copyright (c) 2025 AccelByte Inc. All Rights Reserved
 * This is licensed software from AccelByte Inc, for limitations
 * and restrictions contact your company contract manager.
 */

package net.accelbyte.session.sessiondsm.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ServerStatusType {

    public static final String CREATING = "CREATING";

    public static final String READY = "READY";

    public static final String BUSY = "BUSY";

    public static final String REMOVING = "REMOVING";

    public static final String UNREACHEABLE = "UNREACHEABLE";

    public static final String FAILED = "FAILED";

}
