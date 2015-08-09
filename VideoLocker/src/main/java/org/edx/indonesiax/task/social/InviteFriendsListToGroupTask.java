package org.edx.indonesiax.task.social;

import android.content.Context;

import org.edx.indonesiax.services.ServiceManager;
import org.edx.indonesiax.task.Task;
import org.edx.indonesiax.util.JavaUtil;


public abstract class InviteFriendsListToGroupTask extends Task<Void> {

    Long[] friendList;
    Long groupId;
    String oauthToken;
    public InviteFriendsListToGroupTask(Context context, Long[] friendList, Long groupId, String oauthToken) {
        super(context);
        this.friendList = friendList;
        this.groupId = groupId;
        this.oauthToken = oauthToken;
    }

    @Override
    public Void call( ) {

        long[] primitiveList = JavaUtil.toPrimitive(friendList);

        ServiceManager api = environment.getServiceManager();

        try {

            api.inviteFriendsToGroup(primitiveList, groupId, oauthToken);
            return null;


        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
