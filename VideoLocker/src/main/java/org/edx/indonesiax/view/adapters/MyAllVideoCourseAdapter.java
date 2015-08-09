package org.edx.indonesiax.view.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.edx.indonesiax.R;
import org.edx.indonesiax.core.IEdxEnvironment;
import org.edx.indonesiax.model.api.CourseEntry;
import org.edx.indonesiax.model.api.EnrolledCoursesResponse;
import org.edx.indonesiax.util.MemoryUtil;

public abstract class MyAllVideoCourseAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;

    public MyAllVideoCourseAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_myvideo_course_list, environment);
        lastClickTime = 0;
    }

    @Override
    public void render(BaseViewHolder tag, EnrolledCoursesResponse enrollment) {
        ViewHolder holder = (ViewHolder) tag;

        CourseEntry courseData = enrollment.getCourse();
        holder.courseTitle.setText(courseData.getName());

        String code = courseData.getOrg()+ " | " + courseData.getNumber();
        holder.schoolCode.setText(code);
        String videos=enrollment.getVideoCountReadable() + ",";
        holder.no_of_videos.setText(videos);
        holder.size_of_videos.setText(MemoryUtil.format(getContext(), enrollment.size));

        holder.courseImage.setDefaultImageResId(R.drawable.edx_map);
        holder.courseImage.setImageUrl(courseData.getCourse_image(environment.getConfig()),
            environment.getImageCacheManager().getImageLoader());
        holder.courseImage.setTag(courseData);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        holder.schoolCode = (TextView) convertView
                .findViewById(R.id.school_code);
        holder.courseImage = (NetworkImageView) convertView
                .findViewById(R.id.course_image);
        holder.no_of_videos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.size_of_videos = (TextView) convertView
                .findViewById(R.id.size_of_videos);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        NetworkImageView courseImage;
        TextView courseTitle;
        TextView schoolCode;
        TextView no_of_videos;
        TextView size_of_videos;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        //This has been used so that if user clicks continuously on the screen,
        //two activities should not be opened
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = getItem(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);
}
