package org.edx.indonesiax.test;

import org.edx.indonesiax.model.course.CourseStructureJsonHandler;
import org.edx.indonesiax.model.course.CourseStructureV1Model;
import org.edx.indonesiax.model.course.IBlock;
import org.edx.indonesiax.services.CourseManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * TODO - we will create a separate PR for course manager unit test.
  */
@Ignore
public class CourseManagerTest {
    CourseStructureV1Model model;
    @Before
    public  void setUp() throws Exception {
        //FIXME - there is a bug in android studio [up to version 1.2], test resource is not copied
        //we need to find a workaround to load resource.
        java.net.URL url = CourseManagerTest.class.getResource("course_outline.txt");
        String jsonString = new java.util.Scanner(new File(url.toURI()),"UTF8").useDelimiter("\\Z").next();
         model = new CourseStructureJsonHandler().processInput(jsonString);
    }

    @Test
    public void testNormalizeCourseStructure() throws Exception {
        IBlock block = CourseManager.normalizeCourseStructure(model, "");
    }



}
