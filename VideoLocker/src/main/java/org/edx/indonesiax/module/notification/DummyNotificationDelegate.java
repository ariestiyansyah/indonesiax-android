package org.edx.indonesiax.module.notification;

import org.edx.indonesiax.model.api.EnrolledCoursesResponse;

import java.util.List;

public class DummyNotificationDelegate implements NotificationDelegate{
    @Override
    public void unsubscribeAll() {

    }

    @Override
    public void resubscribeAll() {

    }

    @Override
    public void syncWithServerForFailure() {

    }

    @Override
    public void checkCourseEnrollment(List<EnrolledCoursesResponse> responses) {

    }

    @Override
    public void changeNotificationSetting(String courseId, String channelId, boolean subscribe) {

    }


    @Override
    public boolean isSubscribedByCourseId(String channel){
        return false;
    }

    @Override
    public void checkAppUpgrade(){}
}
