package org.edx.indonesiax.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.interfaces.SectionItemInterface;
import org.edx.indonesiax.model.api.ChapterModel;
import org.edx.indonesiax.model.api.SectionItemModel;
import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.model.db.DownloadEntry.WatchedState;
import org.edx.indonesiax.module.db.DataCallback;
import org.edx.indonesiax.module.db.IDatabase;
import org.edx.indonesiax.util.AppConstants;
import org.edx.indonesiax.util.MemoryUtil;

public abstract class MyAllVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    IDatabase dbStore;
    public MyAllVideoAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_video_list, environment);
        this.dbStore = environment.getDatabase();
    }

    @Override
    public void render(BaseViewHolder tag, final SectionItemInterface sectionItem) {
        final ViewHolder holder = (ViewHolder) tag;

        if (sectionItem != null) {
            if (sectionItem.isChapter()) {
                holder.videolayout.setVisibility(View.GONE);
                ChapterModel c = (ChapterModel) sectionItem;
                holder.course_title.setText(c.name);
                holder.course_title.setVisibility(View.VISIBLE);
                holder.section_title.setVisibility(View.GONE);
            }
            else if (sectionItem.isSection()) {
                holder.videolayout.setVisibility(View.GONE);
                SectionItemModel s = (SectionItemModel) sectionItem;
                holder.section_title.setText(s.name);
                holder.section_title.setVisibility(View.VISIBLE);
                holder.course_title.setVisibility(View.GONE);
            }
            else {
                holder.course_title.setVisibility(View.GONE);
                holder.section_title.setVisibility(View.GONE);
                holder.videolayout.setVisibility(View.VISIBLE);

                DownloadEntry videoData = (DownloadEntry) sectionItem;
                String selectedVideoId = getVideoId();
                holder.videoTitle.setText(videoData.getTitle());

                holder.videoSize.setText(MemoryUtil.format(getContext(), videoData.size));
                holder.videoPlayingTime.setText(videoData.getDurationReadable());

                dbStore.getWatchedStateForVideoId(videoData.videoId,
                        new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(WatchedState result) {
                        WatchedState ws = result;
                        if(ws == null || ws == WatchedState.UNWATCHED) {
                            holder.video_watched_status.setImageResource(R.drawable.cyan_circle);
                        } else if(ws == WatchedState.PARTIALLY_WATCHED) {
                            holder.video_watched_status.setImageResource(R.drawable.ic_partially_watched);
                        } else {
                            holder.video_watched_status.setImageResource(R.drawable.grey_circle);
                        }
                    }
                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });

                if(videoData.isDownloaded()){
                    if (selectedVideoId != null) {
                        if (selectedVideoId.equalsIgnoreCase(videoData.videoId)) {
                            // mark this cell as selected and playing
                            holder.videolayout.setBackgroundResource(R.color.cyan_text_navigation_20);
                        } else {
                            // mark this cell as non-selected
                            holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                        }
                    } else {
                        holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                    }

                    if(AppConstants.myVideosDeleteMode){
                        holder.delete_checkbox.setVisibility(View.VISIBLE);
                        holder.delete_checkbox.setChecked(isSelected(holder.position));
                    }else{
                        holder.delete_checkbox.setVisibility(View.GONE);
                    }
                }else{
                    holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                    holder.delete_checkbox.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.videoTitle = (TextView) convertView
                .findViewById(R.id.video_title);
        holder.videoPlayingTime = (TextView) convertView
                .findViewById(R.id.video_playing_time);
        holder.videoSize = (TextView) convertView
                .findViewById(R.id.video_size);
        holder.video_watched_status = (ImageView) convertView
                .findViewById(R.id.video_watched_status);
        holder.course_title = (TextView) convertView
                .findViewById(R.id.txt_course_title);
        holder.section_title = (TextView) convertView
                .findViewById(R.id.txt_chapter_title);
        holder.videolayout = (RelativeLayout) convertView
                .findViewById(R.id.video_row_layout);
        holder.delete_checkbox  = (CheckBox) convertView
                .findViewById(R.id.video_select_checkbox);
        holder.delete_checkbox.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        if (isChecked) {
                            select(holder.position);
                            onSelectItem();
                        } else {
                            unselect(holder.position);
                            onSelectItem();
                        }
                    }
                });
        return holder;
    }


    private static class ViewHolder extends BaseViewHolder {
        TextView videoTitle;
        TextView videoPlayingTime;
        TextView videoSize;
        ImageView video_watched_status;
        CheckBox delete_checkbox;
        TextView course_title;
        TextView section_title;
        RelativeLayout videolayout;
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        selectedPosition=position;
        SectionItemInterface model = getItem(position);
        if(model!=null) onItemClicked(model, position);
    }

    public abstract void onItemClicked(SectionItemInterface model, int position);
    public abstract void onSelectItem();

}
