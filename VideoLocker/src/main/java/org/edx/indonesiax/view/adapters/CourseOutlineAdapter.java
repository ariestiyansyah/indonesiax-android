package org.edx.indonesiax.view.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.base.MainApplication;
import org.edx.indonesiax.event.DownloadEvent;
import org.edx.indonesiax.logger.Logger;
import org.edx.indonesiax.model.course.BlockPath;
import org.edx.indonesiax.model.course.BlockType;
import org.edx.indonesiax.model.course.CourseComponent;
import org.edx.indonesiax.model.course.HasDownloadEntry;
import org.edx.indonesiax.model.course.IBlock;
import org.edx.indonesiax.model.course.VideoBlockModel;
import org.edx.indonesiax.model.db.DownloadEntry;
import org.edx.indonesiax.model.download.NativeDownloadModel;
import org.edx.indonesiax.module.db.DataCallback;
import org.edx.indonesiax.module.db.IDatabase;
import org.edx.indonesiax.module.prefs.PrefManager;
import org.edx.indonesiax.module.storage.IStorage;
import org.edx.indonesiax.third_party.iconify.IconView;
import org.edx.indonesiax.third_party.iconify.Iconify;
import org.edx.indonesiax.util.AppConstants;
import org.edx.indonesiax.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Used for pinned behavior.
 */
public class CourseOutlineAdapter extends BaseAdapter{

    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<HasDownloadEntry> models);
        void download(DownloadEntry videoData);
    }

    private CourseComponent rootComponent;
    private LayoutInflater mInflater;
    private List<SectionRow> mData;

    private IDatabase dbStore;
    private IStorage storage;
    private DownloadListener mDownloadListener;
    private Context context;

    private boolean currentVideoMode;
    private int numOfTotalUnits;

    public CourseOutlineAdapter(Context context, IDatabase dbStore, IStorage storage, DownloadListener listener) {
        this.context = context;
        this.dbStore = dbStore;
        this.storage = storage;
        this.mDownloadListener = listener;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = new ArrayList();
    }

    @Override public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public SectionRow getItem(int position) {
        if ( position < 0 || position >= mData.size() )
            return null;
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == SectionRow.ITEM;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case SectionRow.ITEM: {
                    convertView = mInflater.inflate(R.layout.row_course_outline_list, parent, false);
                    // apply a tag to this list row
                    ViewHolder tag = getTag(convertView);
                    convertView.setTag(tag);
                    break;
                }
                default: {//SectionRow.SECTION:
                    convertView = mInflater.inflate(R.layout.row_section_header, parent, false);
                    break;
                }
            }
        }

        switch (type) {
            case SectionRow.ITEM:
                return  getRowView(position, convertView, parent);
            default : //SectionRow.SECTION:
                return  getHeaderView(position, convertView, parent);
        }
    }

    /**
     * component can be null.
     * @IComponent component should be ICourse
     */
    public void setData(CourseComponent component){
        if (component != null &&  !component.isContainer())
            return;//
        this.rootComponent = component;
        this.numOfTotalUnits = 0;
        mData.clear();
        if ( rootComponent != null ) {
            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
            currentVideoMode = userPrefManager.isUserPrefVideoModel();
            List<IBlock> children = rootComponent.getChildren();
            this.numOfTotalUnits = children.size();
            for(IBlock block : children){
                CourseComponent comp = (CourseComponent)block;
                if ( currentVideoMode && comp.getBlockCount().videoCount == 0 )
                    continue;

                if ( comp.isContainer() ){
                    SectionRow header = new SectionRow(SectionRow.SECTION, comp );
                    mData.add( header );
                    for( IBlock childBlock : comp.getChildren() ){
                        CourseComponent child = (CourseComponent)childBlock;
                        if ( currentVideoMode && child.getBlockCount().videoCount == 0 )
                            continue;
                        SectionRow row = new SectionRow(SectionRow.ITEM, false, child );
                        mData.add( row );
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.ITEM, true, comp );
                    mData.add( row );
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reloadData(){
        if (  this.rootComponent != null )
            setData(this.rootComponent);
    }

    public View getRowView(int position, View convertView, ViewGroup parent) {
        final SectionRow row = this.getItem(position);
        final SectionRow nextRow = this.getItem(position+1);
        final CourseComponent component = row.component;
        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        if ( nextRow == null ){
            viewHolder.halfSeparator.setVisibility(View.GONE);
            viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.wholeSeparator.setVisibility(View.GONE);
            boolean isLastChildInBlock = !row.component.getParent().getId().equals( nextRow.component.getParent().getId());
            if( isLastChildInBlock ){
                viewHolder.halfSeparator.setVisibility(View.GONE);
            } else {
                viewHolder.halfSeparator.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.rowType.setVisibility(View.GONE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);

        if (component.isContainer()) {
            return getRowViewForContainer(position, convertView, parent, row);
        } else {
            return getRowViewForLeaf(position, convertView, parent, row);
        }
    }

    private  View getRowViewForLeaf(int position, View convertView, ViewGroup parent, final SectionRow row) {
        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        CourseComponent unit =  row.component;
        viewHolder.rowType.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.bulkDownload.setVisibility(View.INVISIBLE);

        if ( !unit.isResponsiveUI() && unit.getType() != BlockType.VIDEO){
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            viewHolder.rowType.setIcon(Iconify.IconValue.fa_laptop);
            viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_base));
        } else if (row.component instanceof VideoBlockModel){
            updateUIForVideo(position, convertView, viewHolder, row);
        } else {
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            if( unit.getType() == BlockType.PROBLEM ) {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_list);
            } else if( unit.getType() == BlockType.DISCUSSION ) {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_comments_o);
            } else {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_file_o);
            }
            checkAccessStatus(viewHolder, unit);
        }
        String title = unit.getDisplayName();
        if (TextUtils.isEmpty(title)) {
            //TODO - wait for production decision
            viewHolder.rowTitle.setText(R.string.untitled_block);
        } else {
            viewHolder.rowTitle.setText(unit.getDisplayName());
        }
        return convertView;
    }

    private void checkAccessStatus(final ViewHolder viewHolder, final CourseComponent unit) {
        dbStore.isUnitAccessed(new DataCallback<Boolean>(true) {
            @Override
            public void onResult(Boolean accessed) {
                if (accessed) {
                    viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_base));
                } else {
                    viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_brand_primary_base));
                }
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        }, unit.getId());
    }

    private void updateUIForVideo(int position, View convertView, final ViewHolder viewHolder, final SectionRow row ){
        VideoBlockModel unit = (VideoBlockModel) row.component;

        viewHolder.rowType.setIcon(Iconify.IconValue.fa_film);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);

        final DownloadEntry videoData =  unit.getDownloadEntry(storage);

        viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setText(videoData.getDurationReadable());

        if (videoData.downloaded == DownloadEntry.DownloadedState.DOWNLOADING) {

            NativeDownloadModel downloadModel = storage.
                getNativeDownlaod(videoData.dmId);
            if(downloadModel!=null){
                int percent = downloadModel.getPercent();
                if(percent>=0 && percent < 100){
                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                }
            }
        }

        dbStore.getWatchedStateForVideoId(videoData.videoId,
            new DataCallback<DownloadEntry.WatchedState>(true) {
                @Override
                public void onResult(DownloadEntry.WatchedState result) {
                    DownloadEntry.WatchedState ws = result;
                    if(ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_brand_primary_base));
                    } else if(ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_base));
                    } else {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_base));
                    }
                }
                @Override
                public void onFail(Exception ex) {
                    logger.error(ex);
                }
            });

        dbStore.getDownloadedStateForVideoId(videoData.videoId,
                new DataCallback<DownloadEntry.DownloadedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.DownloadedState result) {
                        DownloadEntry.DownloadedState ds = result;
                        if (ds == null || ds == DownloadEntry.DownloadedState.ONLINE) {
                            // not yet downloaded
                            viewHolder.bulkDownload.setVisibility(View.VISIBLE);
                            viewHolder.numOfVideoAndDownloadArea.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                    logger.debug("Download Button Clicked");
                                    //notifyDataSetChanged();
                                    mDownloadListener.download(videoData);
                                }
                            });
                        } else if (ds == DownloadEntry.DownloadedState.DOWNLOADING) {
                            // may be download in progress
                            EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                            viewHolder.bulkDownload.setVisibility(View.GONE);
                            storage.getDownloadProgressByDmid(videoData.dmId, new DataCallback<Integer>(true) {
                                @Override
                                public void onResult(Integer result) {
                                    if (result >= 0 && result < 100) {
                                        EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                    } else if (result == 100) {
                                        EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.COMPLETED));
                                    }
                                }

                                @Override
                                public void onFail(Exception ex) {
                                    logger.error(ex);
                                    viewHolder.bulkDownload.setVisibility(View.VISIBLE);
                                }
                            });
                        } else if (ds == DownloadEntry.DownloadedState.DOWNLOADED) {
                            // downloaded
                            viewHolder.bulkDownload.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                        viewHolder.bulkDownload.setVisibility(View.VISIBLE);
                    }
                });

    }

    private  View getRowViewForContainer(int position, View convertView, ViewGroup parent, final SectionRow row) {
        final CourseComponent component = row.component;
        String courseId = component.getCourseId();
        BlockPath path = component.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId =  path.get(2) == null ? "" : path.get(2).getDisplayName();

        ViewHolder holder = (ViewHolder)convertView.getTag();
        holder.rowTitle.setText(component.getDisplayName());
        holder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        if ( component.isGraded() ){
            holder.bulkDownload.setVisibility(View.INVISIBLE);
            holder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            holder.rowSubtitleIcon.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setText(component.getFormat());
        }

        //support video download for video type
        final int totalCount = component.getBlockCount().videoCount;
        if (totalCount == 0 ){
            holder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
        } else {
            holder.noOfVideos.setVisibility(View.VISIBLE);
            holder.noOfVideos.setText("" + totalCount);

            if (  row.numOfVideoNotDownloaded == 0 ){
                holder.bulkDownload.setVisibility(View.GONE);
            } else {
                int inProcessCount = dbStore.getVideosCountBySection(courseId, chapterId, sequentialId, null);
                int webOnlyCount = dbStore.getWebOnlyVideosCountBySection(courseId, chapterId, sequentialId, null);
                row.numOfVideoNotDownloaded = totalCount - inProcessCount - webOnlyCount;
                if (row.numOfVideoNotDownloaded > 0) {
                    holder.bulkDownload.setVisibility(View.VISIBLE);
                    holder.numOfVideoAndDownloadArea
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                mDownloadListener.download(component.getVideos());
                            }
                        });
                } else {
                    holder.bulkDownload.setVisibility(View.GONE);
                }
            }
        }

        if (AppConstants.offline_flag) {
            holder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
            boolean isVideoDownloaded = dbStore.isVideoDownloadedInSection
                (courseId, chapterId, sequentialId, null);
            if(isVideoDownloaded)
            {
                //TODO - any UI update
            }else{
                //TODO - any UI update
            }
        } else {
           //TODO - any UI update?
        }

        return convertView;
    }

    public  View getHeaderView(int position, View convertView, ViewGroup parent){
        final SectionRow row = this.getItem(position);
        TextView titleView = (TextView)convertView.findViewById(R.id.row_header);
        View separator = convertView.findViewById(R.id.row_separator);
        titleView.setText(row.component.getDisplayName());
        if ( position == 0) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    /**
     *
     * @return <code>true</code> if we rebuild the list due to the change of mode preference
     */
    public boolean checkModeChange(){
        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
        boolean modeInConfiguration = userPrefManager.isUserPrefVideoModel();
        if ( modeInConfiguration != currentVideoMode ){
            setData(rootComponent);
            return true;
        }  else {
            return false;
        }
    }

    /**
     * if the app is in the video-only mode, some unit will not show up
     */
    public boolean hasFilteredUnits(){
        return this.numOfTotalUnits > mData.size();
    }

    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowType = (IconView) convertView
                .findViewById(R.id.row_type);
        holder.rowTitle = (ETextView) convertView
                .findViewById(R.id.row_title);
        holder.rowSubtitle = (ETextView) convertView
                .findViewById(R.id.row_subtitle);
        holder.rowSubtitleIcon = (IconView) convertView
                .findViewById(R.id.row_subtitle_icon);
        holder.rowSubtitleIcon.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_light));
        holder.noOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (IconView) convertView
                .findViewById(R.id.bulk_download);
        holder.bulkDownload.setIconColor(context.getResources().getColor(
                R.color.edx_grayscale_neutral_base));
        holder.numOfVideoAndDownloadArea = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.rowSubtitlePanel =convertView.findViewById(R.id.row_subtitle_panel);
        holder.halfSeparator = convertView.findViewById(R.id.row_half_separator);
        holder.wholeSeparator = convertView.findViewById(R.id.row_whole_separator);

        return holder;
    }

    public static class ViewHolder{
        IconView rowType;
        ETextView rowTitle;
        ETextView rowSubtitle;
        IconView rowSubtitleIcon;
        IconView bulkDownload;
        TextView noOfVideos;
        LinearLayout numOfVideoAndDownloadArea;
        View rowSubtitlePanel;
        View halfSeparator;
        View wholeSeparator;
    }

    public static class SectionRow {
        public static final int ITEM = 0;
        public static final int SECTION = 1;

        public final int type;
        public final boolean topComponent;
        public final CourseComponent component;

        //field to cache the temp value
        public int numOfVideoNotDownloaded = -1;

        public SectionRow(int type, CourseComponent component) {
            this(type, false, component);
        }

        public SectionRow(int type, boolean topComponent, CourseComponent component) {
            this.type = type;
            this.topComponent = topComponent;
            this.component = component;
        }
    }
}
