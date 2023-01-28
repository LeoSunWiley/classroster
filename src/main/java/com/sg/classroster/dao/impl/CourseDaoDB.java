package com.sg.classroster.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sg.classroster.dao.CourseDao;
import com.sg.classroster.dao.impl.StudentDaoDB.StudentMapper;
import com.sg.classroster.dao.impl.TeacherDaoDB.TeacherMapper;
import com.sg.classroster.entity.Course;
import com.sg.classroster.entity.Student;
import com.sg.classroster.entity.Teacher;

@Repository
public class CourseDaoDB implements CourseDao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Course getCourseById(int id) {
		try {
			final String SELECT_COURSE_BY_ID = "SELECT * FROM course WHERE id = ?";
			Course course = jdbcTemplate.queryForObject(SELECT_COURSE_BY_ID, new CourseMapper(), id);
			course.setTeacher(getTeacherForCourse(id));
			course.setStudents(getStudentsForCourse(id));
			return course;
		} catch (DataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Course> getAllCourses() {
		final String SELECT_ALL_COURSES = "SELECT * FROM course";
		List<Course> courses = jdbcTemplate.query(SELECT_ALL_COURSES, new CourseMapper());
		associateTeacherAndStudents(courses);
		return courses;
	}

	@Override
	@Transactional
	public Course addCourse(Course course) {
		final String INSERT_COURSE = "INSERT INTO course(name, description, teacherId) VALUES(?,?,?)";
		jdbcTemplate.update(INSERT_COURSE, course.getName(), course.getDescription(), course.getTeacher().getId());
		
		int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
		course.setId(newId);
		insertCourseStudent(course);
		return course;
	}

	@Override
	@Transactional
	public void updateCourse(Course course) {
		final String UPDATE_COURSE = "UPDATE course SET name = ?, description = ?, teacherId = ? WHERE id = ?";
		jdbcTemplate.update(UPDATE_COURSE, course.getName(), course.getDescription(), course.getTeacher().getId(), course.getId());
		
		final String DELETE_COURSE_STUDENT = "DELETE FROM course_student WHERE courseId = ?";
		jdbcTemplate.update(DELETE_COURSE_STUDENT, course.getId());
		insertCourseStudent(course);
	}

	@Override
	@Transactional
	public void deleteCourseById(int id) {
		final String DELETE_COURSE_STUDENT = "DELETE FROM course_student WHERE courseId = ?";
		jdbcTemplate.update(DELETE_COURSE_STUDENT, id);
		
		final String DELETE_COURSE = "DELETE FROM course WHERE id = ?";
		jdbcTemplate.update(DELETE_COURSE, id);
	}

	@Override
	public List<Course> getCoursesForTeacher(Teacher teacher) {
		final String SELECT_COURSES_FOR_TEACHER = "SELECT * FROM course WHERE teacherId = ?";
		List<Course> courses = jdbcTemplate.query(SELECT_COURSES_FOR_TEACHER, new CourseMapper(), teacher.getId());
		associateTeacherAndStudents(courses);
		return courses;
	}

	@Override
	public List<Course> getCoursesForStudent(Student student) {
		final String SELECT_COURSES_FOR_STUDENT = "SELECT c.* FROM course c JOIN course_student cs ON cs.courseId = c.id WHERE cs.studentId = ?";
		List<Course> courses = jdbcTemplate.query(SELECT_COURSES_FOR_STUDENT, new CourseMapper(), student.getId());
		associateTeacherAndStudents(courses);
		return courses;
	}
	
	private Teacher getTeacherForCourse(int id) {
		final String SELECT_TEACHER_FOR_COURSE = "SELECT t.* FROM teacher t JOIN course c ON c.teacherId = t.id WHERE c.id = ?";
		return jdbcTemplate.queryForObject(SELECT_TEACHER_FOR_COURSE, new TeacherMapper(), id);
	}
	
	private List<Student> getStudentsForCourse(int id) {
		final String SELECT_STUDENTS_FOR_COURSE = "SELECT s.* FROM student s JOIN course_student cs ON cs.studentId = s.id WHERE cs.courseId = ?";
		return jdbcTemplate.query(SELECT_STUDENTS_FOR_COURSE, new StudentMapper(), id);
	}
	
	private void associateTeacherAndStudents(List<Course> courses) {
		for (Course course : courses) {
			course.setTeacher(getTeacherForCourse(course.getId()));
			course.setStudents(getStudentsForCourse(course.getId()));
		}
	}
	
	private void insertCourseStudent(Course course) {
		final String INSERT_COURSE_STUDENT = "INSERT INTO course_student(courseId, studentId) VALUES (?, ?)";
		for (Student student : course.getStudents()) {
			jdbcTemplate.update(INSERT_COURSE_STUDENT, course.getId(), student.getId());
		}
	}
	
	public static final class CourseMapper implements RowMapper<Course> {

		@Override
		@Nullable
		public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
			Course course = new Course();
			course.setId(rs.getInt("id"));
			course.setName(rs.getString("name"));
			course.setDescription(rs.getString("description"));
			
			return course;
		}
		
	}

}
