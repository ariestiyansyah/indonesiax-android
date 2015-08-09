package org.edx.indonesiax.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.social.SocialGroup;
import org.edx.indonesiax.util.ResourceUtil;
import org.edx.indonesiax.view.custom.SocialFacePileView;

import java.util.List;

public class GroupListAdapter extends SimpleAdapter<SocialGroup>  {

    private GroupFriendsListener groupFriendsListener;

    public GroupListAdapter(Context context, GroupFriendsListener groupFriendsListener) {
        super(context);
        this.groupFriendsListener = groupFriendsListener;
    }

    public GroupListAdapter(Context context, GroupFriendsListener groupFriendsListener, List<SocialGroup> data) {
        super(context, data);
        this.groupFriendsListener = groupFriendsListener;
    }

    public interface GroupFriendsListener {
        public void fetchGroupFriends(SocialGroup socialGroup);
    }

    @Override
    protected int getRowLayout() {
        return R.layout.group_list_item;
    }

    protected void setUpView(View view, final SocialGroup item) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.group_name);
            viewHolder.details = (TextView) view.findViewById(R.id.group_details);
            viewHolder.facePileView = (SocialFacePileView) view.findViewById(R.id.group_list_face_pile);
            viewHolder.unreadDisplay = (TextView) view.findViewById(R.id.group_unread);
            view.setTag(viewHolder);

        }

        if (item != null) {

            viewHolder.name.setText(item.getName());
            viewHolder.details.setText("");
            viewHolder.facePileView.clearAvatars();

            if (item.getUnread() > 0) {
                viewHolder.unreadDisplay.setText(Integer.toString(item.getUnread()));
                viewHolder.unreadDisplay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.unreadDisplay.setVisibility(View.GONE);
            }

            if (item.getMembers() == null) {
                if (groupFriendsListener != null){
                    groupFriendsListener.fetchGroupFriends(item);
                }
            } else {
                String details = ResourceUtil.getFormattedStringForQuantity(R.string.group_list_members, "members", item.getMembers().size()).toString();
                viewHolder.details.setText(details);
                viewHolder.facePileView.setMemberList(item.getMembers());
            }

        }

    }

    private class ViewHolder {
        private TextView name;
        private TextView details;
        private SocialFacePileView facePileView;
        private TextView unreadDisplay;
    }

}
